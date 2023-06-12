# JDBC

jdbc为java数据库连接标准，为了统一数据库的连接格式而制定的。常见写法:先加载数据库驱动，再创建jdbc数据库连接，最后执行sql。

```java
String Driver = "com.mysql.cj.jdbc.Driver"; //从 mysql-connector-java 6开始
//        String Driver = "com.mysql.jdbc.Driver"; // mysql-connector-java 5
        String DB_URL = "jdbc:mysql://127.0.0.1:3306/security?serverTimezone=UTC&useSSL=true";
        //1.加载启动
        Class.forName(Driver);
        //2.建立连接
        Connection conn = DriverManager.getConnection(DB_URL, "root", "root");
        //3.操作数据库，实现增删改查
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from users");
        //如果有数据，rs.next()返回true
        while (rs.next()) {
            System.out.println(rs.getString("id") + " : " + rs.getString("username"));
        }
```

POC中有这么一段

```
queryInterceptors=com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor&autoDeserialize=true
```

在议题中，作者想找到其中反序列化的利用，首先要找到哪里存在`readObject`的利用，于是瞄准了`com.mysql.cj.jdbc.result.ResultSetImpl#getObject`方法，并且在`com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor#resultSetToMap`中有对于该接口的调用。

![image-20230611183027419](https://img.dem0dem0.top/imagesimage-20230611183027419.png)

其中的`this.connection.getPropertySet().getBooleanProperty(PropertyDefinitions.PNAME_autoDeserialize`这段代码对于了其中JDBC中的`autoDeserializ`,整个调用的流程也就很正常了。 下面就是如何写出自己的`fake server`了。

> 解释flag字段需大于128（第一次跟着搓协议，优点紧张 嘿嘿）
>
> [MySQL: Column Definition](https://dev.mysql.com/doc/dev/mysql-server/8.0.33/page_protocol_com_query_response_text_resultset_column_definition.html)

![image-20230612100623977](https://img.dem0dem0.top/imagesimage-20230612100623977.png)

![image-20230612100639734](https://img.dem0dem0.top/imagesimage-20230612100639734.png)

这两个地方都说明了这一点。

> 多版本使用可以参考：[MySQL_Fake_Server/](MySQL_Fake_Server/)

参考链接：

> 1. [小白看得懂的MySQL JDBC 反序列化漏洞分析 - 先知社区 (aliyun.com)](https://xz.aliyun.com/t/8159)
> 2. [MySQL内核分析之文本结果集协议分析 Text ResultSet_runscript.sh的博客-CSDN博客](https://blog.csdn.net/zhangh571354026/article/details/120461225)
> 3. [MySQL JDBC 客户端反序列化漏洞分析_jdbc反序列化读取文件_fnmsd的博客-CSDN博客](https://blog.csdn.net/fnmsd/article/details/106232092)
> 4. [MySQL JDBC 客户端反序列化漏洞 (seebug.org)](https://paper.seebug.org/1227/#sql_1)
> 5. [由CVE-2022-21724引申jdbc漏洞 (qq.com)](https://mp.weixin.qq.com/s?__biz=MzUzNDMyNjI3Mg==&mid=2247485275&idx=1&sn=e06b07579ecef87f8cce4536d25789ce&chksm=fa973a34cde0b322ef3949c2cf7fc6bf31e945674d2fe313a3dbf63504bdf737f05cba65de18&mpshare=1&scene=23&srcid=0414XqOEScLh3JIaaHk9pp4v&sharer_sharetime=1649906865169&sharer_shareid=33fdea7abe6be586e131951d667ccd06#rd)
