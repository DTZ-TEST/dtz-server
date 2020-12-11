package com.sy599.game.qipai.bbtz.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private long id;
	private Map<Long, BbtzTable> tableMap = new ConcurrentHashMap<Long, BbtzTable>();

	public Room(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public BbtzTable getTable(long id) {
		return tableMap.get(id);
	}
	
}
