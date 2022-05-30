package com.gulvn;

import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(1);
        Class clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor construct = clazz.getDeclaredConstructor(Class.class, Map.class);
        construct.setAccessible(true);
//        Object obj = construct.newInstance(Retention.class, outerMap);
    }
}
