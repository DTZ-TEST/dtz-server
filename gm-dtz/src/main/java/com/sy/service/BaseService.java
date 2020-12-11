package com.sy.service;

import com.sy.mainland.util.db.annotation.SelectDataBase;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface BaseService {
    /**
     * 查询，返回一个list
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param t         对象（条件）
     * @return list
     * @throws Exception
     */
    public <T> List<T> findList(String tableName, T t) throws Exception;

    /**
     * 查询，返回一个list
     *
     * @param <T> 对象类型
     * @param t   对象（条件）
     * @return list
     * @throws Exception
     */
    public <T> List<T> findList(T t) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param t         对象（条件）
     * @return t（返回值）
     * @throws Exception
     */
    public <T> T findOne(String tableName, T t) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T> 对象类型
     * @param t   对象（条件）
     * @return t（返回值）
     * @throws Exception
     */
    public <T> T findOne(T t) throws Exception;


    /**
     * 查询，返回一个list
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param cls       对象类型
     * @param keys      查询条件 key值
     * @param values    查询条件value值
     * @return
     * @throws Exception
     */
    public <T> List<T> findList(String tableName, Class<T> cls, String[] keys,
                                Object[] values) throws Exception;

    /**
     * 查询，返回一个list
     *
     * @param <T>    对象类型
     * @param cls    对象类型
     * @param keys   查询条件 key值
     * @param values 查询条件value值
     * @return
     * @throws Exception
     */
    public <T> List<T> findList(Class<T> cls, String[] keys,
                                Object[] values) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param cls       对象类型
     * @param keys      查询条件 key值
     * @param values    查询条件value值
     * @return
     * @throws Exception
     */
    public <T> T findOne(String tableName, Class<T> cls, String[] keys,
                         Object[] values) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T>    对象类型
     * @param cls    对象类型
     * @param keys   查询条件 key值
     * @param values 查询条件value值
     * @return
     * @throws Exception
     */
    public <T> T findOne(Class<T> cls, String[] keys,
                         Object[] values) throws Exception;

    /**
     * 保存
     *
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param obj       对象
     * @return
     * @throws Exception
     */
    public int save(String tableName, Object obj) throws Exception;

    /**
     * 保存
     *
     * @param obj 对象
     * @return
     * @throws Exception
     */
    public int save(Object obj) throws Exception;

    /**
     * 修改
     *
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param obj       对象
     * @param keys      条件 key值
     * @param values    条件value值
     * @return
     * @throws Exception
     */
    public int update(String tableName, Object obj, String[] keys,
                      Object[] values) throws Exception;

    /**
     * 修改
     *
     * @param obj    对象
     * @param keys   条件 key值
     * @param values 条件value值
     * @return
     * @throws Exception
     */
    public int update(Object obj, String[] keys,
                      Object[] values) throws Exception;

    /**
     * 保存或者更新
     *
     * @param sql
     * @param params
     * @return
     */
    public int saveOrUpdate(String sql, Object[] params);

    /**
     * 查询
     *
     * @param sql
     * @param objs
     * @return
     * @throws SQLException
     */
    public List<Map<String, Object>> find(String sql, Object[] objs)
            throws SQLException;

    /**
     * 统计数量
     * @param sql
     * @param obs
     * @return
     * @throws SQLException
     */
    int count(String sql, Object[] obs) throws SQLException;

    /**
     * 查询单个对象
     * @param sql	要执行的sql语句
     * @param params	参数数组
     * @param classType		类
     * @return		返回单个对象
     * @throws SQLException
     */
    public <T> T findOne(String sql, Object[] params, Class<T> classType) throws SQLException;
    /**
     * 查询多个对象
     * @param sql	要执行的sql语句
     * @param params	参数数组
     * @param classType		类
     * @return	返回对象组成的list
     * @throws SQLException
     */
    public <T> List<T> findList(String sql, Object[] params, Class<T> classType) throws SQLException;

    /**
     * 保存单个对象，并返回对象的主键值
     * @param obj	要保存的对象
     * @param tableName	表名
     * @param keyColumns	要返回的主键字段名
     * @return	返回主键值
     */
    public long updateLast(Object obj, String tableName, String keyColumns);

    /**
     * 保存并返回主键
     * @param t
     * @return
     */
    public <T> long saveAndGetKey(T t);

    /**
     * 保存并返回主键
     * @param tableName
     * @param t
     * @return
     */
    public <T> long saveAndGetKey(String tableName,T t);

    /**
     * 查询，返回一个list
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param t         对象（条件）
     * @return list
     * @throws Exception
     */
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,String tableName, T t) throws Exception;

    /**
     * 查询，返回一个list
     *
     * @param <T> 对象类型
     * @param t   对象（条件）
     * @return list
     * @throws Exception
     */
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,T t) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param t         对象（条件）
     * @return t（返回值）
     * @throws Exception
     */
    public <T> T findOne(SelectDataBase.DataBase dataBase,String tableName, T t) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T> 对象类型
     * @param t   对象（条件）
     * @return t（返回值）
     * @throws Exception
     */
    public <T> T findOne(SelectDataBase.DataBase dataBase,T t) throws Exception;


    /**
     * 查询，返回一个list
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param cls       对象类型
     * @param keys      查询条件 key值
     * @param values    查询条件value值
     * @return
     * @throws Exception
     */
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,String tableName, Class<T> cls, String[] keys,
                                Object[] values) throws Exception;

    /**
     * 查询，返回一个list
     *
     * @param <T>    对象类型
     * @param cls    对象类型
     * @param keys   查询条件 key值
     * @param values 查询条件value值
     * @return
     * @throws Exception
     */
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,Class<T> cls, String[] keys,
                                Object[] values) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T>       对象类型
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param cls       对象类型
     * @param keys      查询条件 key值
     * @param values    查询条件value值
     * @return
     * @throws Exception
     */
    public <T> T findOne(SelectDataBase.DataBase dataBase,String tableName, Class<T> cls, String[] keys,
                         Object[] values) throws Exception;

    /**
     * 查询，返回一个对象
     *
     * @param <T>    对象类型
     * @param cls    对象类型
     * @param keys   查询条件 key值
     * @param values 查询条件value值
     * @return
     * @throws Exception
     */
    public <T> T findOne(SelectDataBase.DataBase dataBase,Class<T> cls, String[] keys,
                         Object[] values) throws Exception;

    /**
     * 保存
     *
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param obj       对象
     * @return
     * @throws Exception
     */
    public int save(SelectDataBase.DataBase dataBase,String tableName, Object obj) throws Exception;

    /**
     * 保存
     *
     * @param obj 对象
     * @return
     * @throws Exception
     */
    public int save(SelectDataBase.DataBase dataBase,Object obj) throws Exception;

    /**
     * 修改
     *
     * @param tableName 表名
     *                  if(tableName==null||tableName.trim().length==0)tableName=t.getClass
     *                  ().getSimpleName();
     * @param obj       对象
     * @param keys      条件 key值
     * @param values    条件value值
     * @return
     * @throws Exception
     */
    public int update(SelectDataBase.DataBase dataBase,String tableName, Object obj, String[] keys,
                      Object[] values) throws Exception;

    /**
     * 修改
     *
     * @param obj    对象
     * @param keys   条件 key值
     * @param values 条件value值
     * @return
     * @throws Exception
     */
    public int update(SelectDataBase.DataBase dataBase,Object obj, String[] keys,
                      Object[] values) throws Exception;

    /**
     * 保存或者更新
     *
     * @param sql
     * @param params
     * @return
     */
    public int saveOrUpdate(SelectDataBase.DataBase dataBase,String sql, Object[] params);

    /**
     * 查询
     *
     * @param sql
     * @param objs
     * @return
     * @throws SQLException
     */
    public List<Map<String, Object>> find(SelectDataBase.DataBase dataBase,String sql, Object[] objs)
            throws SQLException;

    /**
     * 统计数量
     * @param sql
     * @param obs
     * @return
     * @throws SQLException
     */
    int count(SelectDataBase.DataBase dataBase,String sql, Object[] obs) throws SQLException;

    /**
     * 查询单个对象
     * @param sql	要执行的sql语句
     * @param params	参数数组
     * @param classType		类
     * @return		返回单个对象
     * @throws SQLException
     */
    public <T> T findOne(SelectDataBase.DataBase dataBase,String sql, Object[] params, Class<T> classType) throws SQLException;
    /**
     * 查询多个对象
     * @param sql	要执行的sql语句
     * @param params	参数数组
     * @param classType		类
     * @return	返回对象组成的list
     * @throws SQLException
     */
    public <T> List<T> findList(SelectDataBase.DataBase dataBase,String sql, Object[] params, Class<T> classType) throws SQLException;

    /**
     * 保存单个对象，并返回对象的主键值
     * @param obj	要保存的对象
     * @param tableName	表名
     * @param keyColumns	要返回的主键字段名
     * @return	返回主键值
     */
    public long updateLast(SelectDataBase.DataBase dataBase,Object obj, String tableName, String keyColumns);

    /**
     * 保存并返回主键
     * @param t
     * @return
     */
    public <T> long saveAndGetKey(SelectDataBase.DataBase dataBase,T t);

    /**
     * 保存并返回主键
     * @param tableName
     * @param t
     * @return
     */
    public <T> long saveAndGetKey(SelectDataBase.DataBase dataBase, String tableName, T t);
}
