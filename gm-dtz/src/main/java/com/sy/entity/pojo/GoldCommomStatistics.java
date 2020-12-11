package com.sy.entity.pojo;

import java.io.Serializable;

import com.sy.mainland.util.db.annotation.Table;

/**
 * Created by pc on 2017/5/10.
 */
@Table(alias = "gold_commom_statistics")
public class GoldCommomStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private     Integer dateTime;
    private     Integer  todaylc ;
    private 	Integer  twodaylc ;
    private 	Integer  threedaylc ;
    private 	Integer  fourdaylc;
    private 	Integer  fivedaylc ;
    private 	Integer  sixdaylc ;
    private 	Integer  sevendaylc ;
    private 	Integer  fifteendaylc;
    private 	Integer  monthdaylc ;
	private     Integer  totalUser;
	
	private     Integer  dau;
	private     Integer  addUser;
	private     Integer  cjTotal;
	private     Integer  zjTotal;
	private     Integer  gjTotal;
	
	private     Integer  totalNums;

	private     Integer  cjPdkTotal;
	private     Integer  zjPdkTotal;
	private     Integer  gjPdkTotal;

	private     Integer  totalPdkNums;
	
	private     Integer  cjphzTotal;
	private     Integer  zjphzTotal;
	private     Integer  gjphzTotal;

	private     Integer  totalphzNums;
	

	public Integer getCjphzTotal() {
		return cjphzTotal;
	}

	public void setCjphzTotal(Integer cjphzTotal) {
		this.cjphzTotal = cjphzTotal;
	}

	public Integer getZjphzTotal() {
		return zjphzTotal;
	}

	public void setZjphzTotal(Integer zjphzTotal) {
		this.zjphzTotal = zjphzTotal;
	}

	public Integer getGjphzTotal() {
		return gjphzTotal;
	}

	public void setGjphzTotal(Integer gjphzTotal) {
		this.gjphzTotal = gjphzTotal;
	}

	public Integer getTotalphzNums() {
		return totalphzNums;
	}

	public void setTotalphzNums(Integer totalphzNums) {
		this.totalphzNums = totalphzNums;
	}

	public Integer getCjPdkTotal() {
		return cjPdkTotal;
	}

	public void setCjPdkTotal(Integer cjPdkTotal) {
		this.cjPdkTotal = cjPdkTotal;
	}

	public Integer getZjPdkTotal() {
		return zjPdkTotal;
	}

	public void setZjPdkTotal(Integer zjPdkTotal) {
		this.zjPdkTotal = zjPdkTotal;
	}

	public Integer getGjPdkTotal() {
		return gjPdkTotal;
	}

	public void setGjPdkTotal(Integer gjPdkTotal) {
		this.gjPdkTotal = gjPdkTotal;
	}

	public Integer getTotalPdkNums() {
		return totalPdkNums;
	}

	public void setTotalPdkNums(Integer totalPdkNums) {
		this.totalPdkNums = totalPdkNums;
	}

	public Integer getDateTime() {
		return dateTime;
	}

	public void setDateTime(Integer dateTime) {
		this.dateTime = dateTime;
	}

	public Integer getTodaylc() {
		return todaylc;
	}

	public void setTodaylc(Integer todaylc) {
		this.todaylc = todaylc;
	}

	public Integer getTwodaylc() {
		return twodaylc;
	}

	public void setTwodaylc(Integer twodaylc) {
		this.twodaylc = twodaylc;
	}

	public Integer getThreedaylc() {
		return threedaylc;
	}

	public void setThreedaylc(Integer threedaylc) {
		this.threedaylc = threedaylc;
	}

	public Integer getFourdaylc() {
		return fourdaylc;
	}

	public void setFourdaylc(Integer fourdaylc) {
		this.fourdaylc = fourdaylc;
	}

	public Integer getFivedaylc() {
		return fivedaylc;
	}

	public void setFivedaylc(Integer fivedaylc) {
		this.fivedaylc = fivedaylc;
	}

	public Integer getSixdaylc() {
		return sixdaylc;
	}

	public void setSixdaylc(Integer sixdaylc) {
		this.sixdaylc = sixdaylc;
	}

	public Integer getSevendaylc() {
		return sevendaylc;
	}

	public void setSevendaylc(Integer sevendaylc) {
		this.sevendaylc = sevendaylc;
	}

	public Integer getFifteendaylc() {
		return fifteendaylc;
	}

	public void setFifteendaylc(Integer fifteendaylc) {
		this.fifteendaylc = fifteendaylc;
	}

	public Integer getMonthdaylc() {
		return monthdaylc;
	}

	public void setMonthdaylc(Integer monthdaylc) {
		this.monthdaylc = monthdaylc;
	}

	public Integer getTotalUser() {
		return totalUser;
	}

	public void setTotalUser(Integer totalUser) {
		this.totalUser = totalUser;
	}

	public Integer getDau() {
		return dau;
	}

	public void setDau(Integer dau) {
		this.dau = dau;
	}

	public Integer getAddUser() {
		return addUser;
	}

	public void setAddUser(Integer addUser) {
		this.addUser = addUser;
	}

	public Integer getCjTotal() {
		return cjTotal;
	}

	public void setCjTotal(Integer cjTotal) {
		this.cjTotal = cjTotal;
	}

	public Integer getZjTotal() {
		return zjTotal;
	}

	public void setZjTotal(Integer zjTotal) {
		this.zjTotal = zjTotal;
	}

	public Integer getGjTotal() {
		return gjTotal;
	}

	public void setGjTotal(Integer gjTotal) {
		this.gjTotal = gjTotal;
	}

	public Integer getTotalNums() {
		return totalNums;
	}

	public void setTotalNums(Integer totalNums) {
		this.totalNums = totalNums;
	}
   
}
