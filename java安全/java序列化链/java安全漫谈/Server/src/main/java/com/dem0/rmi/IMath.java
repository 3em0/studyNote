package com.dem0.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IMath extends Remote {
    public Integer sum(List<Integer> params) throws RemoteException;
    public Integer add(Integer a,Integer b) throws RemoteException;
}
