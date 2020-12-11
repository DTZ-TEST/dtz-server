package com.sy.util.statistics;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.entity.pojo.AgencyIncome;
import com.sy.entity.pojo.AgencyStatistics;
import com.sy.entity.pojo.GoldCardStatistics;
import com.sy.entity.pojo.GoldCommomStatistics;
import com.sy.entity.pojo.OnlineData;
import com.sy.entity.pojo.RoomCard;
import com.sy.entity.pojo.StatisticData;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.manager.CommonManager;
import com.sy.util.AccountUtil;
import com.sy.util.Constant;
import com.sy.util.PropUtil;
import com.sy.util.SqlHelperUtil;
import com.sy.util.StringUtil;

/**
 * Created by pc on 2017/5/10.
 */
@Component
public class CommonDataStatistics {
    private static final AtomicBoolean ATOMIC_BOOLEAN = new AtomicBoolean(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonDataStatistics.class);

    private static final Logger monitor = LoggerFactory.getLogger("MONITOR");

    @Autowired
    private CommonManager commonManager;

    @Scheduled(cron = "0 0 2 * * *")
    public void incomeStatistics() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        incomeStatistics(calendar.getTime(), commonManager);
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public void incomeMonthStatistics() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        String[] strs = StringUtil.loadMonthRange(calendar.getTime());
        incomeStatistics(strs[0], strs[1], commonManager, true,1);
    }

    @Scheduled(cron = "3 1 0 * * *")
    public void dataStatistics() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        dataStatistics(calendar.getTime(), commonManager, true);
    }

    public static final boolean incomeStatistics(Date date, CommonManager commonManager) {
    	Calendar calendar = Calendar.getInstance();
        //String[] strs = StringUtil.loadWeekRange(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String start = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        return incomeStatistics(start, start, commonManager, false,2);
    }

    public static final boolean incomeStatistics(String startDate, String endDate, CommonManager commonManager, boolean isMonth,Integer value) {
    	 String startTime = startDate + " 00:00:00";
         String endTime = endDate + " 23:59:59";
         startDate = startDate.replace("-", "");
         endDate = endDate.replace("-", "");
         Integer start = Integer.parseInt(startDate);
         Integer end = Integer.parseInt(endDate);
         RoomCard rc0 = new RoomCard();
         List<RoomCard> rcList;
         try {
                 rcList = commonManager.findList(rc0);
                 for (RoomCard rc : rcList) {
                     int temp1 = AccountUtil.countAgencyPay(commonManager, rc, startTime, endTime);
                     int temp2;
                     double tempAgencyRatio;
                     if (rc.getAgencyLevel() != null && rc.getAgencyLevel().intValue() == 99) {
                         temp2 = AccountUtil.countSubAgencyPay(commonManager, rc, startTime, endTime, 0, 200);
                         tempAgencyRatio = 0;
                     } else {
                         temp2 = AccountUtil.countSubAgencyPay(commonManager, rc, startTime, endTime, 0, 2);
                         tempAgencyRatio = 0.1;
                     }

                     double tempMineRatio;
                     if (isMonth) {
                         if (temp1 > 30000) {
                             tempMineRatio = 0.6;
                         } else if (temp1 > 20000) {
                             tempMineRatio = 0.55;
                         } else if (temp1 > 10000) {
                             tempMineRatio = 0.5;
                         } else if (temp1 > 5000) {
                             tempMineRatio = 0.45;
                         } else {
                             tempMineRatio = 0.4;
                         }
                     } else {
                         tempMineRatio = 0.4;
                     }

                     AgencyIncome agencyIncome = new AgencyIncome();
                     agencyIncome.setAgencyId(rc.getAgencyId());
                     agencyIncome.setAgencyIncome(new BigDecimal(temp2 * tempAgencyRatio / 10).setScale(2, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setAgencyLevel(rc.getAgencyLevel());
                     agencyIncome.setAgencyPay(new BigDecimal(temp2 * 1.0 / 10).setScale(2, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setAgencyPhone(rc.getAgencyPhone());
                     agencyIncome.setAgencyRatio(new BigDecimal(tempAgencyRatio).setScale(4, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setAgencyWX(rc.getAgencyWechat());
                     agencyIncome.setBankCard(rc.getBankCard());
                     agencyIncome.setBankName(rc.getBankName());
                     agencyIncome.setCreatedTime(new Date());
                     agencyIncome.setCurrentState("0");
                     agencyIncome.setEndDate(end);
                     agencyIncome.setMineIncome(new BigDecimal(temp1 * tempMineRatio / 10).setScale(2, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setMinePay(new BigDecimal(temp1 * 1.0 / 10).setScale(2, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setMineRatio(new BigDecimal(tempMineRatio).setScale(4, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setStartDate(start);
                     agencyIncome.setTotalIncome(new BigDecimal((temp1 * tempMineRatio / 10) + (temp2 * tempAgencyRatio / 10)).setScale(2, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setTotalPay(new BigDecimal((temp1 + temp2) * 1.0 / 10).setScale(2, BigDecimal.ROUND_HALF_UP));
                     agencyIncome.setUserName(rc.getUserName());
                     if (isMonth){
                         agencyIncome.setAgencyPhone(new BigDecimal((temp1 * tempMineRatio / 10) + (temp2 * tempAgencyRatio / 10)).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                         double tempCE=temp1 * (tempMineRatio - 0.4) / 10;
                         agencyIncome.setTotalIncome(new BigDecimal(tempCE<0?0:tempCE).setScale(2, BigDecimal.ROUND_HALF_UP));
                     }
                     commonManager.saveOrUpdate(SqlHelperUtil.getString("save_update", Constant.AGENCY_INCOME_FILE)
                             , new Object[]{agencyIncome.getStartDate(), agencyIncome.getEndDate(), agencyIncome.getAgencyId()
                                     , agencyIncome.getAgencyLevel(), agencyIncome.getUserName(), agencyIncome.getBankName(), agencyIncome.getBankCard()
                                     , agencyIncome.getAgencyPhone(), agencyIncome.getAgencyWX(), agencyIncome.getMinePay(), agencyIncome.getAgencyPay()
                                     , agencyIncome.getMineRatio(), agencyIncome.getAgencyRatio(), agencyIncome.getMineIncome()
                                     , agencyIncome.getAgencyIncome(), agencyIncome.getTotalPay(), agencyIncome.getTotalIncome(), agencyIncome.getCurrentState()
                                     , agencyIncome.getCreatedTime(),value

                                     , agencyIncome.getAgencyLevel(), agencyIncome.getUserName(), agencyIncome.getBankName(), agencyIncome.getBankCard()
                                     , agencyIncome.getAgencyPhone(), agencyIncome.getAgencyWX(), agencyIncome.getMinePay(), agencyIncome.getAgencyPay()
                                     , agencyIncome.getMineRatio(), agencyIncome.getAgencyRatio(), agencyIncome.getMineIncome()
                                     , agencyIncome.getAgencyIncome(), agencyIncome.getTotalPay(), agencyIncome.getTotalIncome()
                                     , agencyIncome.getCreatedTime(),value});
                 }
         } catch (Exception e) {
             LOGGER.error("Exception:" + e.getMessage(), e);
             return false;
         }
         return true;
    }

    public static final boolean dataStatistics(Date date, CommonManager commonManager) {
        return dataStatistics(date, commonManager, false);
    }

    public static final boolean dataStatistics(Date date, CommonManager commonManager,SelectDataBase.DataBase dataBase) {
        try {
            return dataStatistics(date, commonManager, false, dataBase);
        }catch (Exception e){
            LOGGER.error("Exception:"+e.getMessage(),e);
        }finally {
            loadAgencyStatistics(date, commonManager, true);
        }
        return false;
    }

    public static final boolean dataStatistics(Date date, CommonManager commonManager, boolean retention) {
        try {
            boolean bl1 = dataStatistics(date, commonManager, retention, SelectDataBase.DataBase.DB_1);
            boolean bl2 = dataStatistics(date, commonManager, retention, SelectDataBase.DataBase.DB_3);
            return bl1 || bl2;
        }catch (Exception e){
            LOGGER.error("Exception:"+e.getMessage(),e);
        }finally {
            loadAgencyStatistics(date, commonManager, true);
        }
        return false;
    }

    /**
     * 代理及其付费情况统计
     *
     * @param date
     * @param commonManager
     */
    public static final boolean loadAgencyStatistics(Date date, CommonManager commonManager, boolean force) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        String current = sdf.format(date);

        String cacheKey = "loadAgencyStatistics20170523:" + current;
        if (!force) {
            CacheEntity<String> cacheEntity = CacheEntityUtil.getCache(cacheKey);
            if (cacheEntity != null) {
                return false;
            }
        }

        int currentYMD = Integer.parseInt(sdf1.format(date));
        List<Map<String, Object>> list1;
        List<Map<String, Object>> list2;
        List<Map<String, Object>> list3;
        List<Map<String, Object>> list4;
        try {
            boolean updateAgencyStatistics = true;
            AgencyStatistics agencyStatistics = new AgencyStatistics();
            agencyStatistics.setDateTime(currentYMD);


                agencyStatistics = commonManager.findOne(agencyStatistics);
                list1 = commonManager.find(SqlHelperUtil.getString("agency_total_level", Constant.ROOMCARD_FILE)
                        , new Object[]{current + " 23:59:59"});
                list2 = commonManager.find(SqlHelperUtil.getString("agency_count_level", Constant.ROOMCARD_FILE)
                        , new Object[]{current + " 00:00:00", current + " 23:59:59"});
                list3 = commonManager.find(SqlHelperUtil.getString("agency_total_pay", Constant.ROOMCARD_FILE)
                        , new Object[]{current + " 23:59:59"});
                list4 = commonManager.find(SqlHelperUtil.getString("agency_count_pay", Constant.ROOMCARD_FILE)
                        , new Object[]{current + " 00:00:00", current + " 23:59:59"});



            if (agencyStatistics == null) {
                updateAgencyStatistics = false;
                agencyStatistics = new AgencyStatistics();
                agencyStatistics.setDateTime(currentYMD);
            }

            if (list1 != null && list1.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                int total = 0;
                for (Map<String, Object> map : list1) {
                    String level = String.valueOf(map.get("agencyLevel"));
                    int count = CommonUtil.object2Int(map.get("mycount"));
                    total += count;
                    strBuilder.append(level).append("_").append(count).append(",");
                }
                strBuilder.append("all").append("_").append(total);
                agencyStatistics.setAgencyTotal(strBuilder.toString());
            }

            if (list2 != null && list2.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                int total = 0;
                for (Map<String, Object> map : list2) {
                    String level = String.valueOf(map.get("agencyLevel"));
                    int count = CommonUtil.object2Int(map.get("mycount"));
                    total += count;
                    strBuilder.append(level).append("_").append(count).append(",");
                }
                strBuilder.append("all").append("_").append(total);
                agencyStatistics.setAgencyCount(strBuilder.toString());
            }

            if (list3 != null && list3.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                int total = 0;
                for (Map<String, Object> map : list3) {
                    String level = String.valueOf(map.get("agencyLevel"));
                    int count = CommonUtil.object2Int(map.get("mycount"));
                    total += count;
                    strBuilder.append(level).append("_").append(count).append(",");
                }
                strBuilder.append("all").append("_").append(total);
                agencyStatistics.setPayTotal(strBuilder.toString());
            }

            if (list4 != null && list4.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                int total = 0;
                for (Map<String, Object> map : list4) {
                    String level = String.valueOf(map.get("agencyLevel"));
                    int count = CommonUtil.object2Int(map.get("mycount"));
                    total += count;
                    strBuilder.append(level).append("_").append(count).append(",");
                }
                strBuilder.append("all").append("_").append(total);
                agencyStatistics.setPayCount(strBuilder.toString());
            }


                if (updateAgencyStatistics) {
                    return commonManager.update(agencyStatistics, new String[]{"dateTime"}, new Object[]{agencyStatistics.getDateTime()}) > 0;
                } else {
                    return commonManager.save(agencyStatistics) > 0;
                }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        } finally {
            CacheEntityUtil.setCache(cacheKey, new CacheEntity<>("1", 5 * 60));
        }

        return false;
    }

    private static final boolean dataStatistics(final Date date, CommonManager commonManager, boolean retention, SelectDataBase.DataBase dataBase) {

        if (ATOMIC_BOOLEAN.getAndSet(true)) {
            LOGGER.info("dataStatistics doing");
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");

        try {
            String current = sdf.format(date);

            List<Map<String, Object>> cardsList = commonManager.find(dataBase, SqlHelperUtil.getString("cards_statistics", Constant.USER_INFO_FILE)
                    , new Object[]{});
            LOGGER.info("cards statistics:{}", cardsList);
            if (cardsList != null && cardsList.size() == 1) {
                commonManager.saveOrUpdate(dataBase, SqlHelperUtil.getString("update", Constant.CARDS_STATISTICS_FILE)
                        , new Object[]{CommonUtil.object2Int(cardsList.get(0).get("mycount2"))
                                , CommonUtil.object2Int(cardsList.get(0).get("mycount1"))
                                , current});
            }

            int currentYMD = Integer.parseInt(sdf1.format(date));

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            cal.add(Calendar.DAY_OF_YEAR, -6);
            String pre7day = sdf.format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, -23);
            String pre30day = sdf.format(cal.getTime());

            String sql = SqlHelperUtil.getString("statistics_common_data", Constant.STATISTICS_PF_FILE);

            List<Object> params = new ArrayList<>(27);
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(pre7day + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(pre30day + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");
            params.add(current + " 00:00:00");
            params.add(current + " 23:59:59");

            List<Map<String, Object>> list = commonManager.find(dataBase, sql, params.toArray());

            if (list != null && list.size() >= 12) {
                monitor.info("current:{},datas:{}", current, list);
                StatisticData data = new StatisticData();
                data.setDateTime(currentYMD);
                data.setPf("ALL");

                StatisticData data1 = commonManager.findOne(dataBase, data);
                boolean isUpdate;
                if (data1 != null) {
                    data = data1;
                    isUpdate = true;
                } else {
                    isUpdate = false;
                }


                    List<Map<String, Object>> itemList = commonManager.find(dataBase,
                            SqlHelperUtil.getString("item_count", Constant.ORDER_INFO_FILE),
                            new Object[]{current + " 00:00:00", current + " 23:59:59"});
                    JSONObject extJson;
                    if (StringUtils.isBlank(data.getExtend())) {
                        extJson = new JSONObject();
                    } else {
                        extJson = JSONObject.parseObject(data.getExtend());
                    }

                    if (itemList != null && itemList.size() > 0) {
                        for (Map<String, Object> kv : itemList) {
                            extJson.put("item" + CommonUtil.object2Int(kv.get("myitem")), CommonUtil.object2Int(kv.get("mycount")));
                        }
                        data.setExtend(extJson.toString());
                    }
                    data.setPaySum(CommonUtil.object2Int(list.get(10).get("mycount"))+CommonUtil.object2Int(list.get(11).get("mycount")));

                data.setActive1day(CommonUtil.object2Int(list.get(2).get("mycount")));
                data.setActive7day(CommonUtil.object2Int(list.get(3).get("mycount")));
                data.setActive30day(CommonUtil.object2Int(list.get(4).get("mycount")));
                data.setRegTotalCount(CommonUtil.object2Int(list.get(0).get("mycount")));
                data.setFirstRegPayCount(CommonUtil.object2Int(list.get(5).get("mycount")));
                data.setFirstRegPaySum(CommonUtil.object2Int(list.get(6).get("mycount")));
                data.setFirstCount(CommonUtil.object2Int(list.get(7).get("mycount")));
                data.setFirstSum(CommonUtil.object2Int(list.get(8).get("mycount")));
                data.setPayCount(CommonUtil.object2Int(list.get(9).get("mycount")));

                data.setRegCount(CommonUtil.object2Int(list.get(1).get("mycount")));
                if (data.getActive1day() > 0) {
                    data.setPayRate((float) (data.getPayCount() * 1.0 / data.getActive1day()));
                } else {
                    data.setPayRate(0f);
                }

                if (data.getPayCount() > 0) {
                    data.setArpu((float) (data.getPaySum() * 1.0 / data.getPayCount()));
                } else {
                    data.setArpu(0f);
                }

                if (retention) {
                    params.clear();

                    Calendar cal0 = Calendar.getInstance();
                    cal0.setTime(date);
                    String cur = sdf.format(cal0.getTime());
                    cal0.add(Calendar.DAY_OF_YEAR, -1);
                    String pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -1);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -1);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -1);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -1);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -1);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -1);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -7);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");

                    cal0.add(Calendar.DAY_OF_YEAR, -16);
                    pre = sdf.format(cal0.getTime());
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");
                    params.add(cur + " 00:00:00");
                    params.add(cur + " 23:59:59");
                    params.add(pre + " 00:00:00");
                    params.add(pre + " 23:59:59");


                    sql = SqlHelperUtil.getString("statistics_retention_data", Constant.STATISTICS_PF_FILE);
                    List<Map<String, Object>> list0 = commonManager.find(dataBase, sql, params.toArray());
                    if (list0 != null && list0.size() >= 18) {
                        int i = 0;

                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(date);
                        cal2.add(Calendar.DAY_OF_YEAR, -1);
                        int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        StatisticData tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive1day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r1", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -1);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive2day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r2", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -1);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive3day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r3", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -1);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive4day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r4", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -1);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive5day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r5", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -1);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive6day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r6", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -1);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive7day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r7", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -7);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive14day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r14", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                        i += 2;

                        cal2.add(Calendar.DAY_OF_YEAR, -16);
                        tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                        tempData = new StatisticData();
                        tempData.setDateTime(tempYMD);
                        tempData.setPf("ALL");
                        try {
                            tempData = commonManager.findOne(dataBase, tempData);
                            if (tempData != null) {
                                tempData.setRegTotalAlive30day(CommonUtil.object2Int(list0.get(i).get("mycount")));

                                JSONObject jsonObject;
                                String ext = tempData.getExtend();
                                if (StringUtils.isNotBlank(ext)) {
                                    jsonObject = JSON.parseObject(ext);
                                } else {
                                    jsonObject = new JSONObject(true);
                                }
                                jsonObject.put("r30", CommonUtil.object2Int(list0.get(i + 1).get("mycount")));
                                tempData.setExtend(jsonObject.toString());

                                int tempRet = commonManager.update(dataBase, tempData, new String[]{"dateTime", "pf"}, new Object[]{tempData.getDateTime(), tempData.getPf()});
                                LOGGER.info("date:{},update data:{},result:{}", tempData.getDateTime(), JSON.toJSONString(tempData), tempRet);
                            }
                        } catch (Throwable th) {
                            LOGGER.error("Throwable msg:" + th.getMessage(), th);
                        }
                    }
                }

                if (isUpdate) {
                    int ret = commonManager.update(dataBase, data, new String[]{"dateTime", "pf"}, new Object[]{data.getDateTime(), data.getPf()});
                    LOGGER.info("date:{},update data:{},result:{},retention:{}", data.getDateTime(), JSON.toJSONString(data), ret, retention);
                } else {
                    int ret = commonManager.save(dataBase, data);
                    LOGGER.info("date:{},save data:{},result:{},retention:{}", data.getDateTime(), JSON.toJSONString(data), ret, retention);
                }
                return true;
            } else {
                monitor.error("current:{},datas:{}", current, list);
            }

        } catch (Throwable throwable) {
            LOGGER.error("Throwable:" + throwable.getMessage(), throwable);
        } finally {
            ATOMIC_BOOLEAN.compareAndSet(true, false);

            try{
                statisticsDownloadData(date,commonManager,dataBase);
            }catch (Throwable throwable) {
                LOGGER.error("Throwable:" + throwable.getMessage(), throwable);
            }
        }

        return false;
    }

    /**
     * 统计好友邀请日注册数据量
     * @param date
     * @param commonManager
     * @param dataBase
     * @throws Exception
     */
    private static void statisticsDownloadData(final Date date, CommonManager commonManager, SelectDataBase.DataBase dataBase) throws Exception{
        String gameId="all";
        if(dataBase==null){
            if (!"1".equals(PropUtil.getString("statisticsDownloadData"))){
                return;
            }
        }else{
            if (!StringUtils.contains(PropUtil.getString("statisticsDownloadData"),dataBase.name())){
                return;
            }else{
                gameId=dataBase.name().substring(3);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        String current = sdf.format(date);
        String current1 = sdf1.format(date);
        int dateInt=Integer.parseInt(current1);

        String date1 = PropUtil.getString(gameId + "_activity_start_time");
        String date2 = PropUtil.getString(gameId + "_activity_end_time");
        String date3 = current + " 00:00:00";
        String date4 = current + " 23:59:59";

        String date5;
        String date6;
        if (sdf.parse(date1).after(sdf.parse(date3))) {
            date5 = date1;
        } else {
            date5 = date3;
        }
        if (sdf.parse(date2).before(sdf.parse(date4))) {
            date6 = date2;
        } else {
            date6 = date4;
        }

        if (!sdf1.format(sdf.parse(date5)).equals(sdf1.format(sdf.parse(date6)))){
            return;
        }

        String statisticSql=SqlHelperUtil.getString("select_download_data",Constant.USER_INFO_FILE);

        final String sql="SELECT DISTINCT(inviterId) as userId FROM weixin_authorization ORDER BY userId ASC LIMIT ?,?";
        final int size=3000;
        final int value=100;//钻石
        final int maxCount=10;
        List<Map<String,Object>> list;
        int index=0;
        do {
            list=commonManager.find(dataBase,sql,new Object[]{index*size,size});
            if (list!=null){
                for (Map<String,Object> map:list){
                    List<Map<String,Object>> list1=commonManager.find(dataBase,statisticSql,new Object[]{date5,date6,map.get("userId")});
                    if (list1!=null&&list1.size()==1){
                        int count=CommonUtil.object2Int(list1.get(0).get("mycount"));
                        if (count>0){
                            AgencyIncome agencyIncome=new AgencyIncome();
                            agencyIncome.setStartDate(dateInt);
                            agencyIncome.setEndDate(agencyIncome.getStartDate());
                            agencyIncome.setAgencyId(CommonUtil.object2Int(map.get("userId")));
                            agencyIncome.setMinePay(new BigDecimal((count>maxCount?maxCount:count)*value));
                            agencyIncome.setMineIncome(new BigDecimal(count));
                            commonManager.save(dataBase,agencyIncome);
                        }
                    }
                }
            }
            index++;
        }while (list!=null&&list.size()==size);
    }
    
    @Scheduled(cron = "0 0/1 * * * *")
    public void onlinedData() {
        SimpleDateFormat sim = new SimpleDateFormat("HH");
        SimpleDateFormat simf = new SimpleDateFormat("yyyyMMdd");
        int time = Integer.parseInt(sim.format(new Date()));
        int dateTime = Integer.parseInt(simf.format(new Date()));
        if(time == 0){
        	Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            dateTime = Integer.parseInt(simf.format(calendar.getTime()));
        }
        try {
			onlinedData(commonManager,dateTime,time,SelectDataBase.DataBase.DB_1);
		} catch (Exception e) {
			LOGGER.info("++++++++++++++++++++++++++++++统计在线人数异常"+e.getMessage(),e);
		}
       /* try {
            onlinedData(commonManager,dateTime,time,SelectDataBase.DataBase.DB_3);
        } catch (Exception e) {
            LOGGER.info("++++++++++++++++++++++++++++++统计在线人数异常"+e.getMessage(),e);
        }*/
    }
    
    @Scheduled(cron = "0 0 0/1 * * *")
    public void jfonlinedData() {
        SimpleDateFormat sim = new SimpleDateFormat("HH");
        SimpleDateFormat simf = new SimpleDateFormat("yyyyMMdd");
        int time = Integer.parseInt(sim.format(new Date()));
        int dateTime = Integer.parseInt(simf.format(new Date()));
        if(time == 0){
        	Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            dateTime = Integer.parseInt(simf.format(calendar.getTime()));
        }
        try {
			jfonlinedData(commonManager,dateTime,time,1,15,16);
			LOGGER.info("jftj开始15,16服");
		} catch (Exception e) {
			LOGGER.info("++++++++++++++++++++++++++++++积分统计在线人数异常:"+e.getMessage(),e);
		}

        try {
        	LOGGER.info("jftj开始11服{1}");
            jfonlinedData(commonManager,dateTime,time,2,11);
        } catch (Exception e) {
            LOGGER.info("++++++++++++++++++++++++++++++积分统计在线人数异常:"+e.getMessage(),e);
        }
        
        /*try {
            jfonlinedData(commonManager,dateTime,time,3,18);
        } catch (Exception e) {
            LOGGER.info("++++++++++++++++++++++++++++++积分统计跑胡子在线人数异常:"+e.getMessage(),e);
        }*/
    }
    private static void onlinedData(CommonManager commonManager, int dateTime,int time,SelectDataBase.DataBase dataBase) throws Exception{
    	String hql = " select COALESCE(sum(onlineCount),0) as mycount from server_config where serverType <2 ";
    	int count = 0;
    	List<Map<String, Object>> map = commonManager.find(dataBase,hql , new Object[]{});
    	if(map != null && map.size() > 0){
    		count = Integer.parseInt(map.get(0).get("mycount").toString());
    	}
    	OnlineData data = new OnlineData();
    	data.setDateTime(dateTime);
    	data.setState(0);
    	data = commonManager.findOne(dataBase,data);
    	boolean isupdate = true;
    	if(data == null){
    		data = new OnlineData();
        	data.setDateTime(dateTime);
        	isupdate = false;
    	}
    	if(time == 0){
    		data.setTyfourtime(count);
    	}
    	else if(time == 1){
    		data.setOnetime(count);
    	}
    	else if(time == 2){
    		data.setTwotime(count);
    	}
    	else if(time == 3){
    		data.setThreetime(count);
    	}
    	else if(time == 4){
    		data.setFourtime(count);
    	}
    	else if(time == 5){
    		data.setFivetime(count);
    	}
    	else if(time == 6){
    		data.setSixtime(count);
    	}
    	else if(time == 7){
    		data.setSeventime(count);
    	}
    	else if(time == 8){
    		data.setEighttime(count);
    	}
    	else if(time == 9){
    		data.setNinetime(count);
    	}
    	else if(time == 10){
    		data.setTentime(count);
    	}
    	else if(time == 11){
    		data.setEleventime(count);
    	}
    	else if(time == 12){
    		data.setTlvtime(count);
    	}
    	else if(time == 13){
    		data.setThdtime(count);
    	}
    	else if(time == 14){
    		data.setFottime(count);
    	}
    	else if(time == 15){
    		data.setFivettime(count);
    	}
    	else if(time == 16){
    		data.setSixttime(count);
    	}
    	else if(time == 17){
    		data.setSvnttime(count);
    	}
    	else if(time == 18){
    		data.setEttime(count);
    	}
    	else if(time == 19){
    		data.setNttime(count);
    	}
    	else if(time == 20){
    		data.setTytime(count);
    	}
    	else if(time == 21){
    		data.setTyonetime(count);
    	}
    	else if(time == 22){
    		data.setTytwotime(count);
    	}
    	else if(time == 23){
    		data.setTythreetime(count);
    	}
    	data.setState(0);
    	if(isupdate){
    		commonManager.update(dataBase,data, new String[]{"dateTime","state"}, new Object[]{data.getDateTime(),"0"});
    	}else{
    		commonManager.save(dataBase,data);
    	}
    }

    private static void jfonlinedData(CommonManager commonManager, int dateTime,int time,int state,int ...serverIds) throws Exception{
    	String hql;
    	LOGGER.info("jftj开始11服");
    	if (serverIds!=null&&serverIds.length>0){
    	    StringBuilder stringBuilder = new StringBuilder("select COALESCE(sum(onlineCount),0) as mycount from server_config where serverType=2 and id in (");
    	    for (int serverId : serverIds){
                stringBuilder.append("'").append(serverId).append("',");
            }
            stringBuilder.setCharAt(stringBuilder.lastIndexOf(","),')');
            hql = stringBuilder.toString();
        }else{
            hql = "select COALESCE(sum(onlineCount),0) as mycount from server_config where serverType=2";
        }
    	int count = 0;
    	List<Map<String, Object>> map = commonManager.find(hql , new Object[]{});
    	if(map != null && map.size() > 0){
    		count = Integer.parseInt(map.get(0).get("mycount").toString());
    	}
    	OnlineData data = new OnlineData();
    	data.setDateTime(dateTime);
    	data.setState(state);
    	data = commonManager.findOne(data);
    	boolean isupdate = true;
    	if(data == null){
    		data = new OnlineData();
        	data.setDateTime(dateTime);
        	isupdate = false;
    	}
    	if(time == 0){
    		data.setTyfourtime(count);
    	}
    	else if(time == 1){
    		data.setOnetime(count);
    	}
    	else if(time == 2){
    		data.setTwotime(count);
    	}
    	else if(time == 3){
    		data.setThreetime(count);
    	}
    	else if(time == 4){
    		data.setFourtime(count);
    	}
    	else if(time == 5){
    		data.setFivetime(count);
    	}
    	else if(time == 6){
    		data.setSixtime(count);
    	}
    	else if(time == 7){
    		data.setSeventime(count);
    	}
    	else if(time == 8){
    		data.setEighttime(count);
    	}
    	else if(time == 9){
    		data.setNinetime(count);
    	}
    	else if(time == 10){
    		data.setTentime(count);
    	}
    	else if(time == 11){
    		data.setEleventime(count);
    	}
    	else if(time == 12){
    		data.setTlvtime(count);
    	}
    	else if(time == 13){
    		data.setThdtime(count);
    	}
    	else if(time == 14){
    		data.setFottime(count);
    	}
    	else if(time == 15){
    		data.setFivettime(count);
    	}
    	else if(time == 16){
    		data.setSixttime(count);
    	}
    	else if(time == 17){
    		data.setSvnttime(count);
    	}
    	else if(time == 18){
    		data.setEttime(count);
    	}
    	else if(time == 19){
    		data.setNttime(count);
    	}
    	else if(time == 20){
    		data.setTytime(count);
    	}
    	else if(time == 21){
    		data.setTyonetime(count);
    	}
    	else if(time == 22){
    		data.setTytwotime(count);
    	}
    	else if(time == 23){
    		data.setTythreetime(count);
    	}
    	data.setState(state);
    	if(isupdate){
    		commonManager.update(data, new String[]{"dateTime","state"}, new Object[]{dateTime,String.valueOf(state)});
    	}else{
    		commonManager.save(data);
    	}
    }
    
    /**
     * 统计积分的总和数据
     * @throws Exception
     */
    @Scheduled(cron = "0 3 0 * * *")
    public void StatisticData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        SimpleDateFormat simf = new SimpleDateFormat("yyyyMMdd");
        int dateTime = Integer.parseInt(simf.format(calendar.getTime()));
        String start = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        try {
            goldStatistics(calendar.getTime(),commonManager,dateTime,start, start);
        } catch (Exception e) {
            LOGGER.error("Exception:"+e.getMessage(),e);
        }
    }
    public static void goldStatistics(CommonManager commonManager) {
    	SimpleDateFormat simf = new SimpleDateFormat("yyyyMMdd");
        int dateTime = Integer.parseInt(simf.format(new Date()));
    	Calendar calendar = Calendar.getInstance();
        String start = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        try {
			goldStatistics(calendar.getTime(),commonManager,dateTime,start, start);
		} catch (Exception e) {
			LOGGER.error("Exception:"+e.getMessage(),e);
		}
    }
    
    public static void goldStatistics(CommonManager commonManager, int dateTime,String startDate, String endDate,Date date) {
    	try {
			goldStatistics(date,commonManager,dateTime,startDate, endDate);
		} catch (Exception e) {
			LOGGER.error("Exception:"+e.getMessage(),e);
		}
    }
    private static final void goldStatistics(Date date,CommonManager commonManager, int dateTime,String startDate, String endDate) throws Exception{
    	SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_1");
    	String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	GoldCommomStatistics data = new GoldCommomStatistics();
    	data.setDateTime(dateTime);
    	data = commonManager.findOne(data);
    	boolean isupdate = true;
    	if(data == null){
    		data = new GoldCommomStatistics();
        	data.setDateTime(dateTime);
        	isupdate = false;
    	}
    	String hql = "SELECT COUNT(1) AS mycount FROM t_gold_user  UNION ALL SELECT COUNT(1) AS mycount FROM t_gold_user WHERE regTime>=? AND regTime<=? UNION ALL SELECT COUNT(DISTINCT userId) as mycount FROM t_gold_user WHERE lastLoginTime>=? AND lastLoginTime<=? ";
    	List<Object> params = new ArrayList<>();
    	params.add(startTime);
    	params.add(endTime);
    	params.add(startTime);
    	params.add(endTime);
    	List<Map<String, Object>> map = commonManager.find(dataBase,hql, params.toArray());
    	data.setTotalUser(Integer.parseInt(map.get(0).get("mycount").toString()));
    	data.setAddUser(Integer.parseInt(map.get(1).get("mycount").toString()));
    	data.setDau(Integer.parseInt(map.get(2).get("mycount").toString()));
    	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
    	//统计留存
    	 String userLc = "SELECT COUNT(DISTINCT userId) as mycount FROM t_gold_user WHERE regTime>=? AND regTime<=? AND lastLoginTime>=? AND lastLoginTime<=?";
    	 Calendar cal2 = Calendar.getInstance();
         cal2.setTime(date);
         cal2.add(Calendar.DAY_OF_YEAR, -1);
    	 Calendar cal0 = Calendar.getInstance();
         cal0.setTime(date);
         String cur = sdf.format(cal0.getTime());
         cal0.add(Calendar.DAY_OF_YEAR, -1);
         String pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");

         List<Map<String, Object>> list1 = commonManager.find(dataBase, userLc, params.toArray());
         if (list1 != null && list1.size()>0){
                int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
                GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                if (tempData != null) {
                	tempData.setTwodaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	commonManager.update(tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                }
         }
         cal0.add(Calendar.DAY_OF_YEAR, -1);
         pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");
         list1 = commonManager.find( userLc, params.toArray());
         if (list1 != null && list1.size()>0){
        	 cal2.add(Calendar.DAY_OF_YEAR, -1);
        	 int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
        	    GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                 if (tempData != null) {
                	 tempData.setThreedaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	 commonManager.update( tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                 }
         }

         cal0.add(Calendar.DAY_OF_YEAR, -1);
         pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");
         list1 = commonManager.find(dataBase, userLc, params.toArray());
         if (list1 != null && list1.size()>0){
        	 cal2.add(Calendar.DAY_OF_YEAR, -1);
        	 int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
        	    GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                 if (tempData != null) {
                	 tempData.setFourdaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	 commonManager.update( tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                 }
         }

         cal0.add(Calendar.DAY_OF_YEAR, -1);
         pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");
         list1 = commonManager.find(dataBase, userLc, params.toArray());
         if (list1 != null && list1.size()>0){
        	 cal2.add(Calendar.DAY_OF_YEAR, -1);
        	 int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
        	 GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                 if (tempData != null) {
                	 tempData.setFivedaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	 commonManager.update( tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                 }
         }

         cal0.add(Calendar.DAY_OF_YEAR, -1);
         pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");
         list1 = commonManager.find(dataBase, userLc, params.toArray());
         if (list1 != null && list1.size()>0){
        	 cal2.add(Calendar.DAY_OF_YEAR, -1);
        	 int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
        	 GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                 if (tempData != null) {
                	 tempData.setSixdaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	 commonManager.update( tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                 }
         }

         cal0.add(Calendar.DAY_OF_YEAR, -1);
         pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");
         list1 = commonManager.find(dataBase, userLc, params.toArray());
         if (list1 != null && list1.size()>0){
        	 cal2.add(Calendar.DAY_OF_YEAR, -1);
        	 int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
        	 GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                 if (tempData != null) {
                	 tempData.setSevendaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	 commonManager.update(tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                 }
         }
         cal0.add(Calendar.DAY_OF_YEAR, -7);
         pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");
         list1 = commonManager.find(dataBase, userLc, params.toArray());
         if (list1 != null && list1.size()>0){
        	 cal2.add(Calendar.DAY_OF_YEAR, -7);
        	 int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
        	 GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                 if (tempData != null) {
                	 tempData.setFifteendaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	 commonManager.update( tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                 }
         }

         cal0.add(Calendar.DAY_OF_YEAR, -16);
         pre = sdf.format(cal0.getTime());
         params.clear();
         params.add(pre + " 00:00:00");
         params.add(pre + " 23:59:59");
         params.add(cur + " 00:00:00");
         params.add(cur + " 23:59:59");
         list1 = commonManager.find(dataBase, userLc, params.toArray());
         if (list1 != null && list1.size()>0){
        	 cal2.add(Calendar.DAY_OF_YEAR, -16);
        	 int tempYMD = Integer.parseInt(sdf1.format(cal2.getTime()));
        	 GoldCommomStatistics tempData = new GoldCommomStatistics();
                tempData.setDateTime(tempYMD);
                tempData = commonManager.findOne(dataBase, tempData);
                 if (tempData != null) {
                	 tempData.setMonthdaylc(Integer.parseInt(list1.get(0).get("mycount").toString()));
                	 commonManager.update( tempData, new String[]{"dateTime"}, new Object[]{tempData.getDateTime()});
                 }
         }

         //统计局数
         params.clear();
         params.add(startTime);
         params.add(endTime);
         params.add(startTime);
         params.add(endTime);
         params.add(startTime);
         params.add(endTime);
         //打筒子金币场
         List<Map<String, Object>> numap = commonManager.find(dataBase,"(select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='1154' and serverId in('15','16')  and currentState='2') UNION ALL (select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='1155'  and currentState='2' and serverId in('15','16')) UNION ALL (select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='1157'  and currentState='2' and serverId in('15','16'))", params.toArray());
         data.setCjTotal(Integer.parseInt(numap.get(0).get("sum2").toString()));
         data.setZjTotal(Integer.parseInt(numap.get(1).get("sum2").toString()));
         data.setGjTotal(Integer.parseInt(numap.get(2).get("sum2").toString()));
         data.setTotalNums(data.getCjTotal()+data.getZjTotal()+data.getGjTotal());

         //跑得快金币场
        numap = commonManager.find(dataBase,"(select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='161' and serverId ='11'  and currentState='2') UNION ALL (select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='162'  and currentState='2' and serverId ='11') UNION ALL (select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='163'  and currentState='2' and serverId='11')", params.toArray());
        data.setCjPdkTotal(Integer.parseInt(numap.get(0).get("sum2").toString()));
        data.setZjPdkTotal(Integer.parseInt(numap.get(1).get("sum2").toString()));
        data.setGjPdkTotal(Integer.parseInt(numap.get(2).get("sum2").toString()));
        data.setTotalPdkNums(data.getCjPdkTotal()+data.getZjPdkTotal()+data.getGjPdkTotal());

        //跑胡子金币场
        numap = commonManager.find(dataBase,"(select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='331' and serverId ='11'  and currentState='2') UNION ALL (select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='332'  and currentState='2' and serverId ='11') UNION ALL (select COALESCE(count(*),0) AS sum2 from t_gold_room where createdTime>=? and createdTime<=? and modeId='333'  and currentState='2' and serverId='11')", params.toArray());
        data.setCjphzTotal(Integer.parseInt(numap.get(0).get("sum2").toString()));
        data.setZjphzTotal(Integer.parseInt(numap.get(1).get("sum2").toString()));
        data.setGjphzTotal(Integer.parseInt(numap.get(2).get("sum2").toString()));
        data.setTotalphzNums(data.getCjphzTotal()+data.getZjphzTotal()+data.getGjphzTotal());
        
         //统计总用户和新增用户
         if(isupdate){
        	 commonManager.update(data, new String[]{"dateTime"}, new Object[]{data.getDateTime()});
         }else{
        	 commonManager.save(data);
         }
    }
    
    @Scheduled(cron = "30 2 0 * * *")
    public void cardStatistics() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        SimpleDateFormat simf = new SimpleDateFormat("yyyyMMdd");
        int dateTime = Integer.parseInt(simf.format(calendar.getTime()));
        String start = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        try {
            cardStatistics(commonManager,dateTime,start, start);
        } catch (Exception e) {
            LOGGER.error("Exception:"+e.getMessage(),e);
        }
    }
    public static void cardStatistics(CommonManager commonManager) {
    	SimpleDateFormat simf = new SimpleDateFormat("yyyyMMdd");
        int dateTime = Integer.parseInt(simf.format(new Date()));
    	Calendar calendar = Calendar.getInstance();
        String start = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        try {
        	cardStatistics(commonManager,dateTime,start, start);
		} catch (Exception e) {
			LOGGER.error("Exception:"+e.getMessage(),e);
		}
    }
    private static void cardStatistics(CommonManager commonManager, int dateTime,String startDate, String endDate) throws Exception{
    	SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_1");
    	String hql = " select COALESCE(sum(freeGold),0) as mycount from t_gold_user";
    	Long count = 0L;
    	String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";
    	List<Map<String, Object>> map = commonManager.find(dataBase,hql , new Object[]{});
    	if(map != null && map.size() > 0){
    		count = Long.parseLong(map.get(0).get("mycount").toString());
    	}
    	List<Object> params = new ArrayList<>();
    	GoldCardStatistics data = new GoldCardStatistics();
    	data.setDateTime(dateTime);
    	data = commonManager.findOne(data);
    	boolean isupdate = true;
    	if(data == null){
    		data = new GoldCardStatistics();
    		data.setDateTime(dateTime);
    		isupdate = false;
    	}
    	data.setTotalGold(count);
    	params.add(startTime);
    	params.add(endTime);
    	params.add(startTime);
    	params.add(endTime);
    	params.add(startTime);
    	params.add(endTime);

    	//打筒子金币场
    	List<Map<String, Object>> maps = commonManager.find(dataBase,"(select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='1154' and currentState='2' and createdTime>=? and createdTime<=? and serverId in('15','16')) UNION ALL (select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='1155' and currentState='2' and createdTime>=? and createdTime<=? and serverId in('15','16')) UNION ALL (select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='1157' and currentState='2' and createdTime>=? and createdTime<=? and serverId in('15','16'))", params.toArray());
    	data.setCjGold(Long.parseLong(maps.get(0).get("mycount").toString())*300*3);
    	data.setZjGold(Long.parseLong(maps.get(1).get("mycount").toString())*1500*3);
    	data.setGjGold(Long.parseLong(maps.get(2).get("mycount").toString())*6000*3);
    	data.setTotalService(data.getCjGold()+data.getZjGold()+data.getGjGold());

        //跑得快金币场
        maps = commonManager.find(dataBase,"(select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='161' and currentState='2' and createdTime>=? and createdTime<=? and serverId ='11')  UNION ALL (select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='162' and currentState='2' and createdTime>=? and createdTime<=? and serverId ='11') UNION ALL (select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='163' and currentState='2' and createdTime>=? and createdTime<=? and serverId ='11')", params.toArray());
        data.setCjPdkGold(Long.parseLong(maps.get(0).get("mycount").toString())*200*3);
        data.setZjPdkGold(Long.parseLong(maps.get(1).get("mycount").toString())*1000*3);
        data.setGjPdkGold(Long.parseLong(maps.get(2).get("mycount").toString())*5000*3);
        data.setTotalPdkService(data.getCjPdkGold()+data.getZjPdkGold()+data.getGjPdkGold());
        
      //跑胡子金币场
        maps = commonManager.find(dataBase,"(select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='331' and currentState='2' and createdTime>=? and createdTime<=? and serverId ='11')  UNION ALL (select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='332' and currentState='2' and createdTime>=? and createdTime<=? and serverId ='11') UNION ALL (select COALESCE(count(keyId),0) as mycount from t_gold_room where modeId='333' and currentState='2' and createdTime>=? and createdTime<=? and serverId ='11')", params.toArray());
        data.setCjphzGold(Long.parseLong(maps.get(0).get("mycount").toString())*200*3);
        data.setZjphzGold(Long.parseLong(maps.get(1).get("mycount").toString())*1000*3);
        data.setGjphzGold(Long.parseLong(maps.get(2).get("mycount").toString())*5000*3);
        data.setTotalphzService(data.getCjphzGold()+data.getZjphzGold()+data.getGjphzGold());

    	String hc = " select COALESCE(sum(itemAmount),0) AS sum1,COALESCE(sum(itemCount),0) AS sum2 from t_item_exchange where itemType=0 and createdTime>=? and createdTime<=? UNION ALL select COALESCE(sum(itemAmount),0) AS sum1,COALESCE(sum(itemCount),0) AS sum2 from t_item_exchange where itemType=1 and createdTime>=? and createdTime<=?";
    	params.clear();
    	params.add(startTime);
    	params.add(endTime);
    	params.add(startTime);
    	params.add(endTime);
    	List<Map<String, Object>> em =  commonManager.find(dataBase,hc,  params.toArray());
    	Long ce = 0L;
    	Long card1=Long.parseLong(em.get(0).get("sum1").toString());
    	Long card2=Long.parseLong(em.get(1).get("sum2").toString());
    	Long gold1=Long.parseLong(em.get(0).get("sum2").toString());
    	Long gold2 =Long.parseLong(em.get(1).get("sum1").toString());
    	
    	ce = card1-card2;
    	data.setCardce(ce);
    	data.setExchargeCard(gold2+"("+card2+")");
    	data.setExchargeGold(gold1+"("+card1+")");
    	if(isupdate){
    		commonManager.update(data, new String[]{"dateTime"}, new Object[]{dateTime});
    	}else{
    		commonManager.save(data);
    	}
    }

    public static void repairIncomeStatisticsOnDate(Date date, CommonManager commonManager) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
        incomeStatistics(dateStr, dateStr, commonManager, false, 2);
    }

    public static void repairIncomeStatisticsOnMonth(Date date, CommonManager commonManager) {
        String[] strs = StringUtil.loadMonthRange(date);
        incomeStatistics(strs[0], strs[1], commonManager, true, 1);
    }
}
