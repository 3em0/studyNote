package com.model;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;;

public class User implements Serializable {
    protected String name;
    protected User parent;
    public User(String name)
    {
        this.name = name;
    }
    public void setParent(User parent)
    {
        this.parent = parent;
    }

    public static void main(String[] args) throws Exception {
        User user = new User("Bob");
        user.setParent(new User("Josua"));
        ByteArrayOutputStream byteSteam = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteSteam);
        oos.writeObject(user);
        System.out.println(Base64.getEncoder().encodeToString(byteSteam.toByteArray()));

    }
}

