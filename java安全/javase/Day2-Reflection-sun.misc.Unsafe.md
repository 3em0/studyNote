# java反射机制

## 0x01 获取Class对象

方法: 

- `类名`.class 
- classLoader.loadClass
- Class.forName()

:a:注意

获取数组类型的对象

```java
Class<?> doubleArray = Class.forName("[D");//相当于double[].class
Class<?> cStringArray = Class.forName("[[Ljava.lang.String;");// 相当于String[][].class
```

获取内部类

`com.dem0.test$Hello`

## 0x02 反射Runtime

普通执行

```java
Runtime.getRuntime().exec("whoami")
```

反射执行

```java
Class runtimeClass = Class.forName("java.lang.Runtime");//反射拿到class
Constructor constr = runtimeClass.getDeclaredConstructor();//获取构造方法
constr.setAccessible(true);//将构造方法的属性设置未public
Obejct runtimeIns = constr.newInstance();//创建新示例
Method runtimeMethod = runtimeClass.getMethod("exec",String.class);//反射获取方法
Process process = (Process) runtimeMethod.invoke(runtimeIns,cmd);//反射调用方法
```

## 0x03 反射调用类方法

获取所有类方法

```
Method methods = cla.getDeclaredMethods()
```

获取当前类指定的成员方法

```
Method method = clazz.getDeclaredMethod("方法名", 参数类型如String.class，多个参数用","号隔开);
```

:aerial_tramway: getMethod和getDeClaredMethod区别:

第一个只能获取到public和父类方法

第二个所有方法都能获取

:sa:调用方法

```java
method.invoke(方法实例对象, 方法参数值，多个参数值用","隔开);
```

第一个参数时实例化的对象，如果时static类型的可以直接传递null。

第二个参数， 有参数那么就必须严格的`依次传入对应的参数类型`。

## 0x04 反射调用成员变量

成员变量: **fields** 

获取所有成员变量

```
Field fields = clazz.getDclaredFields();
```

获取当前类指定的成员变量

```
 clazz.getDclaredField(“name”);
```

获取值

```
filed.get(Object)
```

修改

```
filed.set(Object，value)
```

**field.setAccessible(true)**

yyds!

#  sun.misc.Unsafe

底层API 提供关于`内存、CAS、线程调度、类、对象`

## 0x01 获取Unsafe对象

```
 public final class Unsafe {
 private Unsafe() {
    }

    @CallerSensitive
    public static Unsafe getUnsafe() {
        Class var0 = Reflection.getCallerClass();
        if (!VM.isSystemDomainLoader(var0.getClassLoader())) {
            throw new SecurityException("Unsafe");
        } else {
            return theUnsafe;
        }
    }
```

发现他的源码不难看出来，这个类不能被继承，而且通过getUnsafe必须时跟加载器才行。

```
// 反射获取Unsafe的theUnsafe成员变量
Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");

// 反射设置theUnsafe访问权限
theUnsafeField.setAccessible(true);

// 反射获取theUnsafe成员变量值
Unsafe unsafe = (Unsafe) theUnsafeField.get(null);
```

当然我们也可以反射拿到构造方法最后新建实例

```java
Constructor constructor = Unsafe.class.getDeclaredConstructor();
constructor.setAccessible(true);//设置可以被访问
Unsafe unsafe1 = (Unsafe) constructor.newInstance();
```

## 0x02 NB方法-allocateInstance

假如我们现在有一个类，因为一些原因我们既不可以通过new和反射来新建实例。那么allocateInstance可以绕过这个限制。

```java
UnSafeTest test = (UnSafeTest) unsafe1.allocateInstance(UnSafeTest.class);
```

这样我们就可以绕过RASP的一些限制

## 0x02 NB方法-defineClass

之前我学过可以通过`defineClass0/1/2`直接向JVM注册一个类。那么我们在ClassLoader被限制的情况下使用这个方法。

```
unsafe.defineClass(Test_class_name,test_class_byte,0,length)
```

:a: 或者调用传入类加载器和保护域

```
// 获取系统的类加载器
ClassLoader classLoader = ClassLoader.getSystemClassLoader();

// 创建默认的保护域
ProtectionDomain domain = new ProtectionDomain(
    new CodeSource(null, (Certificate[]) null), null, classLoader, null
);

// 使用Unsafe向JVM中注
Class helloWorldClass = unsafe1.defineClass(
    TEST_CLASS_NAME, TEST_CLASS_BYTES, 0, TEST_CLASS_BYTES.length, classLoader, domain
);
```

defineAnonymousClass方法还可以创建内部类

:label: 注意!

这个实例只在java8之前版本的，在java8中应该使用第二种方法。

`Java 11`开始`Unsafe`类已经把`defineClass`方法移除了(`defineAnonymousClass`方法还在)。但是可以使用Lookup.defineClass代替，但是只是间接调用Loader的define，所以没得玩了。



# 测试sun.misc.unsafe

![image-20211229011503686](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229011503686.png)

![image-20211229011718838](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229011718838.png)
