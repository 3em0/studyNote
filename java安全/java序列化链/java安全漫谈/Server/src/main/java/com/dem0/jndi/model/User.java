package com.dem0.jndi.model;

import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public User(String name){
        this.name = name;
    }
    public void who(){
        System.out.println("I am "+ name);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
