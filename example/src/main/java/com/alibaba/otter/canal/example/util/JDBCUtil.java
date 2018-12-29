package com.alibaba.otter.canal.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.alibaba.otter.canal.example.util.DBUtil.getConnection;

public class JDBCUtil {

    /**
     * 优化后的query,只需传递sql命令和需要返回的对象类型就可获得一个对象集合，
     * 在展示层只需变量集合直接操作对象获取属性值，完全面向对象的方式
     * @param sql —— 查询SQL命令
     * @param parms —— 查询条件
     * @param classPo —— 需要返回结果集合的类型
     * @return —— 返回一个LIST容器装PO对象，前台可直接遍历操作对象
     */
    public static List query( String sql , Object[] parms,Class classPo ) throws SQLException {
        ReadWriteLock rwl = new ReentrantReadWriteLock();
        Connection conn = getConnection();
        PreparedStatement ps=null;
        ResultSet rs=null;
        List<Map<String,Object>> resultList = new  ArrayList<Map<String,Object>>();
        try {
            ps = conn.prepareStatement(sql);    //预编译SQL
            if(parms != null && 0!=parms.length ){
                for( int i = 0; i<parms.length; i++ ){
                    ps.setObject(i+1, parms[i]);   //循环设置参数
                }
            }
            //QueryRunner queryRunner = new QueryRunner(DBUtil.getDataSource());
            //resultList = queryRunner.query(conn, sql, new MapListHandler());
            rs = ps.executeQuery();   //执行查询操作
            //下面开始封装每一行数据放入MAP，并将每行数据放入LIST
            if( rs != null ){
                ResultSetMetaData rsm = rs.getMetaData();   //用于获取结果集中列的类型和属性信息对象
                while( rs.next() ){
                    Map<String,Object> map = new HashMap<String,Object>();
                    for( int i = 1; i<=rsm.getColumnCount();i++ ){
                        map.put(rsm.getColumnName(i), rs.getObject(i));  //字段名称——字段值
                    }
                    resultList.add(map);   //将一行的数据放入LIST
                }
            }
            //利用反射来封住数据返回一个指定对象类型的数据集
            //LIST结构：[AddressBookPO@9446e4, AddressBookPO@ba5c7a, AddressBookPO@10d593e]
            //entityList = ResultSetToObject.ToObjectList(classPo, resultList);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                close( conn , ps , rs);   //关闭所有对象
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

    /**
     * 增删改操作
     * @param sql  SQL查询语句
     * @param pares  判断条件
     * @return
     */
    public static int edit(String sql, Object[] pares ) throws SQLException {
        int edit = -1;
        Logger logger = LoggerFactory.getLogger(JDBCUtil.class);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
            if(pares != null && pares.length > 0){
                for (int i = 0; i<pares.length ; i++){
                    pstmt.setString(i + 1 , (String)pares[i]);
                }
            }
            //pstmt.addBatch();
            //int[] ints = pstmt.executeBatch();
            edit = pstmt.executeUpdate();
            pstmt.clearBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            conn.rollback();
            logger.error("commit fail -->tollback");
            e.printStackTrace();
        }finally {
            try {
                close( conn , pstmt , null);   //关闭所有对象
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return edit;
    }


    /**
     * 关闭所有对象
     * @throws Exception
     */
    public static void close(Connection conn , PreparedStatement ps ,  ResultSet rs) throws Exception{
        if( rs != null ){
            rs.close();
        }
        if( ps != null ){
            ps.close();
        }
        if( conn != null ){
            conn.close();
        }
    }
}
