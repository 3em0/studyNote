package com.dem0.internal;

import de.qtc.rmg.internal.ExceptionHandler;
import javassist.*;

import java.io.*;
import java.rmi.Remote;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

public class ReflectUtils {

    private static ClassPool pool;
    private static CtClass remoteClass;
    private static CtClass remoteStubClass;
    private static Set<String> createdClasses;


    /**
     * 初始化存储remoteClass和remoteStubClass方便生成接口时调用
     */
    static {
        pool = ClassPool.getDefault();

        try {
            remoteClass = pool.getCtClass(Remote.class.getName());
            remoteStubClass = pool.getCtClass(RemoteStub.class.getName());
        } catch (NotFoundException e) {
            ExceptionHandler.internalError("ReflectUtil.init", "Caught unexpected NotFoundException.");
        }

        createdClasses = new HashSet<String>();
    }

    /**
     * 将RMIClassLoader设置我们自定义的RMICLASSLoader
     */
    public static void enableCustomRMIClassLoader()
    {
        System.setProperty("java.rmi.server.RMIClassLoaderSpi", "com.dem0.internal.CustomRMIClassLoader");
    }

    /**
     * 生成对应的远程接口，继承自Remote
     *
     * @param className 类名
     * @return created 生成类
     * @throws CannotCompileException 编译错误
     */
    public static Class makeInterface(String className) throws CannotCompileException
    {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {}

        CtClass intfClz = pool.makeInterface(className, remoteClass);
        createdClasses.add(className);

        return intfClz.toClass();
    }

    /**
     * 设置类serialVersionUID字段为2L,对于一些远程类有用
     *
     * @param ctClass class where the serialVersionUID should be added to
     * @throws CannotCompileException should never be thrown in practice
     */
    private static void addSerialVersionUID(CtClass ctClass) throws CannotCompileException
    {
        CtField serialID = new CtField(CtPrimitiveType.longType, "serialVersionUID", ctClass);
        serialID.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        ctClass.addField(serialID, CtField.Initializer.constant(2L));
    }

    /**
     * 这个函数与makeInterface类似，但是作用于传统的RMI Remote Stub机制
     * 其中生成的临时接口类需要设置serialVersionUID为2来满足RMI RemoteStub的默认值
     */
    public static Class makeLegacyStub(String className) throws CannotCompileException, NotFoundException
    {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {}

        makeInterface(className + "Interface");
        CtClass intf = pool.getCtClass(className + "Interface");

        CtClass ctClass = pool.makeClass(className, remoteStubClass);
        ctClass.setInterfaces(new CtClass[] { intf });
        addSerialVersionUID(ctClass);

        createdClasses.add(className);
        return ctClass.toClass();
    }

    public static byte[] WriteObjectToBytes(UnicastRemoteObject remoteObject) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bas);
        oos.writeObject(remoteObject);
        return bas.toByteArray();
    }

    public static void readObjectFromBytes(byte[] serializeData) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        ois.readObject();


    }
}
