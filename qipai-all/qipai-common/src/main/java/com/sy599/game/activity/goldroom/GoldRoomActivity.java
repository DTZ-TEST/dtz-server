package com.sy599.game.activity.goldroom;

import com.alibaba.fastjson.TypeReference;
import com.sy599.game.msg.serverPacket.GoldRoomActivityProto.GoldRoomActivityMsg;
import com.sy599.game.msg.serverPacket.GoldRoomActivityProto.SignatureBook;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldRoomActivity {
    /*** 更新时间**/
    private Date today;

    /*** 破产补助：今天已领取次数**/
    private Integer subsidyCount;

    /*** 本周签到表 <星期(1-7)，是否签到(0否，1是,2补签)>**/
    private Map<Integer, Integer> signatureBook;

    /*** 累计胜利：今天累计胜利次数**/
    private Integer totalWinCount;
    /*** 累计胜利：今天已抽奖次数**/
    private Integer totalWinAward;

    /*** 连胜次数**/
    private Integer comboWinCount;
    /*** 连胜奖励：列表记录有连胜次数的奖励，如，[3，4]表示还有3次，4次连胜奖励未领，领取一个删除一个**/
    private List<Integer> comboWinAward;

    /*** 拉新领取奖励人数**/
    private Integer inviterUserAwardCount;

    /*** 拉新领取总奖励积分**/
    private Integer inviterUserAwardGold;


    public GoldRoomActivity() {
        this.today = new Date();
        this.subsidyCount = 0;
        this.signatureBook = new HashMap<>();
        this.totalWinCount = 0;
        this.totalWinAward = 0;
        this.comboWinCount = 0;
        this.comboWinAward = new ArrayList<>();
        this.inviterUserAwardCount = 0;
        this.inviterUserAwardGold = 0;
    }

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }

    public Integer getSubsidyCount() {
        return subsidyCount;
    }

    public void setSubsidyCount(Integer subsidyCount) {
        this.subsidyCount = subsidyCount;
    }

    public Map<Integer, Integer> getSignatureBook() {
        return signatureBook;
    }

    public void setSignatureBook(Map<Integer, Integer> signatureBook) {
        this.signatureBook = signatureBook;
    }

    public Integer getTotalWinCount() {
        return totalWinCount;
    }

    public void setTotalWinCount(Integer totalWinCount) {
        this.totalWinCount = totalWinCount;
    }

    public Integer getTotalWinAward() {
        return totalWinAward;
    }

    public void setTotalWinAward(Integer totalWinAward) {
        this.totalWinAward = totalWinAward;
    }

    public Integer getComboWinCount() {
        return comboWinCount;
    }

    public void setComboWinCount(Integer comboWinCount) {
        this.comboWinCount = comboWinCount;
    }

    public List<Integer> getComboWinAward() {
        return comboWinAward;
    }

    public void setComboWinAward(List<Integer> comboWinAward) {
        this.comboWinAward = comboWinAward;
    }

    public Integer getInviterUserAwardCount() {
        return inviterUserAwardCount;
    }

    public void setInviterUserAwardCount(Integer inviterUserAwardCount) {
        this.inviterUserAwardCount = inviterUserAwardCount;
    }

    public Integer getInviterUserAwardGold() {
        return inviterUserAwardGold;
    }

    public void setInviterUserAwardGold(Integer inviterUserAwardGold) {
        this.inviterUserAwardGold = inviterUserAwardGold;
    }

    public boolean refresh(long userId) {
        Date now = new Date();
        if (TimeUtil.isSameDay(now.getTime(), today.getTime())) {
            return false;
        }
        LogUtil.monitorLog.info("GoldRoomActivity|refresh|" + userId + "|" + TimeUtil.parseTime(today.getTime(), null));
        if (!TimeUtil.isSameWeek(today, now)) {
            for (int i = 1; i <= 7; i++) {
                signatureBook.put(i, 0);
            }
        }
        this.today = now;
        this.subsidyCount = 0;
        this.totalWinCount = 0;
        this.totalWinAward = 0;
        this.comboWinCount = 0;
        this.comboWinAward = new ArrayList<>();
        return true;
    }

    public void parseFromJson(String jsonData, long userId) {
        if (StringUtils.isBlank(jsonData)) {
            LogUtil.monitorLog.info("GoldRoomActivity|parseFromJson|" + userId);
            return;
        }
        GoldRoomActivity temp = JacksonUtil.readValue(jsonData, new TypeReference<GoldRoomActivity>() {
        });
        if (temp != null) {
            this.today = temp.getToday();
            this.subsidyCount = temp.getSubsidyCount();
            this.signatureBook = temp.getSignatureBook();
            this.totalWinCount = temp.getTotalWinCount();
            this.totalWinAward = temp.getTotalWinAward();
            this.comboWinCount = temp.getComboWinCount();
            this.comboWinAward = temp.getComboWinAward();
            this.inviterUserAwardCount = temp.getInviterUserAwardCount();
            this.inviterUserAwardGold = temp.getInviterUserAwardGold();
        }
    }

    public GoldRoomActivityMsg.Builder toProtoMsg() {
        GoldRoomActivityMsg.Builder msg = GoldRoomActivityMsg.newBuilder();
        msg.setSubsidyCount(this.getSubsidyCount());
        for (int i = 1; i <= 7; i++) {
            SignatureBook.Builder signature = SignatureBook.newBuilder();
            signature.setDay(i);
            Integer stat = signatureBook.get(i);
            signature.setRes(stat == null ? 0 : stat);
            msg.addSignatureBook(signature);
        }
        msg.setTotalWinCount(this.getTotalWinCount());
        msg.setTotalWinAward(this.getTotalWinAward());
        msg.setTotalWinAwardCount(this.getTotalWinAwardCount() - this.getTotalWinAward());
        msg.setComboWinCount(this.getComboWinCount());
        msg.addAllComboWinAward(this.getComboWinAward());
        msg.setInviterUserAwardCount(this.getInviterUserAwardCount());
        msg.setInviterUserAwardGold(this.getInviterUserAwardGold());
        return msg;
    }

    public int getTotalWinAwardCount(){
        int totalWinAwardCount = totalWinCount > 8 ? 7 : totalWinCount - 1;
        totalWinAwardCount = totalWinAwardCount < 0 ? 0 : totalWinAwardCount;
        return totalWinAwardCount;
    }
}
