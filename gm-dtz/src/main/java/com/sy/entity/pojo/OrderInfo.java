package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Column;
import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pc on 2017/4/14.
 */
@Table(alias = "order_info")
public class OrderInfo  implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;//主键
    @Column(alias = "flat_id")
    private String flatId;//玩家平台UID
    @Column(alias = "order_id")
    private String orderId;//平台生成的订单ID
    @Column(alias = "server_id")
    private String serverId;//服务器ID
    @Column(alias = "order_amount")
    private String orderAmount;//充值金额
    @Column(alias = "is_sent")
    private Integer isSent;//0 未发货 1已发货
    @Column(alias = "item_id")
    private Integer itemId;//道具ID
    @Column(alias = "item_num")
    private Integer itemNum;//道具数量
    private String platform;//平台
    @Column(alias = "sell_time")
    private Date sellTime;//收货时间
    @Column(alias = "create_time")
    private Date createTime;//
    private String extend;//拓展
    private Long userId;//玩家Id
    private String payType;//充值type
    private String name;
    private String payPf;
    private String delegateAgency;//帮我代充的人
    private BigDecimal payMoney;//元
    public String getPayPf() {
		return payPf;
	}

	public void setPayPf(String payPf) {
		this.payPf = payPf;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(BigDecimal payMoney) {
        this.payMoney = payMoney;
    }

    public String getDelegateAgency() {
        return delegateAgency;
    }

    public void setDelegateAgency(String delegateAgency) {
        this.delegateAgency = delegateAgency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlatId() {
        return flatId;
    }

    public void setFlatId(String flatId) {
        this.flatId = flatId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Integer getIsSent() {
        return isSent;
    }

    public void setIsSent(Integer isSent) {
        this.isSent = isSent;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getItemNum() {
        return itemNum;
    }

    public void setItemNum(Integer itemNum) {
        this.itemNum = itemNum;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Date getSellTime() {
        return sellTime;
    }

    public void setSellTime(Date sellTime) {
        this.sellTime = sellTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }
}
