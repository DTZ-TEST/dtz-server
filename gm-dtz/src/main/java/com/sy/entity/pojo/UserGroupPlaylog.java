package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

@Table(alias = "user_group_playlog")
public class UserGroupPlaylog implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
	private Integer tableid;
	private Integer userid;
	private Integer count;
	private String creattime;
	private String players;
	private String score;
	private String overtime;
	private Integer playercount;
	private Integer groupid;
	private String gamename;
	private Integer totalCount;
	private String diFen;
	private String diFenScore;
	
	
	public String getDiFen() {
		return diFen;
	}
	public void setDiFen(String diFen) {
		this.diFen = diFen;
	}
	public String getDiFenScore() {
		return diFenScore;
	}
	public void setDiFenScore(String diFenScore) {
		this.diFenScore = diFenScore;
	}
	public Integer getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Integer getTableid() {
		return tableid;
	}
	public void setTableid(Integer tableid) {
		this.tableid = tableid;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getCreattime() {
		return creattime;
	}
	public void setCreattime(String creattime) {
		this.creattime = creattime;
	}
	public String getPlayers() {
		return players;
	}
	public void setPlayers(String players) {
		this.players = players;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getOvertime() {
		return overtime;
	}
	public void setOvertime(String overtime) {
		this.overtime = overtime;
	}
	public Integer getPlayercount() {
		return playercount;
	}
	public void setPlayercount(Integer playercount) {
		this.playercount = playercount;
	}
	public Integer getGroupid() {
		return groupid;
	}
	public void setGroupid(Integer groupid) {
		this.groupid = groupid;
	}
	public String getGamename() {
		return gamename;
	}
	public void setGamename(String gamename) {
		this.gamename = gamename;
	}
	
	
	

}
