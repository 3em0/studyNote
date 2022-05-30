package com.dem0.internal;

import de.qtc.rmg.internal.ExceptionHandler;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.HashMap;
import java.util.Set;

public class CustomRMIClassLoader extends RMIClassLoaderSpi {

    private static RMIClassLoaderSpi originalLoader = RMIClassLoader.getDefaultProviderInstance();
    private static HashMap<String, Set<String>> codebases = new HashMap<>();

    @Override
    public Class<?> loadClass(String codebase, String name, ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
        Class<?> resolvedClass = null;

        //不从远程加载取消codebase
        codebase = null;
        try{
            if (name.endsWith("_Stub"))
                ReflectUtils.makeLegacyStub(name);

            resolvedClass = originalLoader.loadClass(codebase,name,defaultLoader);
        }catch (CannotCompileException | NotFoundException e){
            ExceptionHandler.internalError("loadClass", "Unable to compile unknown stub class.");
        }

        return resolvedClass;
    }

    @Override
    public Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
        Class<?> resolvedClass = null;
        try{
            for (String interfaceName:
                    interfaces) {
                ReflectUtils.makeInterface(interfaceName);
            }

            codebase = null;
            resolvedClass = originalLoader.loadProxyClass(codebase,interfaces,defaultLoader);

        } catch (CannotCompileException e) {
            ExceptionHandler.internalError("loadProxyClass", "Unable to compile unknown interface class.");
        }

        return resolvedClass;
    }

    @Override
    public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
        codebase = null;
        return originalLoader.getClassLoader(codebase);
    }

    @Override
    public String getClassAnnotation(Class<?> cl) {
        return originalLoader.getClassAnnotation(cl);
    }
}
