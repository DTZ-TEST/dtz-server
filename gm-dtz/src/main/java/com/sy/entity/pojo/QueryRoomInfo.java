package com.sy.entity.pojo;

import java.util.List;

public class QueryRoomInfo extends Room{

	private List<UserInfo> userList;

	public List<UserInfo> getUserList() {
		return userList;
	}

	public void setUserList(List<UserInfo> userList) {
		this.userList = userList;
	}
	
	
}
