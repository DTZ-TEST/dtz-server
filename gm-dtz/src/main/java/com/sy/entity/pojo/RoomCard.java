package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/14.
 */
@Table(alias = "roomcard")
public class RoomCard implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;//用户id
    private String userName;//用户名称
    private Integer parentId;//父id
    private Integer agencyId;//代理id
    private Integer commonCard;//普通房卡数
    private Integer freeCard;//免费房卡数
    private Date createTime;//创建时间
    private String remark;//代理商名称
    private String agencyPhone;//手机
    private String agencyQQ;//QQ号
    private String agencyWechat;//微信
    private String agencyEmail;//邮箱
    private String bankName;//开户银行
    private String bankCard;//银行卡号
    private String agencyComment;//备注
    private Integer partAdmin;//分admin标识
    private Date updateTime;//最后修改时间
    private Integer agencyLevel;//代理等级
    private String openid;//openid
    private Integer vip;//vip等级

    public Integer getVip() {
        return vip;
    }

    public void setVip(Integer vip) {
        this.vip = vip;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Integer getAgencyLevel() {
        return agencyLevel;
    }

    public void setAgencyLevel(Integer agencyLevel) {
        this.agencyLevel = agencyLevel;
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Integer agencyId) {
        this.agencyId = agencyId;
    }

    public Integer getCommonCard() {
        return commonCard;
    }

    public void setCommonCard(Integer commonCard) {
        this.commonCard = commonCard;
    }

    public Integer getFreeCard() {
        return freeCard;
    }

    public void setFreeCard(Integer freeCard) {
        this.freeCard = freeCard;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAgencyPhone() {
        return agencyPhone;
    }

    public void setAgencyPhone(String agencyPhone) {
        this.agencyPhone = agencyPhone;
    }

    public String getAgencyQQ() {
        return agencyQQ;
    }

    public void setAgencyQQ(String agencyQQ) {
        this.agencyQQ = agencyQQ;
    }

    public String getAgencyWechat() {
        return agencyWechat;
    }

    public void setAgencyWechat(String agencyWechat) {
        this.agencyWechat = agencyWechat;
    }

    public String getAgencyEmail() {
        return agencyEmail;
    }

    public void setAgencyEmail(String agencyEmail) {
        this.agencyEmail = agencyEmail;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getAgencyComment() {
        return agencyComment;
    }

    public void setAgencyComment(String agencyComment) {
        this.agencyComment = agencyComment;
    }

    public Integer getPartAdmin() {
        return partAdmin;
    }

    public void setPartAdmin(Integer partAdmin) {
        this.partAdmin = partAdmin;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
