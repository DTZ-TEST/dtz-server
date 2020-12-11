package com.sy.service.impl;

import com.sy.dao.CommonDao;
import com.sy.mainland.util.SqlUtil;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.mainland.util.db.annotation.Table;
import com.sy.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseServiceImpl implements BaseService {

    @Autowired
    protected CommonDao commonDao;

    @Override
    public <T> long saveAndGetKey(T t) {
        Object[] strs = SqlUtil.getInsertSql(t, null);
        return commonDao.saveAndGetKey(strs[0].toString(), (Object[]) strs[1]);
    }

    @Override
    public <T> long saveAndGetKey(String tableName, T t) {
        Object[] strs = SqlUtil.getInsertSql(t, tableName);
        return commonDao.saveAndGetKey(strs[0].toString(), (Object[]) strs[1]);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findList(String tableName, T t) throws Exception {
        Object[] strs = SqlUtil.getSelectSql(t, tableName, "*", "");
        return commonDao.findList(strs[0].toString(), (Object[]) strs[1],
                (Class<T>) t.getClass());
    }

    @Override
    public <T> List<T> findList(T t) throws Exception {
        Table table = t.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findList(table.alias(), t);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T findOne(String tableName, T t) throws Exception {
        Object[] strs = SqlUtil.getSelectSql(t, tableName, "*", "");
        return commonDao.findOne(new StringBuilder()
                        .append(strs[0].toString()).append(" limit 1").toString(),
                (Object[]) strs[1], (Class<T>) t.getClass());
    }

    @Override
    public <T> T findOne(T t) throws Exception {
        Table table = t.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findOne(table.alias(), t);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public <T> List<T> findList(String tableName, Class<T> cls, String[] keys,
                                Object[] values) throws Exception {
        if (keys == null || values == null || keys.length != values.length
                || keys.length == 0) {
            return null;
        }
        if (StringUtils.isBlank(tableName)) {
            tableName = cls.getSimpleName();
        }
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select * from ").append(tableName).append(
                " where 1=1");
        for (int i = 0, len = keys.length; i < len; i++) {
            strBuilder.append(" and ").append(keys[i]).append("=?");
        }
        return commonDao.findList(strBuilder.toString(), values, cls);
    }

    @Override
    public <T> List<T> findList(Class<T> cls, String[] keys, Object[] values) throws Exception {
        Table table = cls.getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findList(table.alias(), cls, keys, values);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public <T> T findOne(String tableName, Class<T> cls, String[] keys,
                         Object[] values) throws Exception {
        if (keys == null || values == null || keys.length != values.length
                || keys.length == 0) {
            return null;
        }
        StringBuilder strBuilder = new StringBuilder();
        if (StringUtils.isBlank(tableName)) {
            tableName = cls.getSimpleName();
        }
        strBuilder.append("select * from ").append(tableName).append(
                " where 1=1");
        for (int i = 0, len = keys.length; i < len; i++) {
            strBuilder.append(" and ").append(keys[i]).append("=?");
        }
        strBuilder.append(" limit 1");
        return commonDao.findOne(strBuilder.toString(), values, cls);
    }

    @Override
    public <T> T findOne(Class<T> cls, String[] keys, Object[] values) throws Exception {
        Table table = cls.getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findOne(table.alias(), cls, keys, values);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public int save(String tableName, Object obj) throws Exception {
        Object[] strs = SqlUtil.getInsertSql(obj, tableName);
        return commonDao.saveOrUpdate(strs[0].toString(), (Object[]) strs[1]);
    }

    @Override
    public int save(Object obj) throws Exception {
        Table table = obj.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return save(table.alias(), obj);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public int update(String tableName, Object obj, String[] keys,
                      Object[] values) throws Exception {
        if (keys == null || values == null || keys.length != values.length
                || keys.length == 0) {
            return 0;
        }
        int len = keys.length;
        HashMap<String, Object> map = new HashMap<String, Object>(len);
        for (int i = 0; i < len; i++) {
            map.put(keys[i], values[i].toString());
        }
        Object[] strs = SqlUtil.getUpdateSql(obj, tableName, map);
        return commonDao.saveOrUpdate(strs[0].toString(), (Object[]) strs[1]);
    }

    @Override
    public int update(Object obj, String[] keys, Object[] values) throws Exception {
        Table table = obj.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return update(table.alias(), obj, keys, values);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    @Override
    public int saveOrUpdate(String sql, Object[] params) {
        return commonDao.saveOrUpdate(sql, params);
    }

    @Override
    public List<Map<String, Object>> find(String sql, Object[] objs)
            throws SQLException {
        // TODO Auto-generated method stub
        return commonDao.find(sql, objs);
    }

    @Override
    public int count(String sql, Object[] obs) throws SQLException {
        return commonDao.count(sql, obs);
    }

    @Override
    public <T> List<T> findList(String sql, Object[] params, Class<T> classType) throws SQLException {
        return commonDao.findList(sql,params,classType);
    }

    @Override
    public <T> T findOne(String sql, Object[] params, Class<T> classType) throws SQLException {
        return commonDao.findOne(sql,params,classType);
    }

    @Override
    public long updateLast(Object obj, String tableName, String keyColumns) {
        return commonDao.updateLast(obj,tableName,keyColumns);
    }

    @Override
    public <T> long saveAndGetKey(SelectDataBase.DataBase dataBase,T t) {
        Object[] strs = SqlUtil.getInsertSql(t, null);
        return commonDao.saveAndGetKey(strs[0].toString(), (Object[]) strs[1]);
    }

    @Override
    public <T> long saveAndGetKey(SelectDataBase.DataBase dataBase,String tableName, T t) {
        Object[] strs = SqlUtil.getInsertSql(t, tableName);
        return commonDao.saveAndGetKey(strs[0].toString(), (Object[]) strs[1]);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,String tableName, T t) throws Exception {
        Object[] strs = SqlUtil.getSelectSql(t, tableName, "*", "");
        return commonDao.findList(strs[0].toString(), (Object[]) strs[1],
                (Class<T>) t.getClass());
    }

    @Override
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,T t) throws Exception {
        Table table = t.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findList(dataBase,table.alias(), t);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T findOne(SelectDataBase.DataBase dataBase,String tableName, T t) throws Exception {
        Object[] strs = SqlUtil.getSelectSql(t, tableName, "*", "");
        return commonDao.findOne(new StringBuilder()
                        .append(strs[0].toString()).append(" limit 1").toString(),
                (Object[]) strs[1], (Class<T>) t.getClass());
    }

    @Override
    public <T> T findOne(SelectDataBase.DataBase dataBase,T t) throws Exception {
        Table table = t.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findOne(dataBase,table.alias(), t);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public <T> List<T> findList(SelectDataBase.DataBase dataBase,String tableName, Class<T> cls, String[] keys,
                                Object[] values) throws Exception {
        if (keys == null || values == null || keys.length != values.length
                || keys.length == 0) {
            return null;
        }
        if (StringUtils.isBlank(tableName)) {
            tableName = cls.getSimpleName();
        }
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select * from ").append(tableName).append(
                " where 1=1");
        for (int i = 0, len = keys.length; i < len; i++) {
            strBuilder.append(" and ").append(keys[i]).append("=?");
        }
        return commonDao.findList(strBuilder.toString(), values, cls);
    }

    @Override
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,Class<T> cls, String[] keys, Object[] values) throws Exception {
        Table table = cls.getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findList(dataBase,table.alias(), cls, keys, values);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public <T> T findOne(SelectDataBase.DataBase dataBase,String tableName, Class<T> cls, String[] keys,
                         Object[] values) throws Exception {
        if (keys == null || values == null || keys.length != values.length
                || keys.length == 0) {
            return null;
        }
        StringBuilder strBuilder = new StringBuilder();
        if (StringUtils.isBlank(tableName)) {
            tableName = cls.getSimpleName();
        }
        strBuilder.append("select * from ").append(tableName).append(
                " where 1=1");
        for (int i = 0, len = keys.length; i < len; i++) {
            strBuilder.append(" and ").append(keys[i]).append("=?");
        }
        strBuilder.append(" limit 1");
        return commonDao.findOne(strBuilder.toString(), values, cls);
    }

    @Override
    public <T> T findOne(SelectDataBase.DataBase dataBase,Class<T> cls, String[] keys, Object[] values) throws Exception {
        Table table = cls.getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return findOne(dataBase,table.alias(), cls, keys, values);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public int save(SelectDataBase.DataBase dataBase,String tableName, Object obj) throws Exception {
        Object[] strs = SqlUtil.getInsertSql(obj, tableName);
        return commonDao.saveOrUpdate(strs[0].toString(), (Object[]) strs[1]);
    }

    @Override
    public int save(SelectDataBase.DataBase dataBase,Object obj) throws Exception {
        Table table = obj.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return save(dataBase,table.alias(), obj);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    public int update(SelectDataBase.DataBase dataBase,String tableName, Object obj, String[] keys,
                      Object[] values) throws Exception {
        if (keys == null || values == null || keys.length != values.length
                || keys.length == 0) {
            return 0;
        }
        int len = keys.length;
        HashMap<String, Object> map = new HashMap<String, Object>(len);
        for (int i = 0; i < len; i++) {
            map.put(keys[i], values[i].toString());
        }
        Object[] strs = SqlUtil.getUpdateSql(obj, tableName, map);
        return commonDao.saveOrUpdate(strs[0].toString(), (Object[]) strs[1]);
    }

    @Override
    public int update(SelectDataBase.DataBase dataBase,Object obj, String[] keys, Object[] values) throws Exception {
        Table table = obj.getClass().getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.alias())) {
            return update(dataBase,table.alias(), obj, keys, values);
        } else {
            throw new Exception("tableName is blank");
        }
    }

    @Override
    public int saveOrUpdate(SelectDataBase.DataBase dataBase,String sql, Object[] params) {
        return commonDao.saveOrUpdate(sql, params);
    }

    @Override
    public List<Map<String, Object>> find(SelectDataBase.DataBase dataBase,String sql, Object[] objs)
            throws SQLException {
        // TODO Auto-generated method stub
        return commonDao.find(sql, objs);
    }

    @Override
    public int count(SelectDataBase.DataBase dataBase,String sql, Object[] obs) throws SQLException {
        return commonDao.count(sql, obs);
    }

    @Override
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,String sql, Object[] params, Class<T> classType) throws SQLException {
        return commonDao.findList(sql,params,classType);
    }

    @Override
    public <T> T findOne(SelectDataBase.DataBase dataBase,String sql, Object[] params, Class<T> classType) throws SQLException {
        return commonDao.findOne(sql,params,classType);
    }

    @Override
    public long updateLast(SelectDataBase.DataBase dataBase, Object obj, String tableName, String keyColumns) {
        return commonDao.updateLast(obj,tableName,keyColumns);
    }

}
