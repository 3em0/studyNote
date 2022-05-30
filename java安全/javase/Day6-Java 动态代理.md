# JAVA动态代理

这个主要实现的其实也就是可以不修改原来的代码来实现方法的增强，具体实现看以下代码。

## 0x01 API

主要方法

```java
package java.lang.reflect;

import java.lang.reflect.InvocationHandler;

/**
 * Creator: yz
 * Date: 2020/1/15
 */
public class Proxy implements java.io.Serializable {

  // 省去成员变量和部分类方法...

    /**
     * 获取动态代理处理类对象
     *
     * @param proxy 返回调用处理程序的代理实例
     * @return 代理实例的调用处理程序
     * @throws IllegalArgumentException 如果参数不是一个代理实例
     */
    public static InvocationHandler getInvocationHandler(Object proxy)
            throws IllegalArgumentException {
        ...
    }

    /**
     * 创建动态代理类实例
     *
     * @param loader     指定动态代理类的类加载器
     * @param interfaces 指定动态代理类的类需要实现的接口数组
     * @param h          动态代理处理类
     * @return 返回动态代理生成的代理类实例
     * @throws IllegalArgumentException 不正确的参数异常
     */
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h)
            throws IllegalArgumentException {
        ...
    }

    /**
     * 创建动态代理类
     *
     * @param loader     定义代理类的类加载器
     * @param interfaces 代理类要实现的接口列表
     * @return 用指定的类加载器定义的代理类，它可以实现指定的接口
     */
    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) {
        ...
    }

    /**
     * 检测某个类是否是动态代理类
     *
     * @param cl 要测试的类
     * @return 如该类为代理类，则为 true，否则为 false
     */
    public static boolean isProxyClass(Class<?> cl) {
        return java.lang.reflect.Proxy.class.isAssignableFrom(cl) && proxyClassCache.containsValue(cl);
    }

    /**
     * 向指定的类加载器中定义一个类对象
     *
     * @param loader 类加载器
     * @param name   类名
     * @param b      类字节码
     * @param off    截取开始位置
     * @param len    截取长度
     * @return JVM创建的类Class对象
     */
    private static native Class defineClass0(ClassLoader loader, String name, byte[] b, int off, int len);

}
```

`newProxyInstance`：获取一个新生成的动态代理类实例

`InvocationHandler`:获取类

`getProxyClass`:创建class

还有invocationHandler的方法

```java
 public Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
```

## 0x02 使用java.lang.reflect.Proxy动态创建类对象

其实就是他也有native的方法叫`defineClass0`，可以直接动态创建类。(直接和jvm说hello word)

大概流程就是

1. 准备一个`ClassLoader`可以用系统的那个
2. 获取`java.lang.reflect.Proxy`的`defineClass0（ClassLoader loader, String name,byte[] b, int off, int len）`方法。
3. 返she`method`的`invoke`类就出出来了。

## 0x03 使用JDK动态代理生成FileSystem动态代理类实例

```
 *     Foo f = (Foo) Proxy.newProxyInstance(Foo.class.getClassLoader(),
 *                                          new Class() { Foo.class },
 *                                          handler);
```

handler 需要实现`InvocationHandler`接口

要覆盖他的构造方法和`invoke`方法

```java
// 创建UnixFileSystem类实例
FileSystem fileSystem = new UnixFileSystem();

// 创建动态代理处理类
InvocationHandler handler = new JDKInvocationHandler(fileSystem);

// 通过指定类加载器、类实现的接口数组生成一个动态代理类
Class proxyClass = Proxy.getProxyClass(
      FileSystem.class.getClassLoader(),// 指定动态代理类的类加载器
      new Class[]{FileSystem.class}// 定义动态代理生成的类实现的接口
);

// 使用反射获取Proxy类构造器并创建动态代理类实例
FileSystem proxyInstance = (FileSystem) proxyClass.getConstructor(
      new Class[]{InvocationHandler.class}).newInstance(new Object[]{handler}
);
//Proxy(InvocationHandler h(接口)) 
```

Class类是个很有用的类,用它可以实现反射机制。[理解 new Class[]{FileSystem.class}](https://www.cnblogs.com/yepei/p/5649276.html) 这是一个接口的写法。Class类的对象用于表示当前运行的 Java 应用程序中的类和接口。

```java
package com.anbai.sec.proxy;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
public class JDKInvocationHandler implements InvocationHandler, Serializable {
    private final Object target;
    public JDKInvocationHandler(Object target) {
        this.target = target;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 为了不影响测试Demo的输出结果，这里忽略掉toString方法
        if ("toString".equals(method.getName())) {
            return method.invoke(target, args);
        }
        System.out.println("即将调用[" + target.getClass().getName() + "]类的[" + method.getName() + "]方法...");
        Object obj = method.invoke(target, args);
        System.out.println("已完成[" + target.getClass().getName() + "]类的[" + method.getName() + "]方法调用...");
        return obj;
    }
}
```

这个其实之前在javaweb中学过，就是加一个中间件，你调用proxy的这个方法，它过滤以下，加点东西进去，再选择是不是要触发。

![image-20220101012704699](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220101012704699.png)

必须时接口，这样就可以反射调用。

贴一下大哥的话

**动态代理生成出来的类有如下技术细节和特性：**

1. 动态代理的必须是接口类，通过`动态生成一个接口实现类`来代理接口的方法调用(`反射机制`)。

   ```java
   public static Object newProxyInstance(ClassLoader loader,
                                         Class<?>[] interfaces,
                                         InvocationHandler h)
   ```

   必须时接口 new Class[]{FileSystem.class}

2. 动态代理类会由`java.lang.reflect.Proxy.ProxyClassFactory`创建。

   ```
   proxyClassCache.get(loader, interfaces);
   ```

   ```
   // If the proxy class defined by the given loader implementing
   // the given interfaces exists, this will simply return the cached copy;
   // otherwise, it will create the proxy class via the ProxyClassFactory
   ```

3. `ProxyClassFactory`会调用`sun.misc.ProxyGenerator`类生成该类的字节码，并调用`java.lang.reflect.Proxy.defineClass0()`方法将该类注册到`JVM`。

   ```
     */
               byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
                   proxyName, interfaces, accessFlags);
               try {
                   return defineClass0(loader, proxyName,
                                       proxyClassFile, 0, proxyClassFile.length);
   ```

4. 该类继承于`java.lang.reflect.Proxy`并实现了需要被代理的接口类，因为`java.lang.reflect.Proxy`类实现了`java.io.Serializable`接口，所以被代理的类支持`序列化/反序列化`。

5. 该类实现了代理接口类(示例中的接口类是`com.anbai.sec.proxy.FileSystem`)，会通过`ProxyGenerator`动态生成接口类(`FileSystem`)的所有方法，

6. 该类因为实现了代理的接口类，所以当前类是代理的接口类的实例(`proxyInstance instanceof FileSystem`为`true`)，但不是代理接口类的实现类的实例(`proxyInstance instanceof UnixFileSystem`为`false`)。

7. 该类方法中包含了被代理的接口类的所有方法，通过调用动态代理处理类(`InvocationHandler`)的`invoke`方法获取方法执行结果。

8. 该类代理的方式重写了`java.lang.Object`类的`toString`、`hashCode`、`equals`方法。

9. 如果**通**过动态代理生成了多个动态代理类，新生成的类名中的`0`会自增，如`com.sun.proxy.$Proxy0/$Proxy1/$Proxy2`。

## 0x04 动态代理类实例序列化问题

通过大哥的代码，我们知道，反序列化出来的上面反编译的`proxy`类，但是实例化的时响应的子类

参考:https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html#serial

也就是说动态代理生成的类在序列化的时候不会序列化它的成员变量。

:red_circle:将该类的`Class`对象传递给`java.io.ObjectStreamClass`的静态`lookup`方法时，返回的`ObjectStreamClass`实例将具有以下特性：

1. 调用其`getSerialVersionUID`方法将返回`0L` 。
2. 调用其`getFields`方法将返回长度为零的数组。
3. 调用其`getField`方法将返回`null` 。

但是`proxy`类不受到影响，`h`变量(`InvocationHandler`)将会被序列化，这个`h`存储了动态代理类的处理类实例以及动态代理的接口类的实现类的实例。

在反序列化转换的时候时调用`resolveProxyClass`，不是`resolveClass`

:red_car:这里时从官方文档中摘抄出来的

 If a proxy instance contains an invocation handler that is not assignable to `java.io.Serializable`, however, then a `java.io.NotSerializableException` will be thrown if such an instance is written to a `java.io.ObjectOutputStream`.

如果一个代理类的实例包含了一个没有实现序列化接口的handler，那么这个代理类的实例在序列化的时候就会报错。

