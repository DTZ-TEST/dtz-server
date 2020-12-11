package com.sy599.game.staticdata.model;

import java.util.List;
import java.util.Random;

public class NewPlayerGiftActivityBean
{
    private int liveness;  //领取奖励所需活跃度
    private int rewardType;  //奖励类型 1钻石 2现金红包,3随机奖励
    private int reward;   //奖励额
    private int rndStart; // 奖励:随机起始
    private int rndEnd; // 奖励：随机结束
    private int totalRate; // 总概率值
    private int rate;      // 单个概率

    private List<NewPlayerGiftActivityBean> rndRewards;

    public int getLiveness() {
        return liveness;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
    }

    public int getRewardType() {
        return rewardType;
    }

    public void setRewardType(int rewardType) {
        this.rewardType = rewardType;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public List<NewPlayerGiftActivityBean> getRndRewards() {
        return rndRewards;
    }

    public void setRndRewards(List<NewPlayerGiftActivityBean> rndRewards) {
        this.rndRewards = rndRewards;
    }

    public int getRndStart() {
        return rndStart;
    }

    public void setRndStart(int rndStart) {
        this.rndStart = rndStart;
    }

    public int getRndEnd() {
        return rndEnd;
    }

    public void setRndEnd(int rndEnd) {
        this.rndEnd = rndEnd;
    }

    public int getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(int totalRate) {
        this.totalRate = totalRate;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public NewPlayerGiftActivityBean getRndReward(){
        if(rndRewards == null || rndRewards.size() == 0 ||totalRate <= 0){
            return null;
        }
        int rnd = new Random().nextInt(totalRate);
        int addRate = 0 ;
        NewPlayerGiftActivityBean res = null;
        for(NewPlayerGiftActivityBean rndReward  : rndRewards){
            if(rnd >= addRate && rnd < addRate + rndReward.getRate()){
                res = rndReward;
                break;
            }else{
                addRate += rndReward.getRate();
            }
        }
        return res;
    }
}
