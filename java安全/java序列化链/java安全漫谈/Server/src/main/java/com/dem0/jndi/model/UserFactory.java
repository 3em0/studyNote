package com.dem0.jndi.model;



import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;
//https://docs.oracle.com/javase/8/docs/api/javax/naming/spi/ObjectFactory.html
public class UserFactory implements ObjectFactory {
    /**
     *
     * @param obj 可能包含了地址和引用信息的对象 可以用来创建新的
     * @param name The name of this object relative to nameCtx. 相对于上下文的name
     * @param nameCtx 上下文
     * @param environment 环境
     * @return 返回一个对象
     * @throws Exception
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        System.out.println("UserFactory::getObjectInstance");
        System.out.println(String.format("obj %s name %s nameCtx %s",obj,name,nameCtx));
        System.out.println("environment");
        environment.forEach((k,v)->{
            System.out.println(String.format("key: %s value:%s",k,v));
        });
        String word = (String) environment.get("word");
        return new User(word==null?"Hi":word);
    }
}
