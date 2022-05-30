---
title: ThinkPhp-4
date: 2020-09-21 20:00:18
tags: ['编程',ThinkPHP]
categories: 编程
---

# ThinkPhp - 第4天

## 模版引擎和视图渲染(V)

模版定位

```
		//指定模版
        return $this->fetch('edit');#当前模块/view/当前控制器名(小写)/当前操作(小写).html
        //指定目录下的模版
        return $this->fetch('public/edit');
        //指定模块下的模版
        return $this->fetch('admin@public/edit');
        //view_path 下的模版
        return $this->fetch('/edit');
        //助手函数view()
        return view('')
```

## 视图赋值和过滤

<!--more-->

### 一、视图赋值

```
//助手函数view()
//        return view('');
//        $this->assign('name','ThinkPHP');
        $this->assign([
            "name" => "Think",
            "word" => "PHP",
        ]);
        return $this->fetch();
```

```
//        $content = '{$name}.{$word}';
//        return $this->display($content,[
//            "name" => "Think",
//            "word" => "PHP",
//        ]);

//        return view('index',[
//            "name" => "Think",
//            "word" => "PHP",
//        ]);
        //全局变量的赋值
        \think\facade\View::share('name','value');
        return $this->fetch('index');
```

### 二、视图过滤

```
$this->assign([
            "name" => "Think1",
            "word" => "PHP",
        ]);
        return $this->filter(function ($value){
            return str_replace("1",'<br/>',$value);
        })->fetch();
```

```
  //全局过滤
        return $this->filter(function($content){ return str_replace("1",'<br/>',$content); });
```

## 模版变量输出

### 变量输出

```
数组输出
$data['username'] = '辉夜';
        $data['email'] = 'huiye@163.com';
        $this->assign('user', $data);
        return $this->fetch();
        模版调用
        {$user.username}.{$user.email}
对象输出
```

```
对象输出
$obj = new \stdClass(); 
$obj->username = '辉夜'; 
$obj->email = 'huiye@163.com';
$this->assign('obj', $obj);
模版调用：{$obj->username}.{$obj->email} 
```

### 其他输出

```
默认值
{$user.username|default='没有用户名'}
系统变量
使用$Think.xxx.yyy 方式，可以输出系统的变量
常量输出
{$Think.const.PHP_VERSION} {$Think.PHP_VERSION}
```

**系统配置** `{$Think.config.default_return_type}`

## 模版中的函数与运算符

### 使用函数

```
{$name|md5} #管道符
#系统默认在编译的会采用 htmlentities 过滤函数防止 XSS 跨站脚本攻击； 
可以更改默认函数：：'default_filter' => 'htmlspecialchars'
{$user['email']|raw} 不进行实体转换
```

|  函数   |              说明               |
| :-----: | :-----------------------------: |
|  date   | 格式化时间{$time\|date='Y-m-d'} |
| format  |      格式化字符串{$number       |
|  upper  |           转换为大写            |
|  lower  |           转换为小写            |
|  first  |      输出数组的第一个元素       |
|  last   |     输出数组的最后一个元素      |
| default |             默认值              |
|   raw   |           不使用转义            |

```
$this->assign('time', time()); 
{$time|date='Y-m-d'}

#如果函数有多个参数，可以用逗号隔开
{$name|substr=0,3}
#多个函数同时执行
使用|隔开，并且左到右执行
#PHP语法模式
{:substr(strtoupper(md5($password)), 0, 3)}
```

### 运算符

```
1.四则运算
{$number + $number}
2.有运算时，函数不能使用
3.三元运算
	{$name ? '正确' : '错误'} //$name 为 true 返回正确，否则返回错误
    {$name ?= '真'} //$name 为 true 返回真 
    {$Think.get.name ?? '不存在'} //??用于系统变量，没有值时输出 
    {$name ?: '不存在'} //?:用于普通变量，没有值时输出
4. 三元运算符也支持运算后返回布尔值判断； {$a == $b ? '真' : '假'}
```

注意 **??** 时系统变量

## 循环标签

foreach循环

```
$list = UserModel::all();
$this->assign('list', $list);
return $this->fetch('user');

#下面是模版调用
{foreach $list as $key=>$obj} 
	{$key}.{$obj.id}.{$obj.username}({$obj.gender}) .{$obj.email}<br> 
{/foreach}
其中$list （ :model('user')->all() ）是控制前端传递的数据集，$key 是 index 索引，$obj 是数据对象； 
```

volist 循环

```
{volist name='list' id='obj'}
	{$key}.{$obj.id}.{$obj.username}({$obj.gender}) .{$obj.email}<br>
{/volist}
 volist 中的 name 属性表示数据总集，id 属性表示当前循环的数据单条集
 使用 offset 属性和 length 属性从第 4 条开始显示 5 条，这里下标从 0 开始； （{volist name='list' id='obj' offset='3' length='5'} ）
 通过编译文件可以理解，mod=2 表示索引除以 2 得到的余数是否等于 0 或 1； 
 使用 empty 属性，可以当没有任何数据的时候，实现输出指定的提示； 
```

for 循环

```
{for start='1' end='100' comparison='<' step='2' name='i'}
	{$i} 
{/for}
```

## 模版的比较和定义标签

### 比较标签

```
{eq name='username' value='Mr.Lee'}
李先生
{else/} 
王先生
{/eq}
   相当于$username=='Mr.Lee' 然后就数出李先生，不相等就是王先生。
   属性 name 里是一个变量，$符号可加可不加；而 value 里是一个字符串,如果value中是一个变量值的话，就必须加$符号
   {equal}效果相同
```

不同标签表示不同的比较方式

```
{gt}(>)、{egt}(>=)、{lt}(<)、{elt}(<=)、{heq}(===)和{nheq}(!==)； 
```

统一使用

```
{compare name='username' value='Mr.Lee' type='eq'}
	两个值相等 
{/compare}
输入到type就可以了
```

### 定义标签

```
1.定义变量
{assign name='var' value='123'} //也支持变量 value='$name' 
{$var}
2.原生编码
{php}
 echo "Hello";
{/php}
中间不能使用模版函数，包括标签·语法
3.支持嵌套功能标签
{foreach $list as $key=>$obj} 
	{eq name='obj.username' value='樱桃小丸子'} 
		{$key}.{$obj.id}.{$obj.username}({$obj.gender}) .{$obj.email}<br> 
		{/eq} 
{/foreach}
```

