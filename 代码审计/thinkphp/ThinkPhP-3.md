---
title: ThinkPhP-3
date: 2020-09-20 22:50:47
tags: ['编程',ThinkPHP]
categories: 编程
toc: true
---

### ThinkPhp-3

## 模型查询范围和输出

### 一、模型查询

```
public function scopeGenderMale($query) { 
    $query->where('gender', '男') 
    ->field('id,username,gender,email')->limit(5); 
    }
方法名必须以scope开头
调用：
$result = UserModel::scope('gendermale')->select();
//$result = UserModel::gendermale()->select(); 
```

```
public function scopeEmailLike($query, $value) 
{ $query->where('email', 'like', '%'.$value.'%'); }
public function scopePriceGreater($query, $value)
{ $query->where('price', '>', 80); }
$result = UserModel::emailLike('xiao')->priceGreater(80) ->select();
```

**只能使用 find()和 select()两种方法**

全局范围查询

```
protected function base($query) { $query->where('status', 1); }
```

开关

```
UserModel::useGlobalScope(false)
```

### 二、模型输出

<!--more-->

```
public function view() { 
$user = UserModel::get(21); 
$this->assign('user', $user); 
return $this->fetch(); }
```

```
3. 使用 toArray()方法，将对象按照数组的方式输出； 
$user = UserModel::get(21); print_r($user->toArray());
4. 和之前的数据集一样，它也支持 hidden、append、visible 等方法； 
print_r($user->hidden(['password,update_time'])->toArray());
5. toArray()方法也支持 all()和 select()等列表数据； 
print_r(UserModel::select()->toArray());
6. 使用 toJson()方法将数据对象进行序列化操作，也支持 hidden 等方法； 
print_r($user->toJson());
```

## JSON

### 写入

```
$data = [ 'username' => '辉夜', 'password' => '123', 'gender' => '女', 'email' => 'huiye@163.com', 'price' => 90, 'details' => '123', 'uid' => 1011, 'status' => 1, 'list' => ['username'=>'辉夜', 'gender'=>'女', 'email'=>'huiye@163.com'], ];
Db::name('user')->insert($data);
```

将json数据写入文本类型，可以转换

```
$user = Db::name('user')->json(['list','details']) ->where('id', 173)->find()
```

照样可以输出json数据格式

### 按照json数据段中的数据进行查找

```
$user = Db::name('user')->json(['list','details'])-> 
where('list->username', '辉夜')->find();
```

完全修改

```
如果想完全修改 json 数据，可以使用如下的方式实现： 
$data['list'] = ['username'=>'李白', 'gender'=>'男']; Db::name('user')->json(['list']) ->where('id', 174)->update($data);

```

部分修改

```
$data['list->username'] = '李黑';
Db::name('user')->json(['list']) ->where('id', 174)->update($data);
```

### 模型json

```
protected $json = ['details', 'list'];
```

对象

```
$list = new \StdClass();
$list->username = '辉夜';
$list->gender = '女'; 
$list->email = 'huiye@163.com';
$list->uid = 1011; 
$user->list = $list;
```

通过 json 的数据查询，获取一条数据；

 `$user = UserModel::where('list->username', '辉夜')->find(); return $user->list->email;`

 更新修改 json 数据，直接通过对象方式即可； 

`$user = UserModel::get(179); $user->list->username = '李白'; $user->save()`

## 软删除

软删除：并不是真的将数据库中的数据进行删除，而是打上标签

### 数据库软删除

我们需要在数据表创建一个 delete_time，默认为 NULL

```
Db::name('user')->where('id', 192) ->
useSoftDelete('delete_time', date('Y-m-d H:i:s'))# 一个参数是时间，一个是当前时间
->delete(); 
return Db::getLastSql(); 执行的真实语句是update
```

### 模型段数据删除

```
模型端
use SoftDelete;
protected $deleteTime = 'delete_time';
自动在所有的数据查询中加选择语句
```

```
$user = UserModel::withTrashed()->select();  取消屏蔽
$user = UserModel::onlyTrashed()->select(); 只查看屏蔽
```

恢复

```
$user = UserModel::onlyTrashed()->find();
$user->restore()
```

真正的删除（要先找到，再删除）

```
$user = UserModel::onlyTrashed()->get(193); $user->restore(); $user->delete(true);
```

