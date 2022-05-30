package com.dem0.ldap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static com.dem0.ldap.Server.LDAP_URL;

public class Client {
    public static void main(String[] args) throws NamingException {
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");
        Context ctx = new InitialContext();

        // 获取RMI绑定的恶意ReferenceWrapper对象
        Object obj = ctx.lookup(LDAP_URL);

        System.out.println(obj);

    }
}