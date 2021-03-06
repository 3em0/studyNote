### 数据流重定向

#### 0x01 标准输入 标准输出 标准错误输出

```
1> 将标准输出到xx 覆盖
1>> 附加到xx
2> 将错误信息输出到xx 
2>> 将错误信息附加到xx
```

/dev/null 垃圾桶黑洞 可以吃掉所有信息

将错误信息和正确信息同时输出到一个文件 `2>&1`或者`&>`

< 标准输入 << 表示结束的输入字符

```
cat > catfile << "eof"
读取屏幕输出或输入的内容，并且当读到eof的时候，自动停止
```

#### 0x02 命令执行的判断依据

``` 
& 两个毫不相关的命令交替执行
cmd1&&cmd2 当cmd1执行后为正确就运行cmd2
cmd1||cmd2 $?=0或者1
```

请注意在linux下命令都是从左向右执行的，一定要注意

```
command1 && command2 || command3
```

#### 0x03 管道符

`|`

仅 能处理从前面传来的正确消息，并不能够对于错误消息具有有效的处理

后面的命令必须能够处理前一个命令的数据

cut 命令处理同一行的数据

```
cut -d "分隔字符" -f fileds 以分割字符分割，取fileds
cut -c 字符区间
```

grep

```
-a 将二进制文件以文本的形式输出
-c 计算找到字符的次数
-i 忽略大小写
-n 顺便输出行号
-v 反向选择 显示出没有字符的字符
--color=auto
```

排序命令

`sort` 

```
-f 忽略大小写
-b 忽略字母前面的空格
-M 以月份的时间排序
-n 使用纯数字
-r 反向排序
-u uniq 相同数据出一个代表
-t 分割符号
-k 选取区间
```

uniq 

如果排序完成了，但是显示的数据中，我只想将重复的数据中，有一个代表即可

```
uniq -ic 
-i 忽略大小写字符的不同
-c 进行计数
```

wc 

如果我想要知道 /etc/man_db.conf 这个文件里面有多少字？多少行

```
-l 仅列出行
-w 仅列出多少字（英文字母）
-m 多少字符
```

tee

双重重定向，既将数据输出到文件中，也将数据输出到屏幕中

```
-a 以累加的形式添加到文件中去
```

```
last | tee xxfile | cut -d " " -f1e
```

#### 0x04 字符转换命令

tr 

可以用来删除一段信息中的文字，或是进行文字信息的替换

```
-d a 删除信息中的a
-s 去除重复
tr 'a' 'A' 将其中的a转换为A
```

col 

```
-x 将tab键转换为对等的空格键
```

join

处理两个文件当中，有相同数据的那一行，将它加在一起

```
-t 默认以空格分割字符，并且对比第一个栏位
-i 忽略大小写
-1 第一个文件用哪个栏位分析
-2 第二个文件使用哪个栏位分析
```

```
join -t ':' -1 4 /etc/passwd -2 3 /etc/group | head -n 3
```

示例如上，记住所要处理的文件首先都必须经过排序

paste

直接将两行粘贴在一起，默认以tab

```
-d 后面可以解分割字符 如果file为- 则为标准输入的意识
```

expand 

就是将`tab`转换为空格键

```
-t 可以自定义一个tab键相当于多少个空格键
```

split

大文件划分成为小文件

```
-b 后面接想要划分成的文件大小
-l 以行数来进行划分
split -b 300k /etc/services services
将划分成每个300k的前缀为services的小文件
```

使用数据重定向就可以将小文件合并成为一个文件

xargs

```
-0 如果输入00
```

