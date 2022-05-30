---
title: Metasploit
date: 2020-11-14 17:23:12
tags:
---

# Metasploit使用基础

## 一、Metasploit的几大模块

**1.Auxiliaries(辅助模块)**

扫描端口服务

**2.Exploit(漏洞利用模块)**

发起攻击行为

**3.Payload（攻击载荷模块）**

执行任意代码，后期利用的过程

**4.Post(后期渗透模块)**

获取敏感信息，实施跳板攻击

**5.Encoders**

编码模块

## 二、使用辅助模块进行端口扫描

可以继续利用nmap直接进行扫描，也可以适应辅助模块中的扫描端口

## 三、后渗透攻击

进程迁移保护`migrate`

输入`run post/windows/gather/checkvm`查看是不是一台虚拟机

![image-20201114180723866](https://i.loli.net/2020/12/27/Qd5FVIn7BOejGRE.png)

输入idletime 可以查看近期运行他的时间

![image-20201114180808553](https://i.loli.net/2020/12/27/S3KicOgnQaxFpDh.png)

查看路由 `route`

![image-20201114180836861](https://i.loli.net/2020/12/27/vTkdqXpCb8A5nJw.png)

background将当前会话放到后台

`run post/windows/manage/killav`让目标机器关掉防火墙

`run post/windows/manage/enable_rdp` 开启远程桌面

![image-20201114181214476](https://i.loli.net/2020/12/27/E768sJLvenWxZR2.png)