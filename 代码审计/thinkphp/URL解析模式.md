---
title: ThinkPhP学习第一天
date: 2020-09-17 19:50:05
tags: ['编程',ThinkPHP]
categories: 编程
toc: true
---

# ThinkPhP学习-第一天

## 环境配置

.htaceess

```php
<IfModule mod_rewrite.c>
  Options +FollowSymlinks -Multiviews
  RewriteEngine On

  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteRule ^(.*)$ index.php [L,E=PATH_INFO:$1]
</IfModule>
```

要改成这样，并且打开伪静态模块

<!--more-->

## 学习开始

## URL解析模式

`http://serverName/index.php/模块/控制器/操作/参数/值…`

首先这就是一个正常的URL模式的解析示例

index.php => 入口文件

模块 => application 目录下的一个可自定义的目录(默认index)

控制器=>在 index 目录下有一个 controller 控制器目录的 Index.php 控制器(**Index.php 控制器的类名也必须是 class Index)**

操作 =》 就是类的一个方法

### 示例

```php
<?php namespace app\test\controller;
class Abc { public function eat($who = '隔壁老王') { return $who.'吃饭！'; } }
```

将以上代码复制到application目录下的test目录下的controller目录下的Abc.php（控制器）

则 url就是 ：`public/index.php/test/abc/eat/who/主人老李`

## 模块设计

### 一.目录架构

1.thinkphp多模块，单模块均支持，通过`app_multi_module`这个参数来调节

2.手册上的目录架构

![](https://i.loli.net/2020/09/17/78251DvgTrGRwdn.png)

3.命令空间统一：app\模块名  eg：：`app\index\controller\Index` 

4.多模块访问必须指定模块，单模块则不用

5.绑定了模块，然后绑定了控制器

`	Container::get('app')->bind('test/abc')->run()->send();`

### 二、空模块

`'empty_module'`

 可以通过环境变量设置空目录，将不存在的目录统一指向指定目录； 

### 三、单一模块

命名空间改为`app/controller`

目录架构改为:

![](https://i.loli.net/2020/09/17/7TpNWY64n3eIMZE.png)

### 四、环境变量

ThinkPHP5.1 提供了一个类库 Env 来获取环境变量； `return Env::get('app_path');`

![](https://i.loli.net/2020/09/17/Nr3xbTnHoFpOfdk.png)



## 控制器定义

### 一．控制器定义

1. 控制器，即 controller，控制器文件存放在 controller 目录下；
2.  类名和文件名大小写保持一致，并采用驼峰式（首字母大写）； use think\Controller; class Index extends Controller
3. 继承控制器基类，可以更方便使用功能，但不是必须的；
4.  系统也提供了其它方式，在不继承的情况下完成相同功能； 
5. 前面我们知道如果是一个单词，首字母大写，比如 class Index；
6.  URL 访问时直接 public/index 即可； 
7. 那么如果创建的是双字母组合，比如 class HelloWorld； 
8. URL 访问时必须为：public/hello_world； 
9. 如果你想原样的方式访问 URL，则需要关闭配置文件中自动转换； 'url_convert' => false,
10. 此时，URL 访问可以为：public/HelloWorld； 
11. 如果你想改变根命名空间 app 为其它，可以在根目录下创建.env 文件； 
12. 后写上配对的键值对即可，app_namespace=application

### 二、渲染输出

ThinkPHP 直接采用方法内 return 返回的方式直接就输出了； 

 使用 json 输出，直接采用 json 函数； 

`$data = array('a'=>1, 'b'=>2, 'c'=>3); return json($data);`

使用 view 输出模版，开启错误提示，可知道如何创建模版； 

return view();

默认输出方式为 html 格式输出，如果返回的是数组，则会报错；

 可以更改配置文件里的默认输出类型，更改为 json； return ['user'=>'Lee', 'age'=>100]; 'default_return_type' => 'json',

一般来说，正常页面都是 html 输出，用于模版，AJAX 默认为 json； 

如果继承了基类控制器，那么可以定义控制器初始化方法：initialize()； 

initialize()方法会在调用控制器方法之前执行； protected function initialize() { //parent::initialize(); echo 'init'; }

initialize()方法不需要任何返回值，输出用 PHP 方式，return 无效；

##  控制器的编写

### 一、前置操作

1. 继承 Controller 类后可以设置一个$beforeActionList 属性来创建前置方法；

   ```php
   protected $beforeActionList=[
           'first',
           //one 方法不执行
           'second' => ['except'=>'one'],
           //指定调用
           'third' => ['only'=>'one'],
       ];
   ```

   **要继承**

2. 此时，我们可以分别 URL 访问不同的方法来理解前置的触发执行；

### 二、跳转和重定向

1.Controller 提供两个跳转方法 success(msg,url) 和error(msg);

```
if($this->flag)
        {
            //不指定，就返回refer
            $this->success('注册成功','../');
        }else{
            $this->error("失败");
        }
```

2.成功或错误有一个固定的页面模版：'thinkphp/tpl/dispatch_jump.tpl'； 

3.在 app.php 配置文件中，我们可以更改自己个性化的跳转页面

```
'dispatch_success_tmpl' => Env::get('think_path') 
'tpl/dispatch_jump.tpl', 'dispatch_error_tmpl' => Env::get('think_path') . 'tpl/dispatch_jump.tpl',
```

4.环境变量

![](https://i.loli.net/2020/09/17/7QtCDzHNShp3MfU.png)

### 二、空方法和空控制器

1.不存在的方法

```
//空方法拦截
    public function _empty($name)
    {
        return $name;
    }
```

2.不存在控制器

```
class Error
{
    public function index(Request $request)
    {
        return '此控制器不存在'.$request->controller();
    }
}
```

3.默认空控制器

```
// 默认的空控制器名
    'empty_controller'       => 'Error',
```

## 数据库与模型

### 一、连接数据库（PDO模式）

`config`下的`database.php`可以配置链接数据

具体配置属性到配置文件查看

###　二、开始使用

```
public  function getNoModelData()
    {
       //$data = Db::table('tp_user')->select();
        $data = Db::name('user')->select();
        return json($data);
    }
```

**注意区别**

### 三、模型定义

模型：处理数据库相关模型

```
<?php
namespace app\model;


use think\Model;

class User extends Model
{

}
```

应用trace 查看原生的SQL；

## 查询数据

```
Db::table('tp_user')->find();//查询出一条数据
Db::getLastSql();//查询上一次使用的查询语句是什么
$data = Db::table('tp_user')->where('id',27)->find(); 指定查询，链式
无返回值，就是null
 $data = Db::table('tp_user')->where('id',127)->findOrFail(); 查找不到就抛出异常
 Db::table('tp_user')->where('id',127)->findOrEmpty(); 查询不到就返回空数组
 $data = Db::table('tp_user')->select(); 查询多列数据
 $data = Db::table('tp_user')->where('id',27)->selectOrFail();
 Db::name()不需要前缀
```

助手函数

```
\db助手函数
Db::name('user')->value('username');指定字段的值
Db::name('user')->column('username','id');一列的值，并且用id作为索引，返回一个array
```

数据分批处理和大数据处理和json数据处理，具体问题具体分析

## 链式查询

### 一.查询规则

1.`->`多次调用

2`.find(),select()`结束查询

3.有多少种链式查询规则呢？

[就是我]: https://www.kancloud.cn/manual/thinkphp5_1/354005	"点击我查看"

### 二、多次查询

```
$user = Db::name('user');
        $data1 = $user->where('id',27)->select();
        $data2 = $user->removeOption('where')->select();
        return json($data2);
```

## 增删改操作

### 一.新增数据

`insert()`

```sql
$data = [ 'username' => '辉夜', 'password' => '123', 'gender' => '女', 'email' => 'huiye@163.com', 'price' => 90, 'details' => '123', 'create_time' => date('Y-m-d H:i:s') ]
Db::name('user')->insert($data);
Db::name('user')->data($data)->insert();
```

会返回一个影响行数

**mysql支持replace写入**

```
Db::name('user')->insert($data,true);唯一确定标识的时候，就会把原来的参数删除掉，然后新建
```

批量写入

```
Db::name('user')->insertAll($dataAll);二维数组，其余相同
```

### 二.修改数据

```
$data = [
            'username' => '李白',
        ];
Db::name('user')->where('id',239)->update($data);
```

```
Db::name('user')->where('id', 38)-> data($data)->update(['password'=>'456']);
```

如果有唯一性主键

```
$data = [ 'username' => '李白', 'id' => 38 ]; Db::name('user')->update($data);
```

```
Db::name('user')->inc('price')->update($data); #自增加1
```

```
Db::name('user')->exp('email','UPPER(email)')->update($data);让data中指定的数据的email字段变成大写
```

```
$data = [ 'username' => '李白', 'email' => Db::raw('UPPER(email)'), 'price' => Db::raw('price - 3'), 'id' => 38 ];
Db::name('user')->update($data);
raw方法更新
```

如果只想改其中的一个值

```
Db::name('user')->where('id', 38)->setField('username', '辉夜');
Db::name('user')->where('id', 38)->setInc('price'); setDec()减值，后面可加步长
```

### 三.删除数据

1. 极简删除可以根据主键直接删除，删除成功返回影响行数，否则 0； Db::name('user')->delete(51);
2. 根据主键，还可以删除多条记录； Db::name('user')->delete([48,49,50]);
3. 正常情况下，通过 where()方法来删除； Db::name('user')->where('id', 47)->delete();
4. 通过 true 参数删除数据表所有数据，我还没测试，大家自行测试下； Db::name('user')->delete(true);



## 查询表达式

### 一、比较查询

 where(字段名,查询条件)，where(字段名,表达式,查询条件)； 

 其中，表达式不区分大小写，包括了比较、区间和时间三种类型的查询

使用<>、>、<、>=、<=可以筛选出各种符合比较值的数据列表； 

### 二、区间查询

1.使用like表达式去查询

```
Db::name('user')->where('email','like','xiao%')->select();
数组传递，模糊查询
Db::name('user')->where('email','like',['xiao%','wu%'],'or')->select();注意or
```

2.模版查询

```
Db::name('user')->whereLike('email','xiao%')->select();
Db::name('user')->whereNotLike('email','xiao%')->select();
```

3.between表达式

```
Db::name('user')->where('id','between','19,25')->select(); 
Db::name('user')->where('id','between',[19, 25])->select();
Db::name('user')->whereBetween('id',[19, 25])->select(); 
Db::name('user')->whereNotBetween('id',[19, 25])->select();
```

4.in

```
Db::name('user')->where('id','in', '19,21,29')->select(); 
Db::name('user')->whereIn('id','19,21,29')->select(); 
Db::name('user')->whereNotIn('id','19,21,29')->select();
```

5.null

```
Db::name('user')->where('uid','null')->select(); 
Db::name('user')->where('uid','not null')->select();
Db::name('user')->whereNull('uid')->select(); 
Db::name('user')->whereNotNull('uid')->select();
```



### 三、自定义

```
Db::name('user')->where('id','exp','IN (19,21,25)')->select();
Db::name('user')->whereExp('id','IN (19,21,25)')->select();
```



## 时间查询

### 一、传统方式

```
有time才会自动填充时间
$result = Db::name('user')->where('create_time','> time','2018-1-1')->select();
between查询
Db::name('user')->where('create_time','between time',['2018-1-1','2019-12-1'])->select();
```

### 二、快捷方式

```
wheretime()
$result = Db::name('user')->whereTime('create_time','between',['2018-1-1','2019-12-1'])->select();
默认是大于符号
whereBetweenTime()
Db::name('user')->whereBetweenTime('create_time','2018-1-1','2019-12-1')->select()如果只有一个参数就是当时的一天

```

### 三、固定查询

|  last week   | 上周 |
| :----------: | ---- |
| month 或者 m | 本月 |
|  last month  | 上月 |
|  year 或 y   | 今年 |
|  last year   | 去年 |
|    today     | 今天 |

```
$result = Db::name('user')->whereTime('create_time','y')->select();
```

### 四、其他查询

1.查询指定时间的数据，

```
$result = Db::name('user')->whereTime('create_time','-2 hour')->select();
```

2.查询两个字段的数据有效期

```
$result = Db::name('user')->whereBetweenTimeField('create_time','create_time')->select();
```

`return Db::getLastSql();`