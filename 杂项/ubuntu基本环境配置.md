# ubuntu安全环境配置

## 0x01 docker环境

```
sudo apt-get remove docker docker-engine docker.io containerd runc 
sudo apt-get update 
sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release 
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null&&sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io
sudo docker run hello-world 
```

## 0x02 python环境

```
wget https://repo.anaconda.com/archive/Anaconda3-2021.05-Linux-x86_64.sh
```

然后

```
bash Anaconda3-2021.05-Linux-x86_64.sh
```

一直yes，如果最后你不小心安装了yes

```
export PATH=/home/(your_user_name)/anaconda3/bin:$PATH
source .bashrc
```

## 0x03 tmux 终端管理

安装

```
sudo apt-get install tmux
```

使用参考链接: https://www.ruanyifeng.com/blog/2019/10/tmux.html

常用快捷键

tmux下

``` 
切分窗口
ctrl+b % 左右
ctrl+b " 上下
ctrl+b d 分离session
Ctrl+b x：关闭当前窗格。
Ctrl+b !：将当前窗格拆分为一个独立窗口
Ctrl+b q：显示窗格编号。
Ctrl+b Ctrl+<arrow key>：按箭头方向调整窗格大小。
创建windows
Ctrl+b c：创建一个新窗口，状态栏会显示多个窗口的信息。
Ctrl+b p：切换到上一个窗口（按照状态栏上的顺序）。
Ctrl+b n：切换到下一个窗口。
Ctrl+b <number>：切换到指定编号的窗口，其中的<number>是状态栏上的窗口编号。
Ctrl+b w：从列表中选择窗口。
Ctrl+b ,：窗口重命名。
```

bash下

```
tmux new -s myse 新建一个session
tmux attach-session -t my_session 重新加入session
```

新建一个session相当于重新开启一个tmux

## 0x04 vcode

```
sudo dpkg -i code.deb
```

## 0x05 java 环境

安装jenv

```
git clone https://github.com/jenv/jenv.git ~/.jenv
加在/etc/profile
export PATH="$HOME/.jenv/bin:$PATH";
```

jenv安装Java

```
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.deb
sudo dpkg -i jdk-17_linux-x64_bin.deb
sudo apt --fix-broken install
```

路径在`/usr/lib/jvm`

添加环境变量

```
JAVA_HOME="/usr/lib/jvm/jdk-17/bin";
export JAVA_HOME;
PATH=$PATH:$JAVA_HOME;
export PATH;
```

jenv配置

```
  vim ~/.bashrc  配置当前用户的环境变量
   etc/profile ---> etc/bash.bashrc ----> 用户1/.bashrc ----> 用户1/.profile
```

jenv常用指令

```
jenv add
cat /etc/profile => restart 生效
```

![image-20210924115611945](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20210924115611945.png)

## 0x06 拯救麻瓜操作

进行命令行模式 撤销之前的操作

`ctrl+alt + fx`

## 0x07 docker+vcode

直接放Dockerfile

```
FROM dasctfbase/web_php73_apache_mysql
  
COPY src /var/www/html
#php.ini /usr/local/etc/php/conf.d
# 如需自定义 FLAG 请将自定义脚本覆盖到 /flag.sh
COPY files/flag.sh /flag.sh
# 如需操作数据库请将 sql 文件拷贝到 /db.sql
COPY files/db.sql /db.sql
COPY files/xdebug-3.0.4.tgz /tmp/xdebug-3.0.4.tgz
COPY files/php/upload.ini /usr/local/etc/php/conf.d
#COPY files/php.ini /usr/local/etc/php/php.ini
#COPY files/apache2.conf /etc/apache2/apache2.conf
# 如有上传文件等操作请务必将权限设置正确！
# RUN chown www-data:www-data /var/www/html/uploads/
##dir /usr/include
RUN apt-get clean &&\
    apt-get update &&\
	apt-get install -y libfreetype6-dev &&\
	apt-get install -y libjpeg62-turbo-dev &&\
	apt-get install -y libpng-dev &&\
    docker-php-source extract  &&\
	docker-php-ext-configure gd --with-freetype-dir=/usr/include/ --with-jpeg-dir=/usr/include/ &&\ 
	docker-php-ext-install -j$(nproc) gd &&\
	docker-php-ext-install pdo &&\
	docker-php-ext-install pdo_mysql &&\
	docker-php-ext-install mbstring &&\
	tar -xf /tmp/xdebug-3.0.4.tgz -C /tmp/ &&\
	mv /tmp/xdebug-3.0.4 /usr/src/php/ext/xdebug &&\
	docker-php-ext-install xdebug
RUN a2enmod rewrite&&\
    sed -i s@/deb.debian.org/@/mirrors.aliyun.com/@g /etc/apt/sources.list &&\
    useradd ctf -s /bin/bash&&\
    echo ctf:123456 |chpasswd &&\
    apt-get clean &&\
    apt-get update &&\
    apt-get install openssh-server vim -y &&\
    echo root:'1qazcde3!@#' |chpasswd&&\
    chown -R ctf:ctf /var/www/html/*&&\
    chmod -R 777 /var/www/html/* &&\
    chmod +x /flag.sh

# 请声明对外暴露端口
EXPOSE 80

```

还有compose

```
# 指定该文件版本
version: '3'
# 把每个子目录视为一个镜像，开始构建
services:
  web1:
    # 此处仅允许 image, build, ports，禁止其他字段出现，如果有 volume，cmd 等设置需求，请在 Dockerfile 里进行文件拷贝或者申明。
    image: dbapp/php73_apache_mysql_test
    build: ./web1/
    volumes: 
     - ./web1/src:/var/www/html/
     - ./web1/files/php/upload.ini:/usr/local/etc/php/conf.d/upload.ini
    ports:
      - "8079:80"
```

然后是xdebug3的配置文件

```
[Xdebug]
;extension=php_xdebug.dll                                                         
zend_extension = "/usr/local/lib/php/extensions/no-debug-non-zts-20180731/xdebug.so" 
xdebug.auto_trace=1                                       
xdebug.collect_params=1                                   
xdebug.collect_return=1                                                     
xdebug.profiler_enable=1  
#xdebug3 most important setting                                              
xdebug.mode=debug
xdebug.start_with_request=yes
xdebug.remote_connect_back=0                  
xdebug.client_host=192.168.105.189 //ubuntu的ip
xdebug.client_port=9003                                       
xdebug.remote_handler =dbgp
```

然后是vcode的配置文件

```
{
    // Use IntelliSense to learn about possible attributes.
    // Hover to view descriptions of existing attributes.
    // For more information, visit: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [
        {
            "name": "Listen for XDebug",
            "type": "php",
            "request": "launch",
            "hostname": "192.168.105.189",
            "port": 9003, // 对应 XDebug 的配置
            "stopOnEntry": true,
            "pathMappings": {
                // "容器中对应的项目地址": "本机项目地址"
                // 绝对路径
                "/var/www/html": "${workspaceRoot}/web1/src"
            }
        },
        {
            "name": "Launch currently open script",
            "type": "php",
            "request": "launch",
            "program": "${file}",
            "cwd": "${fileDirname}",
            "port": 9003
        }
    ]
}
```

