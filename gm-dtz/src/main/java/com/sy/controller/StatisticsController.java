package com.sy.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.entity.pojo.AgencyStatistics;
import com.sy.entity.pojo.GoldCommomStatistics;
import com.sy.entity.pojo.LoginData;
import com.sy.entity.pojo.OnlineData;
import com.sy.entity.pojo.RoomCard;
import com.sy.entity.pojo.StatisticData;
import com.sy.entity.pojo.SystemUser;
import com.sy.entity.pojo.UserStatistics;
import com.sy.general.GeneralHelper;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.MessageBuilder;
import com.sy.mainland.util.OutputUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.util.Constant;
import com.sy.util.LanguageUtil;
import com.sy.util.SqlHelperUtil;
import com.sy.util.StringUtil;
import com.sy.util.statistics.CommonDataStatistics;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/data/statistics/*"})
public class StatisticsController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/{varDate:\\d{8}}"})
    public void statistics(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "varDate") String varDate) throws Exception {
        try {
            Integer roleId = loadSystemUser(request).getRoleId();
            if (roleId == null || roleId.intValue() < 9) {
                OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
                return;
            }
            boolean bl = false;
            String item = request.getParameter("item");
            if ("1".equals(item)) {
                StatisticData data = new StatisticData();
                data.setDateTime(Integer.parseInt(varDate));
                data.setPf("ALL");

                data = commonManager.findOne(data);
                if (data != null) {
                    String current = CommonUtil.dateTimeToString(new SimpleDateFormat("yyyyMMdd").parse(varDate), "yyyy-MM-dd");
                    List<Map<String, Object>> itemList = commonManager.find(SqlHelperUtil.getString("item_count", Constant.ORDER_INFO_FILE),
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

                    bl = commonManager.saveOrUpdate("update statistics_platform set extend=? where dateTime=? and pf=?"
                            , new Object[]{data.getExtend(), data.getDateTime(), data.getPf()}) > 0;
                }
            } else if ("2".equals(item)){
                bl = CommonDataStatistics.loadAgencyStatistics(new SimpleDateFormat("yyyyMMdd").parse(varDate),commonManager,true);
            } else if ("3".equals(item)){
                Date date = new SimpleDateFormat("yyyyMMdd").parse(varDate);
                if ("month".equals(request.getParameter("type"))){
                    String[] strs = StringUtil.loadMonthRange(date);
                    bl = CommonDataStatistics.incomeStatistics(strs[0], strs[1], commonManager, true,1);
                }else{
                    bl = CommonDataStatistics.incomeStatistics(date,commonManager);
                }
            } else {
                Date date = new SimpleDateFormat("yyyyMMdd").parse(varDate);
                bl = CommonDataStatistics.dataStatistics(date, commonManager, true);
            }

            OutputUtil.output(1000, bl, request, response, false);
        } catch (Exception e) {
            OutputUtil.output(1002, "varDate is invalid:" + varDate, request, response, false);
        }
    }


    /**
     * @param request
     * @param response
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agentList"})
    public void agentList(HttpServletRequest request,
                          HttpServletResponse response) throws Exception {
        try {
            SystemUser systemUser = loadSystemUser(request);

            if ((systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0)) {
                OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
                return;
            }

            Map<String, String> params = UrlParamUtil.getParameters(request);
            logger.info("params:{}", params);
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");

            Date date1 = null, date2 = null;

            List<AgencyStatistics> list;
            if (StringUtils.isNotBlank(startDate)) {
                date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
            }
            if (StringUtils.isNotBlank(endDate)) {
                date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
            }
            Date currentDate = new Date();
            if (date2 != null) {
                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                    CommonDataStatistics.loadAgencyStatistics(currentDate, commonManager, false);
                }
                if (date1 != null) {
                    long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                    if (days < 0) {
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                                .builder("datas", list = new ArrayList<>()), request, response, null, false);
                        return;
                    } else {
                        if (days > 31) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date2);
                            cal.add(Calendar.DAY_OF_YEAR, -31);
                            date1 = cal.getTime();
                        }
                    }
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date2);
                    cal.add(Calendar.DAY_OF_YEAR, -31);
                    date1 = cal.getTime();
                }
            } else {
                if (date1 != null) {
                    if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                        date1 = currentDate;
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date1);
                    cal.add(Calendar.DAY_OF_YEAR, 31);
                    date2 = cal.getTime();

                    if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                        date2 = currentDate;
                        CommonDataStatistics.loadAgencyStatistics(currentDate, commonManager, false);
                    }
                } else {
                    CommonDataStatistics.loadAgencyStatistics(currentDate, commonManager, false);
                    date2 = currentDate;
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date2);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    date1 = cal.getTime();
                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

            Object[] objs = new Object[2];
            objs[0] = dateFormat.format(date1);
            objs[1] = dateFormat.format(date2);

            list = commonManager.findList(SqlHelperUtil.getString("select_agency_statistic", Constant.ROOMCARD_FILE),
                    objs, AgencyStatistics.class);

            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK").builder("datas", list)
                    , request, response, null, false);
        } catch (Exception e) {
            OutputUtil.output(1002, "system exception", request, response, false);
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/list"})
    public void list(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        SystemUser systemUser = loadSystemUser(request);

        if ((systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0)) {
            OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();

        List<Map<String, Object>> list;
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        CacheEntity<String> cacheEntity = CacheEntityUtil.getCache("todayDataStatistics"+dataBase.name());
        if (cacheEntity == null) {
            CacheEntityUtil.setCache("todayDataStatistics"+dataBase.name(), new CacheEntity<String>("1", 15));
            CommonDataStatistics.dataStatistics(new Date(), commonManager,dataBase);
        }

        list = commonManager.find(dataBase,SqlHelperUtil.getString("select_data", Constant.STATISTICS_PF_FILE)
                , new Object[]{"ALL", CommonUtil.dateTimeToString(date1, "yyyyMMdd"), CommonUtil.dateTimeToString(date2, "yyyyMMdd")}
        );

        List<StatisticData> dataList = (list == null ? new ArrayList<StatisticData>() : new ArrayList<StatisticData>(list.size()));
        if (list != null) {
            for (Map<String, Object> tempData : list) {
                dataList.add(CommonUtil.map2Entity(StatisticData.class, tempData));
            }
        }
        for(StatisticData data : dataList){
       	 SimpleDateFormat sim = new SimpleDateFormat("yyyyMMddHHmmss");
       	 String time1 = data.getDateTime().toString()+"000000";
            String time2 = data.getDateTime().toString()+"235959";
            Date d1 = sim.parse(time1);
            Date d2 = sim.parse(time2);
            int total = commonManager.count(SqlHelperUtil.getString("count_statistics_zc", Constant.STATISTICS_PF_FILE),
                    new Object[]{d1, d2});
            data.setPfPaySum(total);
       }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK").builder("datas", dataList)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/goldlist"})
    public void goldlist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        SystemUser systemUser = loadSystemUser(request);

        if ((systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0)) {
            OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();

        List<GoldCommomStatistics>  list;
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_1");
        CacheEntity<String> cacheEntity = CacheEntityUtil.getCache("todayDataStatisticsgold");
        if (cacheEntity == null) {
            CacheEntityUtil.setCache("todayDataStatisticsgold", new CacheEntity<String>("1", 15));
            CommonDataStatistics.goldStatistics(commonManager);
        }

        list = commonManager.findList(dataBase, "select * from gold_commom_statistics where dateTime>=? and dateTime<=? order by dateTime desc  ", new Object[]{CommonUtil.dateTimeToString(date1, "yyyyMMdd"), CommonUtil.dateTimeToString(date2, "yyyyMMdd")}, GoldCommomStatistics.class);
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK").builder("datas", list)
                , request, response, null, false);
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/onlineData"})
    public void onlineData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }
        
        SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
        int d1 = Integer.parseInt(sim.format(date1));
        int d2 = Integer.parseInt(sim.format(date2));
        String hql = " select * from online_data where dateTime>=? and dateTime<=? and state=0 order by dateTime desc";

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        List<OnlineData> list = commonManager.findList(dataBase,hql, new Object[]{d1,d2}, OnlineData.class);
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK").builder("datas", list==null?new ArrayList<>():list)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/jfonlineData"})
    public void jfonlineData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }
        
        SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
        int d1 = Integer.parseInt(sim.format(date1));
        int d2 = Integer.parseInt(sim.format(date2));
        int d3=0;
        if("pdk".equals(params.get("gameCode"))){
        	d3=2;
        }else if("dtz".equals(params.get("gameCode"))){
        	d3=1;
        }else if("phz".equals(params.get("gameCode"))){
        	d3=3;
        }
        String hql = " select * from online_data where dateTime>=? and dateTime<=? and state=? order by dateTime desc";
        List<OnlineData> list = commonManager.findList(hql, new Object[]{d1,d2,d3}, OnlineData.class);
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK").builder("datas", list==null?new ArrayList<>():list)
                , request, response, null, false);
    }
    
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pkdata"})
    public void pkdata(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        RoomCard roomCard = loadRoomCard(request);
        SystemUser systemUser = loadSystemUser(request);

        
        
        if ((systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0)) {
            OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();

        List<Map<String, Object>> list;
        List<Map<String, Object>> list2;
        List<Map<String, Object>> list3 = null;
        Map<String,Object> map3=new HashMap<>();
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", list = new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }


        list = commonManager.find("SELECT t.currentDate,COUNT(DISTINCT t.userId) userNum,t.roomType,SUM(t.gameCount1) playNum,0 countuserNum,SUM(t.gameCount0) xiaoplayNum  FROM t_user_statistics t  WHERE t.userId='system'   AND currentDate>=? AND currentDate<=? GROUP BY t.currentDate,roomType order by currentDate desc"
                , new Object[]{ CommonUtil.dateTimeToString(date1, "yyyyMMdd"), CommonUtil.dateTimeToString(date2, "yyyyMMdd")}
        );
        list2=commonManager.find("SELECT t.currentDate,COUNT(DISTINCT t.userId) userNum,roomType FROM t_user_statistics t WHERE t.userId != 'system'  AND currentDate >=?  AND currentDate <= ? GROUP BY t.currentDate,roomType ORDER BY currentDate DESC"
                , new Object[]{ CommonUtil.dateTimeToString(date1, "yyyyMMdd"), CommonUtil.dateTimeToString(date2, "yyyyMMdd")}
        );
        list3=commonManager.find("SELECT t.currentDate,COUNT(DISTINCT userId) userId FROM t_user_statistics t WHERE  currentDate >=? AND currentDate <= ? AND userId!='system'  GROUP BY t.currentDate ORDER BY currentDate DESC"
                , new Object[]{ CommonUtil.dateTimeToString(date1, "yyyyMMdd"), CommonUtil.dateTimeToString(date2, "yyyyMMdd")}
        );
        
        for(int i=0;list.size()-1>=i;i++){
        	for(int cc=0;list2.size()-1>=cc;cc++){
        		if(list2.get(cc).get("currentDate").equals(list.get(i).get("currentDate"))&&list2.get(cc).get("roomType").equals(list.get(i).get("roomType"))){
        			list.get(i).put("userNum", list2.get(cc).get("userNum"));
        		}
        	}

        }
        
        for(int i=0;list.size()-1>=i;i++){
        	for(int cc=0;list3.size()-1>=cc;cc++){
        		if(list3.get(cc).get("currentDate").equals(list.get(i).get("currentDate"))){
        			list.get(i).put("countuserNum", list3.get(cc).get("userId"));
        		}
        	}

        }
        System.out.println(list);
//        for(int i=0;list.size()-1>=i;i++){   
//        	if(list2.get(bb).get("currentDate").equals(list.get(i).get("currentDate"))&&list2.get(bb).get("roomType").equals(list.get(i).get("roomType"))){
//        		list.get(i).put("userNum", list2.get(bb).get("userNum"));
//        		bb++;
//        	}
//        }
        
        
        JSONArray jsonArray = new JSONArray();
        if(list!=null){
        	List<String> list0 = new ArrayList<String>();
        	for(Map<String, Object> map:list){
        		String curDate= String.valueOf(map.get("currentDate"));
        		String roomType = String.valueOf(map.get("roomType"));
        		
        		if(list0.contains(curDate)){
        			JSONObject json = jsonArray.getJSONObject(jsonArray.size()-1);
//        			json.put("userNum", CommonUtil.object2Int(map.get("userNum"))+json.getIntValue("userNum"));
        			json.put("playNum", CommonUtil.object2Int(map.get("playNum"))+json.getIntValue("playNum"));
        			json.put("xiaoplayNum", CommonUtil.object2Int(map.get("xiaoplayNum"))+json.getIntValue("xiaoplayNum"));
//        		
        			json.put(roomType, map);
        		}else{
        			list0.add(curDate);
        			JSONObject json1 =new JSONObject();
        			json1.put("currentDate", curDate);
        			json1.put("userNum", CommonUtil.object2Int(map.get("userNum")));
        			json1.put("playNum", CommonUtil.object2Int(map.get("playNum")));
        			json1.put("xiaoplayNum", CommonUtil.object2Int(map.get("xiaoplayNum")));
//        			
        			json1.put(roomType, map);
        			jsonArray.add(json1);
        		}
        	}
        }
        
        System.out.println(jsonArray);
        
       /* List<UserStatistics> dataList = (list == null ? new ArrayList<UserStatistics>() : new ArrayList<UserStatistics>(list.size()));
        for(UserStatistics data : dataList){
        	if(data.getRoomType().equals("common")){
        		data.setRoomType("普通场");
        	}else if(data.getRoomType().equals("group")){
        		data.setRoomType("亲友场");
        	}
       }
        if (list != null) {
            for (Map<String, Object> tempData : list) {
                dataList.add(CommonUtil.map2Entity(UserStatistics.class, tempData));
            }
        }*/
        
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK").builder("datas", jsonArray)
                , request, response, null, false);
    }
    
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/newDau"})
    public void newDau(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        SystemUser systemUser = loadSystemUser(request);

        if ((systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0)) {
            OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();

        List<Map<String, Object>> list;
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

      /*  CacheEntity<String> cacheEntity = CacheEntityUtil.getCache("todayDataStatistics"+dataBase.name());
        if (cacheEntity == null) {
            CacheEntityUtil.setCache("todayDataStatistics"+dataBase.name(), new CacheEntity<String>("1", 15));
            CommonDataStatistics.dataStatistics(new Date(), commonManager,dataBase);
        }*/

        list = commonManager.find(dataBase,"SELECT t1.currentDate,COUNT(t1.userId) as dau FROM t_login_data t1 WHERE t1.currentDate>=? AND t1.currentDate<=? GROUP BY t1.currentDate ORDER BY t1.currentDate DESC"
                , new Object[]{ CommonUtil.dateTimeToString(date1, "yyyyMMdd"), CommonUtil.dateTimeToString(date2, "yyyyMMdd")}
        );

//        List<LoginData> dataList = (list == null ? new ArrayList<LoginData>() : new ArrayList<LoginData>(list.size()));
//        if (list != null) {
//            for (Map<String, Object> tempData : list) {
//                dataList.add(CommonUtil.map2Entity(LoginData.class, tempData));
//            }
//        }
//        for(LoginData data : dataList){
//       	 SimpleDateFormat sim = new SimpleDateFormat("yyyyMMddHHmmss");
//       	 String time1 = data.getDateTime().toString()+"000000";
//            String time2 = data.getDateTime().toString()+"235959";
//            Date d1 = sim.parse(time1);
//            Date d2 = sim.parse(time2);
//            int total = commonManager.count(SqlHelperUtil.getString("count_statistics_zc", Constant.STATISTICS_PF_FILE),
//                    new Object[]{d1, d2});
//            data.setPfPaySum(total);
//       }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK").builder("datas", list)
                , request, response, null, false);
    }

    /**
     * 修复每日反佣金提现
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/repairIncome"})
    public void repairIncomeStatistics(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("repairIncomeStatistics|params|" + params);
        String dateStr = params.get("date"); //日期：格式20190820
        Date date = new SimpleDateFormat("yyyyMMdd").parse(dateStr);
        String type = params.get("type"); // 类型：1：修复当月数据，2：修复当天数据
        type = type == null ? "2" : type;
        if ("2".equals(type)) {
            CommonDataStatistics.repairIncomeStatisticsOnDate(date, commonManager);
        } else {
            CommonDataStatistics.repairIncomeStatisticsOnMonth(date, commonManager);
        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "OK"), request, response, null, false);
    }
}
