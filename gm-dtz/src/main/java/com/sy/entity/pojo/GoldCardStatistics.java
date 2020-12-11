package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by pc on 2017/4/18.
 */
@Table(alias = "gold_card_statistics")
public class GoldCardStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer dateTime;
    private Long totalGold;

    private Long totalService;
    private Long cjGold;
    private Long zjGold;
    private Long gjGold;

	private Long totalPdkService;
	private Long cjPdkGold;
	private Long zjPdkGold;
	private Long gjPdkGold;

	private Long  cjphzGold;
	private Long  zjphzGold;
	private Long  gjphzGold;
	private Long  totalphzService;
	
    private String exchargeGold;
    private String exchargeCard;
    private Long cardce;
	public Integer getDateTime() {
		return dateTime;
	}
	public void setDateTime(Integer dateTime) {
		this.dateTime = dateTime;
	}
	public Long getTotalGold() {
		return totalGold;
	}
	public void setTotalGold(Long totalGold) {
		this.totalGold = totalGold;
	}
	public Long getTotalService() {
		return totalService;
	}
	public void setTotalService(Long totalService) {
		this.totalService = totalService;
	}
	public Long getCjGold() {
		return cjGold;
	}
	public void setCjGold(Long cjGold) {
		this.cjGold = cjGold;
	}
	public Long getZjGold() {
		return zjGold;
	}
	public void setZjGold(Long zjGold) {
		this.zjGold = zjGold;
	}
	public Long getGjGold() {
		return gjGold;
	}
	public void setGjGold(Long gjGold) {
		this.gjGold = gjGold;
	}
	public Long getTotalPdkService() {
		return totalPdkService;
	}
	public void setTotalPdkService(Long totalPdkService) {
		this.totalPdkService = totalPdkService;
	}
	public Long getCjPdkGold() {
		return cjPdkGold;
	}
	public void setCjPdkGold(Long cjPdkGold) {
		this.cjPdkGold = cjPdkGold;
	}
	public Long getZjPdkGold() {
		return zjPdkGold;
	}
	public void setZjPdkGold(Long zjPdkGold) {
		this.zjPdkGold = zjPdkGold;
	}
	public Long getGjPdkGold() {
		return gjPdkGold;
	}
	public void setGjPdkGold(Long gjPdkGold) {
		this.gjPdkGold = gjPdkGold;
	}
	public Long getCjphzGold() {
		return cjphzGold;
	}
	public void setCjphzGold(Long cjphzGold) {
		this.cjphzGold = cjphzGold;
	}
	public Long getZjphzGold() {
		return zjphzGold;
	}
	public void setZjphzGold(Long zjphzGold) {
		this.zjphzGold = zjphzGold;
	}
	public Long getGjphzGold() {
		return gjphzGold;
	}
	public void setGjphzGold(Long gjphzGold) {
		this.gjphzGold = gjphzGold;
	}
	public Long getTotalphzService() {
		return totalphzService;
	}
	public void setTotalphzService(Long totalphzService) {
		this.totalphzService = totalphzService;
	}
	public String getExchargeGold() {
		return exchargeGold;
	}
	public void setExchargeGold(String exchargeGold) {
		this.exchargeGold = exchargeGold;
	}
	public String getExchargeCard() {
		return exchargeCard;
	}
	public void setExchargeCard(String exchargeCard) {
		this.exchargeCard = exchargeCard;
	}
	public Long getCardce() {
		return cardce;
	}
	public void setCardce(Long cardce) {
		this.cardce = cardce;
	}
	

    
    
	
    
}
