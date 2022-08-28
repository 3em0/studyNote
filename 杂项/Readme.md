# 杂项

> 记录一下不知道怎么分区的东西。

# 2022

- 2022/08/25 [php==>攻击`new $a($b)`](https://swarm.ptsecurity.com/exploiting-arbitrary-object-instantiations/)

  > CTF: https://github.com/AFKL-CUIT/CTF-Challenges/tree/master/CISCN/2022/backdoor
  >
  > 发现可利用类的方法: include&& autoload && build-in(get_declared_classes())
  >
  > 触发__sleep方法：session的序列化和反序列化。
  >
  > payload: imagick

- 2022/08/28  [HTTP/2 Server Push 机制 XSS 到其他域](https://blog.zeddyu.info/2022/08/16/2022-08-10-h2-push/ )
