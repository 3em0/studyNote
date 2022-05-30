package com.dem0.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ICalc extends Remote {
    public Integer sum(List<Integer> params) throws RemoteException;
    Object equ(Object a,Object b) throws RemoteException;

}
