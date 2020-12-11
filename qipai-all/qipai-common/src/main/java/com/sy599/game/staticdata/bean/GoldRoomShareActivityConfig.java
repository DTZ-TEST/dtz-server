package com.sy599.game.staticdata.bean;

import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 欢乐金币场局数奖励活动
 */
public class GoldRoomShareActivityConfig extends ActivityConfigInfo {

    private static final String name = "欢乐金币场局数奖励活动";
    private int finishCount = Integer.MAX_VALUE;//每天完成的金币场次
    private int totalRate = 0; //钻石奖励所有概率和
    private List<RewardData> rewardList = new ArrayList<>(); //随机钻石奖励列表
    private int totalRateRedBag = 0; //现金奖励所有概率和
    private List<RewardData> rewardListRedBag = new ArrayList<>();//随机现金奖励列表


    @Override
    public void configParamsAndRewards() {
        if (StringUtils.isBlank(params)) {
            LogUtil.errorLog.info(name + "配置异常:params[" + params + "]");
            return;
        }
        this.finishCount = Integer.valueOf(params);
        String[] split = rewards.split("#");
        String goldRewards = split[0];
        String[] strArray = goldRewards.split("[|]");
        if (strArray == null || strArray.length == 0) {
            LogUtil.errorLog.info(name + "配置异常:reward[" + goldRewards + "]");
            return;
        }
        for (String str : strArray) {
            if (StringUtils.isBlank(str)) {
                LogUtil.errorLog.info(name + "配置异常:reward[" + goldRewards + "]");
                continue;
            }
            String[] splits = str.split(",");
            if (splits.length != 3) {
                LogUtil.errorLog.info(name + "配置异常:reward[" + goldRewards + "]");
                continue;
            }
            int rate = Integer.valueOf(splits[0]);
            int startValue = Integer.valueOf(splits[1]);
            int endValue = Integer.valueOf(splits[2]) + 1;

            if (rate <= 0 || startValue < 0 || endValue <= 0) {
                LogUtil.errorLog.info(name + "配置异常:reward[" + goldRewards + "]");
                continue;
            }

            totalRate += rate;
            rewardList.add(new RewardData(rate, startValue, endValue));
        }

        if(split.length > 1){
            String redBagRewards = split[1];
            strArray = redBagRewards.split("[|]");
            if (strArray == null || strArray.length == 0) {
                LogUtil.errorLog.info(name + "配置异常:reward[" + redBagRewards + "]");
                return;
            }
            for (String str : strArray) {
                if (StringUtils.isBlank(str)) {
                    LogUtil.errorLog.info(name + "配置异常:reward[" + redBagRewards + "]");
                    continue;
                }
                String[] splits = str.split(",");
                if (splits.length != 3) {
                    LogUtil.errorLog.info(name + "配置异常:reward[" + redBagRewards + "]");
                    continue;
                }
                int rate = Integer.valueOf(splits[0]);
                int startValue = Integer.valueOf(splits[1]);
                int endValue = Integer.valueOf(splits[2]);

                if (rate <= 0 || startValue < 0 || endValue < 0) {
                    LogUtil.errorLog.info(name + "配置异常:reward[" + redBagRewards + "]");
                    continue;
                }

                totalRateRedBag += rate;
                rewardListRedBag.add(new RewardData(rate, startValue, endValue));
            }
        }
    }

    public int calcReward() {
        Random rnd = new Random();
        int r = rnd.nextInt(totalRate);
        int total = 0;
        RewardData reward = null;
        for (RewardData data : rewardList) {
            if (r >= total && r < total + data.getRate()) {
                reward = data;
                break;
            } else {
                total += data.getRate();
            }
        }
        int res = 0;
        if (reward != null && reward.getEndValue() - reward.getStartValue() > 0) {
            res = reward.getStartValue() + rnd.nextInt(reward.getEndValue() - reward.getStartValue());
        }
        return res;
    }

    public int calcRewardRedBag() {
        Random rnd = new Random();
        int r = rnd.nextInt(totalRateRedBag);
        int total = 0;
        RewardData reward = null;
        for (RewardData data : rewardListRedBag) {
            if (r >= total && r < total + data.getRate()) {
                reward = data;
                break;
            } else {
                total += data.getRate();
            }
        }
        int res = 0;
        if (reward != null) {
            res = reward.getStartValue() ;
            if(reward.getEndValue() - reward.getStartValue() > 0){
                res += rnd.nextInt(reward.getEndValue() - reward.getStartValue());
            }
        }
        return res;
    }

    public int getFinishCount() {
        return finishCount;
    }

    public class RewardData {
        int rate; // 概率
        int startValue;// 起始值
        int endValue;  // 结束值

        public RewardData(int rate, int startValue, int endValue) {
            this.rate = rate;
            this.startValue = startValue;
            this.endValue = endValue;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        public int getStartValue() {
            return startValue;
        }

        public void setStartValue(int startValue) {
            this.startValue = startValue;
        }

        public int getEndValue() {
            return endValue;
        }

        public void setEndValue(int endValue) {
            this.endValue = endValue;
        }
    }
}
