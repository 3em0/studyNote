package com.dem0.jndi;

import com.dem0.rmi.Calc;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.directory.InitialDirContext;
import java.rmi.RemoteException;
import java.util.Hashtable;

public class RMIContextFactoryTest {
    public static void main(String[] args) throws RemoteException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.rmi.registry.RegistryContextFactor");
        env.put(Context.PROVIDER_URL,"rmi://localhost:1099");
        Calc calc = new Calc();
        try {
            InitialContext initialContext = new InitialContext(env);
            initialContext.bind("calc",calc);
            System.out.println("calc bindings");
            initialContext.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
