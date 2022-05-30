package com.dem0.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Math extends UnicastRemoteObject implements IMath{
    protected Math() throws RemoteException {
        super();
    }
    @Override
    public Integer sum(List<Integer> params) throws RemoteException {
        return  1;
    }

    @Override
    public Integer add(Integer a, Integer b) throws RemoteException {
        return  a+b;
    }
}
