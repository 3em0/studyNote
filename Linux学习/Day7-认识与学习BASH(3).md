# Day7-认识与学习BASH

## 0X01 BASH基础

### 1. shell内建属性

1. 获取字符串的长度

   ```
   length=${#var} 可以获得var的长度
   ```

2. 获取当前使用的shell

   ```
   $BASH $0
   ```

3. 检查当前运行用户

   ```bash
   if [ $UID -ne 0 ] then
   	echo NOT ROOT USER. Please run as root
   else
   	echo Root User
   fi
   ```

   或者

   ```bash
   if test $UID -ne 0 then
   	echo NOT ROOT USER. Please run as root
   else
   	echo ROOT USER
   fi
   ```

4. 修改bash的提示字符

   环境变量 : PS1 可以控制

   ```
   cat ~/.bashrc | grep PS1
   ```

   还有一些比较特殊的字符在里面可以使用

   ```
   \u 扩展为用户名
   \h 扩展为主机名
   \w 扩展为档期啊目录
   ```

### 2. 函数添加环境变量

```
prepend() { [ -d "$2" ] && eval $1=\"$2':'\$$1'\" && export $1;}
```

### 3. 数组

```
echo ${array[*]}
echo ${array[@]}
```

分别以字符的形式打印数组元素

### 4.终端操作

`stty -echo` 以密码形式获取输入 但不输出

```bash
#!/bin/bash
echo -e "Please Enter password:"
stty -echo
read password
stty echo
echo 
echo password read.
```

设计一个终端倒计时

```bash
#!/bin/bash
echo Count;
tput sc
for count in `seq 0 40`
do
	tput rc
	tput ed
	echo -n $count
	sleep 1
done
```

### 5. 调试脚本

```bash
set -x 执行时显示参数和命令
set +x 禁止调试
set -v 当命令进行读取时显示输入
set +v 禁止打印输入
```

### 6.获取输入

```
read 
-n 指定数量
-s 无回显
-p 提示信息
-t 时限
-d 特定的分隔符
```

### 7. 配置文件定制bash

登录shell: `/etc/profie` profile /bash_login

交互shell、ssh单条执行: bash.bashrc .bashrc

BASH_ENV: 设置之后才能在子shell中用别名

ssh登录 读取的配置文件

```bash
1. /etc/profile
2. /etc/bash.bashrc
3. profile
```

## 0x02 命令执行

### 1. 录制并回放终端会话

```
script -t 2> timing.log -a output.session
```

scriptreplay; 回访执行过程。

### 2.xargs

和find 结合使用 注意指定参数

```
find xxx -print0 | xargs -0 
```

### 3. md5sum

- 对文件md5

```
md5sum file > a.md5 生成
md5sum -c a.md5  快速check
```

- 对于目录进行md5

```
md5deep -r1 dir > a.md5
```

-r指定迭代深度 此处为一层

- 密码hash

  ```
  openssl password -1 -salt salt_string password
  ```

### 4. 排序

```
sort data.txt | uniq -s 2 -w 2
```

- sort

  ```
  -nrk 依据第一列 按逆序排序
  -k 2 按照第2列排序
  -d 按照字典排序
  ```

- uniq

  ```
  -c 统计数据出现次数
  -n 显示唯一行
  -d 显示重复
  -s 跳过
  -w 从当前开始多少
  ```

### 5. 分割

split 在不出网环境写数据就有大用了哦。

```
-b 指定各个文件大小
-d 使用数字后缀
-a 后缀长度
-l 指定行数
```

csplit

```
{真数} 指定分割执行的次数
-s 命令静默
-n 文件名后缀数字格式
-f 后缀的前缀
-b 指定后缀格式
```

### 6. 并行

并行计算 yyds 都是开的子shell

```
#!/bin/bash
PIDARRAY=()
for file in File1.iso File2.iso
do
	md5sum $file &
	PIDARRAY+=("$!")
done
wait ${PIDARRAY[@]}
```

### 7.树状目录图

```
cd /var/log
find . -exec sh -c 'echo -n {}| tr -d "[:alnum:]_.\-" | tr "/" " "; basename {} ' \;
```

## 0x03 文件操作

- **-**作为命令行参数 借此实现从stdin获得输入

### 1. 创建任意大小文件

   ```
   dd if=/dev/zero of=junk.data bs=1m count=1
   ```

   if: 输入文件

   of: 输出文件

   bs: 指定单位大小

   count: 被复制的块数

### 2. 删除相同文件

   ```
   #!/bin/bash
   # name: remove_duplicates.sh
   # use: remove the same
   ls -lS --time-style=long-iso | awk 'BEGIN{
   	getline; getline;
   	name1=$8; size=$5
   }
   {
   	name2=$8;
   	if(size==$5)
   {
   	"md5sum "name1 | getline; csum=$1;
   	"md5sum "name2 | getline; csum2=$1;
   	if( csum==csum2 )
   	{
   		print name1; print name2;
   	}
   };
   size=$5; name1=name2;
   }' | sort -u > duplicate_files
   
   cat duplicate_files | xargs -I {} md5sum {} | \
   sort | uniq -w 32 | awk '{ print $2 }' | \
   sort -u > unique_files
   
   echo Remving....
   comm duplicate_files unique_files -3 | tee /dev/stderr | \
   	xargs rm
   
   echo Removed duplicates files successfully.
   
   ```

### 3. 文件权限

1. 目录的粘滞位

   ```
   如果目录设置了该权限 只有创建者才能删除
   /tmp
   ```

2. 递归赋权

   **-R**

3. 将文件设置为不可修改

   chattr 设置文件的扩展属性

   ```
   chattr +i file
   chattr -i file
   ```

4. 查看文件状态

   ```bash
   !/bin/bash
   # name: filestat.sh
   if [ $# -ne 1 ];
   then
   	echo "Usage: is $0 basepath";
   	exit
   fi
   path=$1
   
   declare -A statarray;
   
   while read line;
   do
   	ftype=`file -b "$line" | cut -d, -f1`
   	let statarray["$ftype"]++;
   done < <(find $path -type f -print) #注意 这里不是<<  第一个< 是重定向 第二个是子进程输出转换
   
   echo ============ FIle types and counts =============
   for ftype in "${!statarray[@]}";
   do
   	echo $ftype: ${statarray["$ftype"]}
   done
   
   ```

5. 创建1g ext4文件并且挂载

   ```
   dd if=/dev/zero of=look.img bs=1G count=1
   mkfs.ext4 look.img
   file look.img
   mkdir /mnt/loop
   mout -o loop look.img /mnt/loop`	
   ```

   **-o loop** 指名挂载的是环回i文件 而非设备

   fdisk 标准分区工具 +

   ```
    losetup -o 32256 /dev/loop2 look.img
   ```

   ![image-20211228011414642](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211228011414642.png)

   如果需要创建分区 就必须手动擦操作了

   ```
   losetup /dev/loop1 loo.img
   fidsk /dev/loop1
   ```

   在loop.img中创建分区并挂载第一个分区

   ```
   losetup -o 32256 /dev/loop2 loopback.img
   ```

 6. 挂载ISO文件

   这样我们访问该目录 就是访问的iso文件上的数据了。

```
mkdir /a
mount -o loop linux.iso /a
```

### 4. iso文件

创建iso文件

```
dd if=/dev/cdrom of=iamge.iso
#此时将所有文件都导入对应的，目录内
mkisofs -v "Label" -o image.iso dir/
```

-v : 指定卷标的。

创建启动闪存和硬盘的混合型iso文件

```
isohybrid image.iso
dd if=image.iso of=/dev/sdb1
```

用命令行刻录iso

```
cdrecourd -v dev=/dev/cdrom image.iso -speed 8
```

弹出托盘

```
eject
eject -t 合上。
```

### 5. 文件差异

![image-20211228082710985](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211228082710985.png)

目录差异

```
diff -Naur dir1/ dir2/
-N 将缺失的文件视为空文件
-a 将所有文件视为文本文件
-u 生成一体化输出
-r 递归目录下所有文件
```

### 6. 监视文件变化

```
dmeg | tail -f
```

### 7. 音频文件

**Ubutu Studio*
