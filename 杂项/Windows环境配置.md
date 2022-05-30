# Windows下的环境配置

记录两个比较常用的命令; 

左右拆分: <kbd>alt</kbd>+<kbd>shift</kbd>+<kbd>=</kbd>

上下拆分: <kbd>alt</kbd>+<kbd>shift</kbd>+<kbd>-</kbd>

## wsl配置

> 1. zsh配置 https://zhuanlan.zhihu.com/p/227929716
> 2. zsh配置 http://www.oopswow.com/2021/01/06/WSL2-install-ubuntu20-and-change-the-default-installation-driver/
> 3. 迁移wsl的安装路径 https://www.cnblogs.com/konghuanxi/p/14731846.html

pyenv的安装

> 1. `port PYENV_ROOT="$HOME/.pyenv"
>    export PYENV_ROOT="$HOME/.pyenv"
>    export PATH="$PATH:$PYENV_ROOT/bin"
>    eval "$(pyenv init --path)"`
> 2. `sudo apt-get update; sudo apt-get install make build-essential libssl-dev zlib1g-dev \
>    libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
>    libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev`

主题安装

> https://www.jianshu.com/p/4b2b7074d9e2

与Virtual冲突

> https://blog.csdn.net/qq_36992069/article/details/104750248



## VMware配置

> 1. smb配置https://blog.csdn.net/lintao8613/article/details/79422977

sudo apt-get install samba samba-common

sudo smbpasswd -a kali

> 必须添加已经存在的用户

smb配置

```
[share]
path = /
available = yes  
browsable = yes    
public    = yes    
writable  = yes
force user = root 
```

