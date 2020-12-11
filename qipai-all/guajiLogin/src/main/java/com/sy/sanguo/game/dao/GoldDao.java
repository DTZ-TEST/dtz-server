package com.sy.sanguo.game.dao;

import com.sy.sanguo.game.bean.GoldUserInfo;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import java.util.*;

public class GoldDao extends BaseDao{
    private static GoldDao _inst = new GoldDao();

    public static GoldDao getInstance() {
        return _inst;
    }

    public  GoldUserInfo selectGoldUserByUserId(long userId) throws Exception{
        Object o = this.getSql().queryForObject("gold.selectGoldUserByUserId", userId);
        if (o != null) {
            return (GoldUserInfo) o;
        }
        return null;
    }

    public void insertGoldUser(GoldUserInfo userInfo) throws Exception{
        this.getSql().insert("gold.insertGoldUser", userInfo);
    }

    public int updateGoldUser(Map<String, Object> map) throws Exception {
        return this.getSql().update("gold.updateGoldUser", map);
    }


}
