package com.dem0.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class serCalc {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        ICalc calc = new Calc();
        Registry registry = LocateRegistry.getRegistry("192.168.59.1", 9999);
        registry.bind("calc",calc);
    }
}
