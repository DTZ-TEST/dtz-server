package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Column;
import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/14.
 */
@Table(alias = "system_user")
public class SystemUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(alias = "user_id")
    private Integer userId;//用户id
    @Column(alias = "isHavePurchase")
    private Integer isHavePurchase;//是否有购卡权限
    @Column(alias = "user_name")
    private String userName;//用户名称
    @Column(alias = "user_pwd")
    private String userPwd;//用户密码
    @Column(alias = "user_session")
    private String userSession;//用户会话
    @Column(alias = "role_id")
    private Integer roleId;//角色
    private Integer isForbidden;//是否禁止登录
    @Column(alias = "pay_pwd")
    private String payPwd;//财务充值密码
    @Column(alias = "inviter_id")
    private String inviterId;//邀请人ID
    @Column(alias = "create_time")
    private Date createdTime;
    @Column(alias = "modify_time")
    private Date modifiedTime;
    @Column(alias = "user_tel")
    private String userTel;

    private String pf;//平台

    public String getUserTel() {
        return userTel;
    }

    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserSession() {
        return userSession;
    }

    public void setUserSession(String userSession) {
        this.userSession = userSession;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getIsForbidden() {
        return isForbidden;
    }

    public void setIsForbidden(Integer isForbidden) {
        this.isForbidden = isForbidden;
    }

    public String getPayPwd() {
        return payPwd;
    }

    public void setPayPwd(String payPwd) {
        this.payPwd = payPwd;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

	public Integer getIsHavePurchase() {
		return isHavePurchase;
	}

	public void setIsHavePurchase(Integer isHavePurchase) {
		this.isHavePurchase = isHavePurchase;
	}
    
    
}