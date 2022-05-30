---
title: Getshell新知识
date: 2020-11-13 08:31:13
tags: Buu
---

# [GKCTF2020]老八小超市儿

**题目来自buu**

## 一、题目初探

首先是一个shopxo搭建的演示站，通过扫描后台得到如下的网页

![](https://i.loli.net/2020/11/13/Q98CNd2GzDnVXwq.png)



## 二、题目解答

首先是找到后台登陆的`admin.php`,然后通过百度找到shopxo的默认管理员登陆账号和密码`admin`和`shopxo`，登陆进去之后，全是各种各样的设置窗口，看得你头皮发麻（摸摸头，发现头发没了）。一般的后台getshell的方法，现在大致有两种

1.数据库的备份，通过数据库将shell写入

2.更改图片或者是上传shell，一般的admn路径都没有限制了

这里通过寻找，很容易找到一个后台的上传shell，`主题的配置`。

![](https://i.loli.net/2020/11/13/9ipawsGRUmKIJdb.png)

可以看到文中的一个关键字，**文件上传**

然后down一个主题到本地后，解压，打开文件。这个时候有一点的迷茫，很多的目录，很多的文件，那么我的shell应该放在哪里，放在什么位置，才能被我访问到呢？

![](https://i.loli.net/2020/11/13/zVwre78ycgSB3Jm.png)

这个问题解决，接下来放入shell，然后链接蚁剑就可以了。后面还有一个小trick，就是需要通过auto.sh文件get到root权限下的目录，然后才能成功地获取到flag