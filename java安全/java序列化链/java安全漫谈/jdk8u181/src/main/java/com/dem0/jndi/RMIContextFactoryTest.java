package com.dem0.jndi;

import com.dem0.rmi.ICalc;
import com.dem0.vuln.CC6;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class RMIContextFactoryTest {
    public static void main(String[] args) {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL,"rmi://localhost:1099");

        try {
            InitialContext initialContext = new InitialContext(env);
            ICalc calc = (ICalc) initialContext.lookup("calc");
            initialContext.close();
            List<Integer> li = new ArrayList<Integer>();
            li.add(1);
            li.add(2);
            System.out.println(calc.sum(li));
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
