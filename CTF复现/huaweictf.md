---
title: huaweictf
date: 2020-12-20 23:19:22
tags:
---

# huawei 划水CTF大赛

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

SSTI的奇怪绕过姿势

https://blog.csdn.net/solitudi/article/details/107752717

SSRF

ip限制的绕过：https://www.cnblogs.com/W4nder/p/14078695.html

nodejs中对引号的绕过：https://www.cnblogs.com/W4nder/p/14078695.html

payload

```
1.单引号二次编码一下绕
2.%EF%BC%87 这个解出来就是单引号
```

