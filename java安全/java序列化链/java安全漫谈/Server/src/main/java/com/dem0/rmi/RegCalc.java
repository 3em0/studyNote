package com.dem0.rmi;

import sun.rmi.transport.tcp.TCPEndpoint;

import java.io.ObjectInputStream;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RegCalc {
    private void start() throws Exception {
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        System.setProperty("java.security.policy", "E:\\blog\\study-note\\java安全\\java序列化链\\java安全漫谈\\vuln.policy");
        if (System.getSecurityManager() == null) {
            System.out.println("setup SecurityManager");
            System.setSecurityManager(new SecurityManager());
        }
        Math h = new Math();
        Calc calc = new Calc();
//        new sun.rmi.registry.RegistryImpl
        LocateRegistry.createRegistry(1099);
        Naming.rebind("r", h);
        Naming.rebind("calc", calc);
//        new ObjectInputStream();

    }
    public static void main(String[] args) throws Exception {
        new RegCalc().start();
    }
}

