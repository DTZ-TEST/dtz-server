package com.sy.entity.pojo;

import java.sql.Timestamp;

/**
 * Created by 35829 on 2017/6/5.
 */
public class Lottery {
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Timestamp getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Timestamp create_time) {
        this.create_time = create_time;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private long userId;
    private Timestamp create_time;
    private String prize;
    private String nickname;



}
