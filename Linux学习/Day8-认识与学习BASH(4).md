# Day7-认识与学习BASH

## 0x04 让文件飞

1. cut切分文件

   ```bash
   cut -f filed_name filename
   ```

   ```bash
   cut -f 3 --complement studentdata.txt
   ```

   -f : 指定列数

   `--complement` 将没有选中的列显示出来

   ```bash
   cat studentdata.txt | tr "\t" ";" | cut -f 3 -d ";"
   ```

   ```
   cat studentdata.txt | tr -d  "\t"  | cut -c1-2,3-4 --output-delimiter=";"
   ```

   `-c` 从字符数来进行分割

2. sed替换文件

   ```bash
   cat /etc/passwd | cut -d : -f1,3 | sed 's/:/ - UID: /' 
   ```

   `-i` 用修改后的数据替换原始文件

   `g`对每行进行全局匹配 `/#g`表示第几次匹配

   `d`直接进行删除，不替换

   `&` 表示已经匹配到的字符

3. awk 命令高级文本处理

   简单使用

   ```bash
   echo | awk '{ var1 ="v1";var2="v2";var3="v3";\                    
   print var1 "-"  var2 "-" var3;
   }
   '
   ```

   特殊变量

   ```
   NR 记录编号=相当于行号
   NF 字段数量
   $0 包含当前记录的文本内容
   $1 第一个字段的文本内容
   $2 第二个字段的文本内容
   ```

4. 统计特定文件的词频

   ```word_freq.sh
   #!/bin/bash
   #name: word_freq.sh
   #to : 
   
   if [ $# -ne 1 ];
   then
   	echo "Usage: $0 filename";
   	exit -1;
   fi
   
   filename=$1;
   egrep -o "\b[[:alpha:]]+\b" $filename | tr [A-Z] [a-z] |  \
   	awk '{ count[$0]++ }
   		END{ printf("%-14s%s\n","World","Count");
   		for(ind in count)
   			{
   				printf("%-14s%d\n",ind,count[ind]);
   			}
   	}' | sort
   
   ```

5. 按列合并两个文件

   ```
   paste file1 file2
   ```

6. 打印指定行或模式之间的文本

   ```
   awk 'NR=M,NR=N' filename
   awk '/pattern/,/pattern/' filename
   ```

   打印位于这两个模式之间的数据

7. 逆序

   ```
   seq 5 | tac
   ```

   awk实现

   ```
   seq 5| awk '{ lifo[NR]=$0 } \
   > END { for(lno=NR;lno>-1;lno--){ print lifo[lno]; }
   ```

8. 练习题

   ```
   1、统计出/etc/passwd文件中其默认shell为非/sbin/nologin的用户个数，并将用户都显示出来
   cat /etc/passwd | grep -v "/sbin/nologin" | cut -d :  -f1
   2. 查出用户UID最大值的用户名、UID及shell类型
   cat /etc/passwd | cut -d : -f1,3,7 | sort -t : -k 2 -n | tail -n 1
   ```


## 0x05 WEB

```
wget --mirror --convert-links example.com 
```

下载镜像 

```bash
#!/bin/bash
# name: img_downlaod.sh

if [ $# -ne 3 ];
then
	echo "Usage: $0 URL -d DIR";
	exit -1;
fi

while [ $# -gt 0 ]
do
	case $1 in
	-d) shift; directory=$1; shift ;;
	*) url=$1; shift;;
esac
done

mkdir -p $directory;
baseurl=$(echo $url | grep -o "https?://[a-z.\-]+")

echo Downloading $url;

curl -s $url | egrep -o "<img[^>]*src=[^>]*>" | \
	sed 's/<img[^>]*src=\"\([^"]*\).*/\1/g' | \
	sed "s,^/,$baseurl/," > /tmp/$$.list

cd $directory;

while read filename
do
	echo Download $filename;
	curl -s -O "$filename" --silent
done < /tmp/$$.list
```

```
todo:
1 命令行的字典
2 
```

