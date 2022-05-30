# 用IDEA调试tomcat

> 参考:https://wx.zsxq.com/dweb2/index/search/tomcat
>
> 这里p神介绍了几个方法，但是我们作为二道贩子，就演示一下其中最简单的就可以了。
>
> tomcat官网:

这里就以p神的一个小项目作为演示:https://github.com/phith0n/JavaThings/blob/master/shirodemo

创建的时候，使用`maven-archetype-webapp`

![image-20220401095043402](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401095043402.png)

下载tomcat

![image-20220401095137195](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401095137195.png)

导入依赖

![image-20220401095352753](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401095352753.png)

然后添加一个调试配置

![image-20220401095423005](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401095423005.png)

![image-20220401095528015](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401095528015.png)

![image-20220401095537212](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401095537212.png)

`我们选择war exploded。其实前者是说将本地项目打包成war，部署到Tomcat中；后者是直接将war 解压后的目录部署到Tomcat中，用哪个都可以。`

![image-20220401095959071](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401095959071.png)

然后现在就可以调试了，但是现在如果跟进到tomcat中是反汇编的代码，不带劲，我们可以导入`tomcat`的源码，这样就可以看到注释了。

![image-20220401100114927](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401100114927.png)

![image-20220401100205867](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401100205867.png)

然后我们发现呀，是源码和反编译的字节码不能对应上：然后就要加下下面这个tomcat-deployer的中的`juli.jar`.

![image-20220401100240100](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220401100240100.png)

然后就大功告成了，成功了。