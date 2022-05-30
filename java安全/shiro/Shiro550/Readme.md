# Shiro550 

>具体关于shiro： https://www.yuque.com/tianxiadamutou/zcfd4v/op3c7v

经典漏洞了 hvv的老熟人**CVE-2016-4437**

简易环境:`Authentication 和 Authorization`

```
Subject：代表当前的用户
SecurityManager：管理者所有的 Subject ，在官方文档中描述其为 Shiro 架构的核心
Realms：SecurityManager的认证和授权需要使用Realm，Realm负责获取用户的权限和角色等信息，再返回给SecurityManager来进行判断，在配置 Shiro 的时候，我们必须指定至少一个Realm 来实现认证（authentication）和/或授权（authorization）
```

## 漏洞原理

Shiro的加密是aes的，且key在源码中是写死的，所以我们可以通过key伪造任意反序列化数据

## 漏洞利用

![image-20220112195906734](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220112195906734.png)

先使用test目录下的CC11生成cc11的利用链子，然后再使用AEC那个类生成加密后的数据，发送就可以触发。这里使用CC1而不适用cc11，这是为什么呢? p神的java安全漫谈。

## 漏洞分析

首先通过函数`getRememberedSerializedIdentity`从请求中获取`remember`的值，然后使用`convertBytesToPrincipals`解析

![image-20220112200553757](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220112200553757.png)

解密然后反序列化

![image-20220112200616227](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220112200616227.png)

![image-20220112200644142](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220112200644142.png)

中间就是cc11链子的时刻了。

## 漏洞检测

看到师傅的骚骚的检测方法:http://www.lmxspace.com/2020/08/24/%E4%B8%80%E7%A7%8D%E5%8F%A6%E7%B1%BB%E7%9A%84shiro%E6%A3%80%E6%B5%8B%E6%96%B9%E5%BC%8F/

虽然已经是2020年的文章，但是今天看起来依然很舒服。

:a:常用的方法

- urldns
- cc盲打

:b:不常用

![image-20220112204314813](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220112204314813.png)

主要逻辑就在里面，如果解密失误或者序列化的数据不是想要的，那么这里就会捕捉这个异常，并且在回显中加上deleteme。

![image-20220112204347818](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220112204347818.png)

写exp: 也就是我只要加上Pri这个类的序列化，那么只有密码错误的时候才会出现deleteme了。

