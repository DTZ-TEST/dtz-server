package com.sy.controller;

import com.sy.entity.pojo.RoomCard;
import com.sy.entity.pojo.SystemUser;
import com.sy.entity.pojo.UserInfo;
import com.sy.mainland.util.*;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.mainland.util.cache.TimeUnitSeconds;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.util.LanguageUtil;
import com.sy.util.StringUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/*"})
public class ManagerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/transfer"})
    public void agencyTransfer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        try {
            RoomCard roomCard=loadRoomCard(request);
            SystemUser systemUser = loadSystemUser(request);
            if (systemUser == null || systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0) {
                OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
                return;
            }

            String agencyId = params.get("agencyId");
            String parentId = params.get("parentId");

            String checkResult = StringUtil.checkBlank(false, params, "agencyId", "parentId");
            if (checkResult != null || !CommonUtil.isPureNumber(agencyId) || !CommonUtil.isPureNumber(parentId)||agencyId.equals(parentId)) {
                OutputUtil.output(1003, LanguageUtil.getString("param_error"), request, response, false);
                logger.info("checkResult:{}", checkResult);
            } else {
                RoomCard roomCard1 = new RoomCard();
                roomCard1.setAgencyId(Integer.parseInt(agencyId));
                roomCard1 = commonManager.findOne(roomCard1);
                if (roomCard1 == null) {
                    OutputUtil.output(1001, LanguageUtil.getString("agency_not_exists") + ":" + agencyId, request, response, false);
                } else {
                    RoomCard roomCard2 = new RoomCard();
                    roomCard2.setAgencyId(Integer.parseInt(parentId));
                    roomCard2 = commonManager.findOne(roomCard2);
                    if (roomCard2 == null) {
                        OutputUtil.output(1001, LanguageUtil.getString("agency_not_exists") + ":" + parentId, request, response, false);
                    } else {

                        if (roomCard1.getParentId()!=null&&roomCard1.getParentId().intValue()==roomCard2.getUserId().intValue()){
                            OutputUtil.output(1006, LanguageUtil.getString("operate_fail"), request, response, false);
                            logger.info("agency transfer fail:operator={},agencyId={},from={},to={}:{}",roomCard.getAgencyId(), agencyId, roomCard1.getParentId(), roomCard2.getUserId(), parentId);
                            return;
                        }

                        if (roomCard2.getParentId()!=null&&roomCard2.getParentId().intValue()==roomCard1.getUserId().intValue()){
                            OutputUtil.output(1007, LanguageUtil.getString("cyclic_dependency"), request, response, false);
                            logger.info("agency transfer fail:operator={},agencyId={},from={},to={}:{}",roomCard.getAgencyId(), agencyId, roomCard1.getParentId(), roomCard2.getUserId(), parentId);
                            return;
                        }

                        int ret = commonManager.saveOrUpdate("update roomcard set parentId=? where userId=? and parentId=?"
                                , new Object[]{roomCard2.getUserId(), roomCard1.getUserId(), roomCard1.getParentId()});
                        if (ret > 0) {
                            CacheEntityUtil.setCache("refresh agency" + agencyId, new CacheEntity<>("1", TimeUnitSeconds.CACHE_1_DAY));
                            OutputUtil.output(1000, LanguageUtil.getString("operate_success"), request, response, false);
                            logger.info("agency transfer success:operator={},agencyId={},from={},to={}:{}",roomCard.getAgencyId(), agencyId, roomCard1.getParentId(), roomCard2.getUserId(), parentId);
                        } else {
                            OutputUtil.output(1005, LanguageUtil.getString("operate_fail"), request, response, false);
                            logger.info("agency transfer fail:operator={},agencyId={},from={},to={}:{}",roomCard.getAgencyId(), agencyId, roomCard1.getParentId(), roomCard2.getUserId(), parentId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            OutputUtil.output(1002, LanguageUtil.getString("operate_fail"), request, response, false);
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/improve"})
    public void agencyImprove(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        try {
            RoomCard roomCard=loadRoomCard(request);
            SystemUser systemUser = loadSystemUser(request);
            if (systemUser == null || systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0) {
                OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
                return;
            }

            String agencyId = params.get("agencyId");

            if (!CommonUtil.isPureNumber(agencyId)){
                OutputUtil.output(1003, LanguageUtil.getString("param_error"), request, response, false);
            }else{
                RoomCard roomCard1 = new RoomCard();
                roomCard1.setAgencyId(Integer.parseInt(agencyId));
                roomCard1 = commonManager.findOne(roomCard1);
                if (roomCard1 == null) {
                    OutputUtil.output(1001, LanguageUtil.getString("agency_not_exists") + ":" + agencyId, request, response, false);
                } else {
                    if (roomCard1.getAgencyLevel()!=null&&roomCard1.getAgencyLevel().intValue()>=2){
                        OutputUtil.output(1005, LanguageUtil.getString("agency_auth_limit"), request, response, false);
                    }else{
                        int ret;
                        if (roomCard1.getAgencyLevel()==null){
                            ret=commonManager.saveOrUpdate("update roomcard set agencyLevel=1 where agencyId=?"
                            ,new Object[]{roomCard1.getAgencyId()});
                        }else{
                            ret=commonManager.saveOrUpdate("update roomcard set agencyLevel=agencyLevel+1 where agencyId=? and agencyLevel<2"
                                    ,new Object[]{roomCard1.getAgencyId()});
                        }
                        if (ret > 0) {
                            CacheEntityUtil.setCache("refresh agency" + agencyId, new CacheEntity<>("1", TimeUnitSeconds.CACHE_1_DAY));
                            OutputUtil.output(1000, LanguageUtil.getString("operate_success"), request, response, false);
                            logger.info("agency improve success:operator={},agencyId={},current={}",roomCard.getAgencyId(), agencyId, roomCard1.getAgencyLevel());
                        } else {
                            OutputUtil.output(1005, LanguageUtil.getString("operate_fail"), request, response, false);
                            logger.info("agency improve fail:operator={},agencyId={},current={}", roomCard.getAgencyId(),agencyId, roomCard1.getAgencyLevel());
                        }

                    }
                }
            }
        }catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            OutputUtil.output(1002, LanguageUtil.getString("operate_fail"), request, response, false);
        }

    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/reset/player/agency"})
    public void resetAgencyOfPlayer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        try {
            RoomCard roomCard=loadRoomCard(request);
            SystemUser systemUser = loadSystemUser(request);
            if (systemUser == null || systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0) {
                OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
                return;
            }

            String userId = params.get("userId");
            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

            if (!CommonUtil.isPureNumber(userId)){
                OutputUtil.output(1003, LanguageUtil.getString("param_error"), request, response, false);
            }else{
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(Long.parseLong(userId));
                userInfo = commonManager.findOne(dataBase,userInfo);
                if (userInfo == null) {
                    OutputUtil.output(1001, LanguageUtil.getString("player_not_exists") + ":" + userId, request, response, false);
                } else {
                    if (userInfo.getPayBindId()==null||userInfo.getPayBindId().intValue()<=0){
                        OutputUtil.output(1005, LanguageUtil.getString("reset_player_agency"), request, response, false);
                    }else{
                        int ret;

                        ret=commonManager.saveOrUpdate(dataBase,"update user_inf set payBindId=0 where userId=?"
                                    ,new Object[]{userInfo.getUserId()});

                        if (ret > 0) {
                            OutputUtil.output(1000, LanguageUtil.getString("operate_success"), request, response, false);
                            logger.info("reset player agency success:operator={},userId={},payBindId={},payBindTime={}",roomCard.getAgencyId(), userId,userInfo.getPayBindId(),userInfo.getPayBindTime());
                        } else {
                            OutputUtil.output(1005, LanguageUtil.getString("operate_fail"), request, response, false);
                            logger.info("reset player agency fail:operator={},userId={},payBindId={},payBindTime={}",roomCard.getAgencyId(), userId,userInfo.getPayBindId(),userInfo.getPayBindTime());
                        }

                    }
                }
            }
        }catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            OutputUtil.output(1002, LanguageUtil.getString("operate_fail"), request, response, false);
        }
    }

}
