package com.sy.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.sy.dao.BaseDao;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.SqlPrintUtil;
import com.sy.mainland.util.db.annotation.Table;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseDaoImpl extends JdbcDaoSupport implements BaseDao {

    private static final Logger logger = LoggerFactory.getLogger(BaseDaoImpl.class);

    @SuppressWarnings("unchecked")
    public <T> T findOne(String sql, Object[] params, Class<T> classType) throws SQLException {
        if (sql != null && sql.indexOf("limit") == -1 && sql.indexOf("LIMIT") == -1) {
            sql = new StringBuilder().append(sql).append(" limit 1").toString();
        }
        SqlPrintUtil.printSql(sql, params);
        List<Map<String, Object>> s = getJdbcTemplate().queryForList(sql, params);
        if (s == null || s.size() == 0) {
            return null;
        }
        try {
            return CommonUtil.map2Entity(classType, s.get(0));
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findList(String sql, Object[] params, Class<T> classType) throws SQLException {
        SqlPrintUtil.printSql(sql, params);
        List<Map<String, Object>> s = getJdbcTemplate().queryForList(sql, params);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < s.size(); i++) {
            try {
                list.add(CommonUtil.map2Entity(classType, s.get(i)));
            } catch (Exception e) {
                logger.error("Exception:" + e.getMessage(), e);
            }
        }
        return list;
    }

    public int saveOrUpdate(String sql, Object[] params) {
        SqlPrintUtil.printSql(sql, params);
        return getJdbcTemplate().update(sql, params);
    }

    public long saveAndGetKey(final String sql,final Object[] params){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {

                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                for (int i=0;i<params.length;i++){
                    ps.setObject(i+1,params[i]);
                }

                return ps;
            }
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public long updateLast(Object obj, String tableName, String keyColumns) {// 保存对象，并获取主键值
        SqlPrintUtil.printSql(tableName, JSONObject.toJSONString(obj));
        SimpleJdbcInsertOperations simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate());

        if (StringUtils.isBlank(tableName)) {
            Table table = obj.getClass().getAnnotation(Table.class);
            if (table != null) {
                tableName = table.alias();
            }
        }

        simpleJdbcInsert.withTableName(tableName);
        BeanPropertySqlParameterSource bp = new BeanPropertySqlParameterSource(obj);
        SqlParameterSource sps = bp;
        return (Long) simpleJdbcInsert.usingGeneratedKeyColumns(keyColumns).executeAndReturnKey(sps);
    }

    @Override
    public List<Map<String, Object>> find(String sql, Object[] obs) throws SQLException {
        // TODO Auto-generated method stub
        SqlPrintUtil.printSql(sql, obs);
        return getJdbcTemplate().queryForList(sql, obs);
    }

    @Override
    public int count(String sql, Object[] obs) throws SQLException {
        SqlPrintUtil.printSql(sql, obs);
        List<Map<String, Object>> list=getJdbcTemplate().queryForList(sql, obs);
        if (list!=null&&list.size()==1){
            if (list.get(0).size()==1){
                Object ob=list.get(0).entrySet().iterator().next().getValue();
                if (ob==null){
                    return 0;
                }else{
                    return CommonUtil.object2Int(ob);
                }
            }
        }
        return 0;
    }
}
