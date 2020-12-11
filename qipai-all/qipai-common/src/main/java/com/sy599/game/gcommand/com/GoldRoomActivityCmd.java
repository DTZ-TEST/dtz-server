package com.sy599.game.gcommand.com;

import com.sy599.game.activity.goldroom.GoldRoomActivity;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.dao.LogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.GoldRoomActivityProto;
import com.sy599.game.msg.serverPacket.GoldRoomActivityProto.Award;
import com.sy599.game.msg.serverPacket.GoldRoomActivityProto.GoldRoomActivityMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GoldRoomActivityCmd extends BaseCommand {
    /*** 签到奖励积分数 **/
    public static final Map<Integer, Integer> signAward = new HashMap<>();

    {
        signAward.put(1, 500);
        signAward.put(2, 600);
        signAward.put(3, 700);
        signAward.put(4, 800);
        signAward.put(5, 900);
        signAward.put(6, 1000);
        signAward.put(7, 2000);
    }

    /*** 补签金币**/
    public static final int sign_gold = 200;
    /*** 破产补助单次积分数**/
    public static final int subsidyAward = 3000;
    /*** 邀请玩家每人积分数**/
    public static final int inviteUserAward = 3000;

    public static final int playType_subsidy = -1000;
    public static final int playType_sign = -1001;
    public static final int playType_lottery = -1002;
    public static final int playType_comboWin = -1003;
    public static final int playType_inviteUser = -1004;

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        if (req.getParamsCount() < 1) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        int optType = req.getParams(0);
        GoldRoomActivity activity = player.getMyActivity().getGoldRoomActivity();
        switch (optType) {
            case 1:
                // 获取数据
                getMsg(player, activity, req);
                break;
            case 2:
                // 领取破产补助
                subsidy(player, activity, req);
                break;
            case 3:
                // 签到
                sign(player, activity, req);
                break;
            case 4:
                // 累计胜利抽奖
                lottery(player, activity, req);
                break;
            case 5:
                // 连胜次数抽奖
                awardComboWin(player, activity, req);
                break;
            case 6:
                // 拉新奖励
                awardInviteUser(player, activity, req);
                break;
            default:
                player.writeErrMsg(LangMsg.code_3);

        }
    }

    public void setCommonMsg(ComReq req, GoldRoomActivityMsg.Builder msg) {
        if (req.getStrParamsCount() > 0) {
            msg.setReqCode(req.getStrParams(0));
        }
        msg.setSysInviteUserAward(inviteUserAward);
        msg.setSysSubsidyAward(subsidyAward);
        for (int i = 1; i <= 7; i++) {
            Award.Builder award = Award.newBuilder();
            award.setType(1);
            award.setValue(signAward.get(i));
            msg.addSysSignAward(award);
        }
    }

    /**
     * 获取数据
     *
     * @param player
     * @param activity
     * @throws Exception
     */
    public void getMsg(Player player, GoldRoomActivity activity, ComReq req) throws Exception {
        GoldRoomActivityMsg.Builder msg = activity.toProtoMsg();
        setCommonMsg(req, msg);

        Map<String, Object> inviteCountMap = UserDao.getInstance().loadGoldRoomActivityInvite(player.getUserId());
        if (inviteCountMap != null) {
            msg.setInviterUserCount1(Integer.valueOf(inviteCountMap.get("count1").toString()));
            msg.setInviterUserCount2(Integer.valueOf(inviteCountMap.get("count2").toString()));
        } else {
            msg.setInviterUserCount1(0);
            msg.setInviterUserCount2(0);
        }

        // 处理用户邀请的数据
        player.writeSocket(msg.build());
    }

    /**
     * 领取破产补助
     *
     * @param player
     * @param activity
     * @param req
     * @throws Exception
     */
    public void subsidy(Player player, GoldRoomActivity activity, ComReq req) {
        if (activity.getSubsidyCount() >= 1) {
            player.writeErrMsg(LangMsg.code_407);
            return;
        }
        int gold = subsidyAward;
        int combo = req.getParams(1);
        if (combo == 1) {
            gold *= 2;
        }
        activity.setSubsidyCount(activity.getSubsidyCount() + 1);
        player.changeActivity();
        player.saveBaseInfo();
        player.changeGold(gold, 0, playType_subsidy);
        GoldRoomActivityMsg.Builder msg = activity.toProtoMsg();
        setCommonMsg(req, msg);

        Award.Builder award = Award.newBuilder();
        award.setType(1);
        award.setValue(gold);
        msg.addAwardList(award);
        player.writeSocket(msg.build());
        LogUtil.monitorLog.info("GoldRoomActivityCmd|" + player.getUserId() + "|" + gold + "|" + req.getParamsList());
        LogDao.getInstance().insertLogActivityReward(player.getUserId(), playType_subsidy, 1, gold);
    }

    /**
     * 签到
     *
     * @param player
     * @param activity
     * @param req
     * @throws Exception
     */
    public void sign(Player player, GoldRoomActivity activity, ComReq req) {
        int day = req.getParams(1);
        Map<Integer, Integer> signatureBook = activity.getSignatureBook();
        if (signatureBook.containsKey(day) && (signatureBook.get(day) == 1 || signatureBook.get(day) == 2)) {
            player.writeErrMsg(LangMsg.code_401);
            return;
        }
        int dayOfWeek = TimeUtil.dayOfWeek();
        if (day > dayOfWeek) {
            player.writeErrMsg(LangMsg.code_402);
            return;
        }
        int state = 1; // 1正常签到，2补签
        if (day != dayOfWeek) {
            state = 2;
        }
        if (state == 2) {
            if (player.getGoldPlayer().getAllGold() < 200) {
                player.writeErrMsg(LangMsg.code_403, sign_gold);
                return;
            }
        }
        activity.getSignatureBook().put(day, state);
        player.changeActivity();
        player.saveBaseInfo();
        int gold = signAward.get(day);
        int sendGol = gold;
        if (state == 2) {
            gold -= 200;
        }
        player.changeGold(gold, 0, playType_sign);
        GoldRoomActivityMsg.Builder msg = GoldRoomActivityMsg.newBuilder();
        setCommonMsg(req, msg);

        GoldRoomActivityProto.SignatureBook.Builder signature = GoldRoomActivityProto.SignatureBook.newBuilder();
        signature.setDay(day);
        signature.setRes(state);
        msg.addSignatureBook(signature);

        Award.Builder award = Award.newBuilder();
        award.setType(1);
        award.setValue(sendGol);
        msg.addAwardList(award);
        player.writeSocket(msg.build());
        LogUtil.monitorLog.info("GoldRoomActivityCmd|" + player.getUserId() + "|" + gold + "|" + req.getParamsList());
        LogDao.getInstance().insertLogActivityReward(player.getUserId(), playType_sign, 1, gold);
    }

    /**
     * 累计胜利奖励
     *
     * @param player
     * @param activity
     * @param req
     * @throws Exception
     */
    public void lottery(Player player, GoldRoomActivity activity, ComReq req) {
        if (activity.getTotalWinAward() >= activity.getTotalWinAwardCount()) {
            player.writeErrMsg(LangMsg.code_404);
            return;
        }
        activity.setTotalWinAward(activity.getTotalWinAward() + 1);
        player.changeActivity();
        player.saveBaseInfo();
        GoldRoomActivityMsg.Builder msg = activity.toProtoMsg();
        setCommonMsg(req, msg);

        Award.Builder award = Award.newBuilder();
        Random r = new Random();
        int rnd = r.nextInt(100);
        int awardType = 1;
        int awardValue = 0;
        if (rnd < 70) {
            // 奖金币
            awardType = 1;
            awardValue = 300 + r.nextInt(501);
            player.changeGold(awardValue, 0, playType_lottery);
        } else {
            // 奖钻石
            awardType = 2;
            awardValue = 2 + r.nextInt(3);
            player.changeCards(awardValue, 0, true, CardSourceType.goldRoomActivity_lottery);

        }
        award.setType(awardType);
        award.setValue(awardValue);
        msg.addAwardList(award);
        player.writeSocket(msg.build());
        LogUtil.monitorLog.info("GoldRoomActivityCmd|" + player.getUserId() + "|" + awardType + "|" + awardValue + "|" + req.getParamsList());
        LogDao.getInstance().insertLogActivityReward(player.getUserId(), playType_lottery, awardType, awardValue);
    }

    /**
     * 连胜奖励
     *
     * @param player
     * @param activity
     * @param req
     * @throws Exception
     */
    public void awardComboWin(Player player, GoldRoomActivity activity, ComReq req) {

        if (activity.getComboWinAward().isEmpty()) {
            player.writeErrMsg(LangMsg.code_405);
            return;
        }
        Integer winCount = activity.getComboWinAward().remove(0);
        player.changeActivity();
        player.saveBaseInfo();
        int gold = (winCount - 2) * 100;
        player.changeGold(gold, 0, playType_comboWin);
        GoldRoomActivityMsg.Builder msg = activity.toProtoMsg();
        setCommonMsg(req, msg);

        Award.Builder award = Award.newBuilder();
        award.setType(1);
        award.setValue(gold);
        msg.addAwardList(award);
        player.writeSocket(msg.build());
        LogUtil.monitorLog.info("GoldRoomActivityCmd|" + player.getUserId() + "|" + gold + "|" + req.getParamsList());
        LogDao.getInstance().insertLogActivityReward(player.getUserId(), playType_comboWin, 1, gold);
    }

    /**
     * 拉新奖励
     *
     * @param player
     * @param activity
     * @param req
     * @throws Exception
     */
    public void awardInviteUser(Player player, GoldRoomActivity activity, ComReq req) throws Exception {

        Map<String, Object> inviteCountMap = UserDao.getInstance().loadGoldRoomActivityInvite(player.getUserId());
        if (inviteCountMap == null) {
            player.writeErrMsg(LangMsg.code_406);
            return;
        }
        Integer count1 = Integer.valueOf(inviteCountMap.get("count1").toString());
        if (count1 <= activity.getInviterUserAwardCount()) {
            player.writeErrMsg(LangMsg.code_406);
            return;
        }

        int gold = (count1 - activity.getInviterUserAwardCount()) * inviteUserAward;
        int combo = req.getParams(1);
        if (combo == 1) {
            gold *= 2;
        }
        activity.setInviterUserAwardCount(count1);
        activity.setInviterUserAwardGold(activity.getInviterUserAwardGold() + gold);
        player.changeActivity();
        player.saveBaseInfo();
        player.changeGold(gold, 0, playType_inviteUser);
        GoldRoomActivityMsg.Builder msg = activity.toProtoMsg();
        setCommonMsg(req, msg);

        // 处理用户邀请的数据
        if (inviteCountMap != null) {
            msg.setInviterUserCount1(Integer.valueOf(inviteCountMap.get("count1").toString()));
            msg.setInviterUserCount2(Integer.valueOf(inviteCountMap.get("count2").toString()));
        } else {
            msg.setInviterUserCount1(0);
            msg.setInviterUserCount2(0);
        }
        Award.Builder award = Award.newBuilder();
        award.setType(1);
        award.setValue(gold);
        msg.addAwardList(award);
        player.writeSocket(msg.build());
        LogUtil.monitorLog.info("GoldRoomActivityCmd|" + player.getUserId() + "|" + gold + "|" + req.getParamsList());
        LogDao.getInstance().insertLogActivityReward(player.getUserId(), playType_inviteUser, 1, gold);
    }


}
