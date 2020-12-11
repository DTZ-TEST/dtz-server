package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/18.
 */
@Table(alias = "user_inf")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String headimgurl;
    private String name;
    private Integer sex;
    private Integer payBindId;
    private Integer regBindId;
    private Integer enterServer;
    private Long cards;
    private Long freeCards;
    private String ip;
    private Date payBindTime;
    private Date regTime;
    private Date logoutTime;
    private Integer userState;//玩家状态：0禁止登陆，1正常，2红名
    private String identity;
    private Long totalCount;
    private String photo;
    private Long playingTableId;
    private String phoneNum;
    private String phonePw;


    public Long getPlayingTableId() {
		return playingTableId;
	}

	public void setPlayingTableId(Long playingTableId) {
		this.playingTableId = playingTableId;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getEnterServer() {
        return enterServer;
    }

    public void setEnterServer(Integer enterServer) {
        this.enterServer = enterServer;
    }

    public Integer getUserState() {
        return userState;
    }

    public void setUserState(Integer userState) {
        this.userState = userState;
    }

    public Long getFreeCards() {
        return freeCards;
    }

    public void setFreeCards(Long freeCards) {
        this.freeCards = freeCards;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    public Integer getRegBindId() {
        return regBindId;
    }

    public void setRegBindId(Integer regBindId) {
        this.regBindId = regBindId;
    }

    public Long getCards() {
        return cards;
    }

    public void setCards(Long cards) {
        this.cards = cards;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getPayBindId() {
        return payBindId;
    }

    public void setPayBindId(Integer payBindId) {
        this.payBindId = payBindId;
    }

    public Date getPayBindTime() {
        return payBindTime;
    }

    public void setPayBindTime(Date payBindTime) {
        this.payBindTime = payBindTime;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getPhonePw() {
        return phonePw;
    }

    public void setPhonePw(String phonePw) {
        this.phonePw = phonePw;
    }
}
