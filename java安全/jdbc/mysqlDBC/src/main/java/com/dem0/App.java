package com.dem0;

import java.sql.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ClassNotFoundException, SQLException {
        String Driver = "com.mysql.cj.jdbc.Driver";
//        com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor
//        com.mysql.cj.jdbc.result.ResultSetImpl
        String DB_URL = "jdbc:mysql://127.0.0.1:3306/sys?characterEncoding=utf8&useSSL=false&queryInterceptors=com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor&autoDeserialize=true&serverTimezone=UTC";
        Class.forName(Driver);
        Connection conn = DriverManager.getConnection(DB_URL,"root","root");
    }
}
