# Day9

## 0x01 归档

### 1. tar归档

常用指令:

`-c` 新建归档文件

`-x`解开归档文件

`-r`向已有的归档文件添加新的文件

`-v/-vv`显示归档文件的详细信息

`-f`指定归档文件的文件名

`-t`列出归档文件中的文件名

`-z`使用gzip2压缩或者解压文件

`-A`拼接两个归档文件

:a: 通过ssh传输数据

```bash
tar cvf - files/ | ssh user@example.com "tar xv -C documents/
```

:b:根据时间戳来确定是否加入归档

```bash
tar -uvvf a.tar files
```

如果时间戳比原有文件的大 就加入

:ab: 删除其中的那文件

```bash
tar --delete --file a.tar files
```

:abc: 排除部分文件

```
tar --exclude-vcs
--exclude
```

:accept:`-a`自动检测压缩格式

`gzip` `gunzip`

`bzip` `bunzip`

`lzma` `unlzma`

解压缩文件的操作。

### 2. zip归档

`-u`更新压缩归档文件中的内容

`-d`删除

`-l`列举

### 3. 多核归档pbzip

实战演练

tar配合pbzip2

```bash
tar -c dir/ | pbzip -c >a.tar.bz2
```

`-d` 解压缩

tar.bz2 

`pbzip -dc a.tar.bz2 | tar x`

### 4. squashfs

(1). 创建

```bash
mksquashfs sources comporess.squashfs
```

(2). 利用环回形式挂载

```bash
mkdir /mnt/squash
mount -o loop comporess.squashfs /mnt/squash
```

### 5.rsync备份系统快照

`rsync -av source dest`

`-a`  归档操作

`-v`展示进度

:a:备份远程数据

```
rsync -avz user@host:PATH dir/
```

`-z`表明使用压缩

:b:删除已经不存在的数据

```bash
rsync -avz sour des --delete
```

### 6. 差异化归档

```bash
tar -czf dat-`date +%j`.tgz `find /home/user -newer`
```

