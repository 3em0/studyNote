package com.gulvn;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Test {
    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL("http://127.0.0.1/")});
        Class<?> helloWorld = urlClassLoader.loadClass("HelloWorld");
        helloWorld.newInstance();
    }
}
