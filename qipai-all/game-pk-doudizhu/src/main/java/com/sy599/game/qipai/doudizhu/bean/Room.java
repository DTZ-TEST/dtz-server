package com.sy599.game.qipai.doudizhu.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private long id;
	private Map<Long, DdzTable> tableMap = new ConcurrentHashMap<Long, DdzTable>();

	public Room(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public DdzTable getTable(long id) {
		return tableMap.get(id);
	}
	
}
