package com.dem0.learn;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JavaLangObject {

    public static void main(String[] args) throws IOException {
        People people = new People();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping();
        String json="{\"age\":10,\"name\":\"l1nk3r\",\"sex\":[\"com.dem0.learn.MySex\",{\"sex\":100}]}";
        People p2 = objectMapper.readValue(json, People.class);
        System.out.println(p2);
    }

}

class People{
    public int age;
    public String name;
    public Sex sex;

    @Override
    public String toString() {
        return "People{" +
                "age=" + age +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                '}';
    }
}
interface Sex{
    public void setSex(int sex);
    public int getSex();
}
class  MySex implements Sex{
    public MySex(){

    }
    public  int sex;
    @Override
    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public int getSex() {
        return this.sex;
    }
}