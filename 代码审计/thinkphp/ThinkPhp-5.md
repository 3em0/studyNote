---
title: ThinkPhp-5
date: 2020-09-23 20:49:09
tags: ['编程',ThinkPHP]
categories: 编程
---

# ThinkPhp-5

## 模版的条件判断标签

### switch

```
{switch number} 
{case 1}1{/case}
{case 5}5{/case}
{case 10}10{/case} 
{default/}不存在 {/switch}
```

### if

```
{if ($number > 10) and ($number < 20)}
    6666
{else if $number == 10}
    8888
{/if}
```

<!--more-->

### {present}和{notpresent}判断变量是否已经定义赋值(是否存在)； 

```
{present name='user'}
存在
{/present}
{present name='user'} 
user已存在 
{else/} 
user不存在 
{/present
```

### {empty}和{notempty}判断变量是否为空值

```
{empty name='username'}
有值
{/empty}
{empty name='username'} 
username 有值
{else/}
username 没值 
{/empty}
```

```
{defined name='PI'}
PI存在 
{else/}
PI不存在 
{/defined}
```

## 模版的加载包含输出

### 包含文件

```
{include file='public/header' title='$title' keywords='模版'/}
主体
{include file='../application/view/public/footer.html'}
```

header.html

```php+HTML
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>[title]</title>
</head>
<body>头部<br>
```

footer

```

<br>尾部</body>
</html>
```

### 输出替换

运用于调用静态文件，将路径打包

```
 在目前二级目录下，template.php 中，配置新增一个参数；
 'tpl_replace_string' => [ '__JS__' => 'static/js', '__CSS__' => 'static/css', ]
```

如果是在顶级域名下，直接在改成/static/css 即可，加一个反斜杠； 

html 文件调用端，直接通过`__CSS__(__JS__)`配置的魔术方法调用即可

```
<link rel="stylesheet" type="text/css" href="__CSS__/basic.css">
<script type="text/javascript" src="__JS__/basic.js"></script>
```

## 模版的布局和继承

### 模版的布局

开启布局功能

```
template.php
'layout_on' => true,
// 改变模版位置
  'layout_name' => 'public/layout',
  //更改__content__
  'layout_item' => '{__REPLACE__}'
```

然后模版开启

```php+HTML
{include file='public/header' title='$title' keywords='模版'/}
{__CONTENT__}//引入index.html中的内容
{include file='../application/view/public/footer.html'}
```

### 第二种打开模版

index.html

```
{layout name="public/layout" repalce='[__CONTENT__]'}
主体
```

### 第三种打开模版

在C端实现

```
$this->view->engine->layout(true);//如果不使用默认路径还是需要更改配置
```

## 模版继承

首先创建一个类似于父类的东西

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>{$title}</title>
</head>
<body>
<div>
{block name='nav'}nav{/block}
</div>
<div>
{block name='footer'} @ThinkPHP 版权所有 {/block}
</div>

</body>
</html>
```

注意其中有两个block标签，是用来进行占位的

然后进行调用 `{__block__}`这是用来引用原来的基类中的内容

```
{extend name='public/base'}
{extend name='../application/view/public/base.html'}
{block name='nav'}
<ol>
    <li>首页</li>
    <li>分类</li>
    <li>关于</li>
</ol>
{/block}
{block name='footer'}
I LOVE YOU {__block__}
{/block}
```

