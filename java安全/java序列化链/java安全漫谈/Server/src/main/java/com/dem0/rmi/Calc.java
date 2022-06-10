package com.dem0.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Calc extends UnicastRemoteObject implements ICalc{
    private int baseNumber = 123;

    public Calc() throws RemoteException {
        super();
    }

    @Override
    public Integer sum(List<Integer> params) throws RemoteException {
        Integer sum = baseNumber;
        for (Integer param : params) {
            sum += param;
        }
        return sum;
    }

    @Override
    public Object equ(Object a, Object b) throws RemoteException {
        return null;
    }
}