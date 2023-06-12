# Springcloud

> y4tacker: https://github.com/Y4tacker/JavaSec#11spring

## CVE-2021-22053

> 三梦师傅：[CVE-2021-22053: Spring Cloud Netflix Dashboard template resolution vulnerability](https://github.com/SecCoder-Security-Lab/spring-cloud-netflix-hystrix-dashboard-cve-2021-22053)
>
> 收获一个好用工具: cafecompare : jar包源码的diff工具

这里对这个漏洞就不展开分析了，因为本身其实也没有必要。是`Hystrix`微服务的一个处理问题，`Thymeleaf`注入。

## CVE-2022-22947

> y4师傅：[CVE-2022-22947 SpringCloudGateWay 远程代码执行](https://github.com/Y4tacker/JavaSec/blob/main/11.Spring/CVE-2022-22947 SpringCloudGateWay 远程代码执行/index.md)
>
> y4er师傅: [CVE-2022-22947](https://y4er.com/posts/cve-2022-22947-springcloud-gateway-spel-rce-echo-response)

首先查看`commit`:https://github.com/spring-cloud/spring-cloud-gateway/commit/337cef276bfd8c59fb421bfe7377a9e19c68fe1e。发现了其修改的关键点

![image-20221023173140502](C:\Users\79475\AppData\Roaming\Typora\typora-user-images\image-20221023173140502.png)

## CVE-2022-22965

> 先知：https://xz.aliyun.com/t/11129

## CVE-2022-22980 

