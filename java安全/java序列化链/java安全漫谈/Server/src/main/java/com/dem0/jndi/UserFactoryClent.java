package com.dem0.jndi;

import com.dem0.jndi.model.User;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.beans.Expression;
import java.util.Hashtable;

public class UserFactoryClent {
    public static void main(String[] args) throws NamingException {
        System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase","true");
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "rmi://localhost:1099");
        env.put("word","Dem0");
        InitialContext ctx = new InitialContext(env);
        User obj = (User) ctx.lookup("User");
        System.out.println(obj);

        obj.who();
    }
}
