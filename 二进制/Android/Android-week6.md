# 自制路由器: `OSI`七层模型，`DIY`路由器抓包神器，免代理

- Kali Linux/Nethunter常用网络命令
- 网络工具集/瑞士军刀: netwox
- 网络层: MAC/IP/ARP/DHCP
- 传输层: IP/TCP/UDP/ICMP
- 应用层: DNS/Telnet/NC/FTP

> 终极目的: 让APP毫无感知就被抓包了

首先是通过kali自带的工具`nm-connection-editor`配置网卡

![image-20220309160810649](https://img.dem0dem0.top/images/image-20220309160810649.png)

`netwox` 老的玩法

`nethos`



## 树莓派的基础玩法

`balenaetcher`镜像刷入工具.

4G镜像开始

- `shadowsocks：3.0.0`安装 
- `ssserver`和`sslocal` 安装
- https://konstakang.com/

网络拓扑

```
curl -> proxychains -> sslocal->nps->内网穿透->npc->ssserver->4g
```

- 从USB启动

  刷入官方的系统，然后是用sudo
