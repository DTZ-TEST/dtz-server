package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

@Table(alias = "weixin_authorization")
public class WXauthorization implements Serializable {

    private static final long serialVersionUID = 1L;

    private String unionId;
    private Integer agencyId;
    private Date createTime;
    private Long inviterId;
    private Date inviterTime;

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public Integer getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Integer agencyId) {
        this.agencyId = agencyId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public Date getInviterTime() {
        return inviterTime;
    }

    public void setInviterTime(Date inviterTime) {
        this.inviterTime = inviterTime;
    }
}
