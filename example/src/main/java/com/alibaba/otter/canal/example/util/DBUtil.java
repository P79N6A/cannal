package com.alibaba.otter.canal.example.util;

import org.apache.commons.dbcp.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

        private static Properties properties = new Properties();
        private static DataSource dataSource;
        //加载DBCP配置文件
        static{
            try{
                InputStream is  = DBUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
                properties.load(is);
            }catch(IOException e){
                e.printStackTrace();
            }

            try{
                dataSource = BasicDataSourceFactory.createDataSource(properties);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    //获取数据源
    public static DataSource getDataSource(){
        return dataSource;
    }

        //从连接池中获取一个连接
        public static Connection getConnection(){
            Connection connection = null;
            try{
                connection = dataSource.getConnection();
            }catch(SQLException e){
                e.printStackTrace();
            }
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return connection;
        }

        public static void main(String[] args) {
            getConnection();
        }
}
