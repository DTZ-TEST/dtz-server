package com.sy599.game.db.dao;

import com.sy599.game.db.bean.ResourcesConfigs;

import java.util.List;

public class ResourcesConfigsDao extends BaseDao {

	private static ResourcesConfigsDao resourcesConfigsDao = new ResourcesConfigsDao();
	
	public static ResourcesConfigsDao getInstance(){
		return resourcesConfigsDao;
	}
	
	public List<ResourcesConfigs> loadAllConfigs() throws Exception{
		return (List<ResourcesConfigs>)this.getSqlLoginClient().queryForList("resources_configs.load_all_configs");
	}

}
