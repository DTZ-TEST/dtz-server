package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Column;
import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/5/10.
 */
@Table(alias = "statistics_platform")
public class StatisticData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer dateTime;
    private String pf;
    private Integer regTotalCount;
    private Integer active1day;
    private Integer active7day;
    private Integer active30day;
    private Integer paySum;
    private Integer payCount;
    private Float payRate;
    @Column(alias = "ARPU")
    private Float arpu;
    @Column(alias = "LTV")
    private Float ltv;
    private Integer regTotalAlive1day;
    private Integer regTotalAlive2day;
    private Integer regTotalAlive3day;
    private Integer regTotalAlive4day;
    private Integer regTotalAlive5day;
    private Integer regTotalAlive6day;
    private Integer regTotalAlive7day;
    private Integer regCount;
    private String extend;
    private Integer regTotalAlive14day;
    private Integer regTotalAlive30day;
    private Integer firstCount;
    private Integer firstSum;
    private Integer firstRegPayCount;
    private Integer firstRegPaySum;
    private Integer pfPaySum;//代理后台充值总额

    public Integer getDateTime() {
        return dateTime;
    }

    public void setDateTime(Integer dateTime) {
        this.dateTime = dateTime;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public Integer getRegTotalCount() {
        return regTotalCount;
    }

    public void setRegTotalCount(Integer regTotalCount) {
        this.regTotalCount = regTotalCount;
    }

    public Integer getActive1day() {
        return active1day;
    }

    public void setActive1day(Integer active1day) {
        this.active1day = active1day;
    }

    public Integer getActive7day() {
        return active7day;
    }

    public void setActive7day(Integer active7day) {
        this.active7day = active7day;
    }

    public Integer getActive30day() {
        return active30day;
    }

    public void setActive30day(Integer active30day) {
        this.active30day = active30day;
    }

    public Integer getPaySum() {
        return paySum;
    }

    public void setPaySum(Integer paySum) {
        this.paySum = paySum;
    }

    public Integer getPayCount() {
        return payCount;
    }

    public void setPayCount(Integer payCount) {
        this.payCount = payCount;
    }

    public Float getPayRate() {
        return payRate;
    }

    public void setPayRate(Float payRate) {
        this.payRate = payRate;
    }

    public Float getArpu() {
        return arpu;
    }

    public void setArpu(Float arpu) {
        this.arpu = arpu;
    }

    public Float getLtv() {
        return ltv;
    }

    public void setLtv(Float ltv) {
        this.ltv = ltv;
    }

    public Integer getRegTotalAlive1day() {
        return regTotalAlive1day;
    }

    public void setRegTotalAlive1day(Integer regTotalAlive1day) {
        this.regTotalAlive1day = regTotalAlive1day;
    }

    public Integer getRegTotalAlive2day() {
        return regTotalAlive2day;
    }

    public void setRegTotalAlive2day(Integer regTotalAlive2day) {
        this.regTotalAlive2day = regTotalAlive2day;
    }

    public Integer getRegTotalAlive3day() {
        return regTotalAlive3day;
    }

    public void setRegTotalAlive3day(Integer regTotalAlive3day) {
        this.regTotalAlive3day = regTotalAlive3day;
    }

    public Integer getRegTotalAlive4day() {
        return regTotalAlive4day;
    }

    public void setRegTotalAlive4day(Integer regTotalAlive4day) {
        this.regTotalAlive4day = regTotalAlive4day;
    }

    public Integer getRegTotalAlive5day() {
        return regTotalAlive5day;
    }

    public void setRegTotalAlive5day(Integer regTotalAlive5day) {
        this.regTotalAlive5day = regTotalAlive5day;
    }

    public Integer getRegTotalAlive6day() {
        return regTotalAlive6day;
    }

    public void setRegTotalAlive6day(Integer regTotalAlive6day) {
        this.regTotalAlive6day = regTotalAlive6day;
    }

    public Integer getRegTotalAlive7day() {
        return regTotalAlive7day;
    }

    public void setRegTotalAlive7day(Integer regTotalAlive7day) {
        this.regTotalAlive7day = regTotalAlive7day;
    }

    public Integer getRegCount() {
        return regCount;
    }

    public void setRegCount(Integer regCount) {
        this.regCount = regCount;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public Integer getRegTotalAlive14day() {
        return regTotalAlive14day;
    }

    public void setRegTotalAlive14day(Integer regTotalAlive14day) {
        this.regTotalAlive14day = regTotalAlive14day;
    }

    public Integer getRegTotalAlive30day() {
        return regTotalAlive30day;
    }

    public void setRegTotalAlive30day(Integer regTotalAlive30day) {
        this.regTotalAlive30day = regTotalAlive30day;
    }

    public Integer getFirstCount() {
        return firstCount;
    }

    public void setFirstCount(Integer firstCount) {
        this.firstCount = firstCount;
    }

    public Integer getFirstSum() {
        return firstSum;
    }

    public void setFirstSum(Integer firstSum) {
        this.firstSum = firstSum;
    }

    public Integer getFirstRegPayCount() {
        return firstRegPayCount;
    }

    public void setFirstRegPayCount(Integer firstRegPayCount) {
        this.firstRegPayCount = firstRegPayCount;
    }

    public Integer getFirstRegPaySum() {
        return firstRegPaySum;
    }

    public void setFirstRegPaySum(Integer firstRegPaySum) {
        this.firstRegPaySum = firstRegPaySum;
    }

	public Integer getPfPaySum() {
		return pfPaySum;
	}

	public void setPfPaySum(Integer pfPaySum) {
		this.pfPaySum = pfPaySum;
	}
    
    
}
