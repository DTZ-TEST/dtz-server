package com.sy.util;

import com.sy.mainland.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by pc on 2017/4/14.
 */
public final class StringUtil {

    public static int loadAgencyId(){
        SecureRandom secureRandom=new SecureRandom();
        int base;
        if("random".equals(PropUtil.getString("agency_mode"))){
            base=100001+secureRandom.nextInt(899999);
            while (filterId(base)){
                base=100001+secureRandom.nextInt(899999);
            }
        }else{
            String str;
            StringBuilder strBuilder=new StringBuilder(6);
            int first=secureRandom.nextInt(9)+1;
            strBuilder.append(first).append(first);
            strBuilder.append(secureRandom.nextInt(10)).append(secureRandom.nextInt(10));
            int last=secureRandom.nextInt(10);
            strBuilder.append(last).append(last);

            str=strBuilder.toString();
            while (filterId(str)){
                strBuilder.setLength(0);
                first=secureRandom.nextInt(9)+1;
                strBuilder.append(first).append(first);
                strBuilder.append(secureRandom.nextInt(10)).append(secureRandom.nextInt(10));
                last=secureRandom.nextInt(10);
                strBuilder.append(last).append(last);
                str=strBuilder.toString();
            }

            base=Integer.parseInt(str);
        }
        return base;
    }

    private static boolean filterId(int id){
        return filterId(String.valueOf(id));
    }
    /**
     * 过滤(2017.04.11添加)<br/>
     * 4A/5A/6A，如：102222、85555<br/>
     ABCD/ABCDE/ABCDEF，如：201234、56789<br/>
     3A/3B，如：111222<br/>
     * @param id
     * @return
     */
    private static boolean filterId(String id){
        String userIdStr=id;
        if (userIdStr.length()>=4){
            int count=1;
            int temp=userIdStr.charAt(userIdStr.length()-1)-userIdStr.charAt(userIdStr.length()-2);

            switch (temp){
                case 0:
                    boolean isAAABBB=false;
                    for (int i=userIdStr.length()-2;i>=1;i--){
                        if (userIdStr.charAt(i)-userIdStr.charAt(i-1)==0){
                            count++;
                            if (count>=3||(isAAABBB&&count>=2)){
                                return true;
                            }
                        }else{
                            if(count>=2){
                                count=0;
                                isAAABBB=true;
                            }else{
                                return false;
                            }
                        }
                    }
                    break;
                case 1:
                    for (int i=userIdStr.length()-2;i>=1;i--){
                        if (userIdStr.charAt(i)-userIdStr.charAt(i-1)==1){
                            count++;
                            if (count>=3){
                                return true;
                            }
                        }else{
                            return false;
                        }
                    }
                    break;
                case -1:
                    for (int i=userIdStr.length()-2;i>=1;i--){
                        if (userIdStr.charAt(i)-userIdStr.charAt(i-1)==-1){
                            count++;
                            if (count>=3){
                                return true;
                            }
                        }else{
                            return false;
                        }
                    }
                    break;
                default:
                    return false;
            }

        }
        return false;
    }

    /**
     * 判断当前号码是否是正确的手机号码
     * @param phoneNumber
     * @return
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        if(phoneNumber!=null&&phoneNumber.matches("^1[34578]\\d{9}$")) {
            return true;
        }
        return false;
    }

    /**
     * 获取所在周的开始日期和结束日期
     * @param date
     * @return
     */
    public static String[] loadWeekRange(Date date){
        String[] strs=new String[2];

        Calendar calendar=Calendar.getInstance();
        if (date!=null){
            calendar.setTime(date);
        }
        int day=calendar.get(Calendar.DAY_OF_WEEK)-1;

        if (day>0){
            calendar.add(Calendar.DAY_OF_YEAR,7-day);
        }

        strs[1]=CommonUtil.dateTimeToString(calendar.getTime(),"yyyy-MM-dd");
        calendar.add(Calendar.DAY_OF_YEAR,-6);
        strs[0]=CommonUtil.dateTimeToString(calendar.getTime(),"yyyy-MM-dd");
        return strs;
    }

    /**
     * 获取所在月的开始日期和结束日期
     * @param date
     * @return
     */
    public static String[] loadMonthRange(Date date){
        String[] strs=new String[2];

        Calendar calendar=Calendar.getInstance();

        if (date!=null){
            calendar.setTime(date);
        }

        int currentMonth=calendar.get(Calendar.MONTH)+1;

        strs[0]=new StringBuilder().append(calendar.get(Calendar.YEAR)).append("-")
                .append(currentMonth>=10?"":"0")
                .append(currentMonth)
                .append("-01").toString();

        strs[1]=new StringBuilder().append(calendar.get(Calendar.YEAR)).append("-")
                .append(currentMonth>=10?"":"0")
                .append(currentMonth).append("-")
                .append(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toString();

        return strs;
    }

    /**
     * 跨月
     * @param year
     * @param month 1~12
     * @return
     */
    public static List<String> loadWeekRange1(int year , int month){
        List<String> list=new ArrayList<>();

        Calendar calendar=Calendar.getInstance();
        calendar.setLenient(true);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,1);

        String[] strs=loadWeekRange(calendar.getTime());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String[] temps=strs[0].split("\\-");
        int[] tempInts=new int[temps.length];
        for (int i=0;i<temps.length;i++){
            tempInts[i]=Integer.parseInt(temps[i]);
        }
        tempInts[1]-=1;

        String[] temps1=strs[1].split("\\-");
        int[] tempInts1=new int[temps1.length];
        for (int i=0;i<temps1.length;i++){
            tempInts1[i]=Integer.parseInt(temps1[i]);
        }
        tempInts1[1]-=1;

        if (tempInts[0]==calendar.get(Calendar.YEAR)&&tempInts[1]==calendar.get(Calendar.MONTH)){
            list.add(strs[0]+"~"+strs[1]);
        }

        calendar.set(Calendar.YEAR,tempInts1[0]);
        calendar.set(Calendar.MONTH,tempInts1[1]);
        calendar.set(Calendar.DAY_OF_MONTH,tempInts1[2]);

        while (true){
            calendar.add(Calendar.DAY_OF_YEAR,1);
            if (tempInts1[1]!=calendar.get(Calendar.MONTH)){
                break;
            }else{
                StringBuilder stringBuilder=new StringBuilder(21);
                stringBuilder.append(sdf.format(calendar.getTime())).append("~");
                calendar.add(Calendar.DAY_OF_YEAR,6);
                stringBuilder.append(sdf.format(calendar.getTime()));
                list.add(stringBuilder.toString());

                if (tempInts1[1]!=calendar.get(Calendar.MONTH)){
                    break;
                }
            }
        }

        return list;
    }

    /**
     * 不跨月
     * @param year
     * @param month 1~12
     * @return
     */
    public static List<String> loadWeekRange(int year , int month){
        List<String> list=new ArrayList<>();

        StringBuilder stringBuilder=new StringBuilder().append(year).append("-");
        if (month<10){
            stringBuilder.append("0");
        }
        stringBuilder.append(month).append("-");

        String base=stringBuilder.toString();

        Calendar calendar=Calendar.getInstance();
        calendar.setLenient(true);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,1);

        int totalDay=calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int week1=calendar.get(Calendar.WEEK_OF_MONTH);

        list.add(new StringBuilder().append(base).append("01").toString());

        for(int i=1;i<=totalDay;i++){
            int day1=calendar.get(Calendar.DAY_OF_MONTH);
            calendar.add(Calendar.DAY_OF_YEAR,1);
            int week2=calendar.get(Calendar.WEEK_OF_MONTH);
            if (week1!=week2){
                StringBuilder tempBuilder=new StringBuilder();
                if (day1>1){
                    int idx=list.size()-1;
                    tempBuilder.append(list.get(idx));
                    tempBuilder.append("~").append(base);
                    if (day1<10){
                        tempBuilder.append("0");
                    }
                    tempBuilder.append(day1);
                    list.set(idx,tempBuilder.toString());
                    tempBuilder.setLength(0);
                }

                if (week2>1){
                    int day2=i+1;
                    calendar.add(Calendar.DAY_OF_YEAR,1);
                    tempBuilder.append(base);
                    if (day2<10){
                        tempBuilder.append("0");
                    }
                    tempBuilder.append(day2);

                    list.add(tempBuilder.toString());
                    i++;
                }
                week1=week2;
            }
        }

        return list;
    }

    /**
     * 检查是否有空值，并返回提示信息
     *
     * @param returnOnlyName
     * @param params
     * @param names
     * @return
     */
    public static String checkBlank(boolean returnOnlyName, Map<String, String> params, String... names) {
        String result = null;
        for (int i = 0, len = names.length; i < len; i++) {
            if (StringUtils.isBlank(params.get(names[i]))) {
                return returnOnlyName ? names[i] : new StringBuilder(names[i]).append(" is blank").toString();
            }
        }
        return result;
    }

    /**
     * 获取随机数
     *
     * @param size
     * @param type CharacterType
     * @return
     */
    public static final String getRandomString(int size, CharacterType type) {
        if (size <= 0) {
            throw new IllegalArgumentException("size<=0 error");
        }
        String str;
        switch (type) {
            case DIGIT:
                str = "0123456789";
                break;
            case LETTER:
                str = "qwertyuipasdfghjkzxcvbnm";
                break;
            default:
                str = "0123456789qwertyuipasdfghjkzxcvbnm";
                break;
        }

        StringBuilder strBuilder = new StringBuilder();
        int strLenth = str.length();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < size; i++) {
            strBuilder.append(str.charAt(random.nextInt(strLenth)));
        }
        return strBuilder.toString();
    }

    public enum CharacterType {
        /**
         * 数字
         */
        DIGIT,
        /**
         * 字母
         */
        LETTER,
        /**
         * 数字和字母
         */
        DIGIT_LETTER
    }
}
