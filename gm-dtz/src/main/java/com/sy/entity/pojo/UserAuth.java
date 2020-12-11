package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/18.
 */
@Table( alias = "t_auth")
public class UserAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long userId;
    private Long authId;
    private String currentState;
    private Date createdTime;
    private Date modifiedTime;

    public UserAuth() {
    }

    public UserAuth(Long userId, Long authId) {
        this.userId = userId;
        this.authId = authId;
        currentState="1";
        createdTime = new Date();
        modifiedTime = new Date();
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAuthId() {
        return authId;
    }

    public void setAuthId(Long authId) {
        this.authId = authId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
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
}
