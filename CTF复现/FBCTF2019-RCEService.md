# FBCTF2019-RCEService

## 0x01 题目初现

```php
<?php
putenv('PATH=/home/rceservice/jail');

if (isset($_REQUEST['cmd'])) {
  $json = $_REQUEST['cmd'];

  if (!is_string($json)) {
    echo 'Hacking attempt detected<br/><br/>';
  } elseif (preg_match('/^.*(alias|bg|bind|break|builtin|case|cd|command|compgen|complete|continue|declare|dirs|disown|echo|enable|eval|exec|exit|export|fc|fg|getopts|hash|help|history|if|jobs|kill|let|local|logout|popd|printf|pushd|pwd|read|readonly|return|set|shift|shopt|source|suspend|test|times|trap|type|typeset|ulimit|umask|unalias|unset|until|wait|while|[\x00-\x1FA-Z0-9!#-\/;-@\[-`|~\x7F]+).*$/', $json)) {
    echo 'Hacking attempt detected<br/><br/>';
  } else {
    echo 'Attempting to run command:<br/>';
    $cmd = json_decode($json, true)['cmd'];
    if ($cmd !== NULL) {
      system($cmd);
    } else {
      echo 'Invalid input';
    }
    echo '<br/><br/>';
  }
}

?>
```

可以看到基本过滤完了所有可读取，可以操作文件的函数，也不能够反弹shell(因为BUU不出网)。

## 0x02 题目分析

**putenv** 将环境切换到这个目录下来，我们可以接着分析一波，这个目录一定是有不可名状的密码，看完答案之后的深吼（”FLAG就在这里“）。

这里如果如果不能够真唱的绕过函数的话，剩下的攻击点就只有那个正则表达式匹配的函数。

我们可以复习一下，这个函数的一些问题。

1. 这个**preg_replace**函数可能会导致任意代码的执行**/e**

2. 正则表达式的数组绕过，在匹配的时候，遇到数组会直接返回**FALSE**。只匹配一行

3. PCRE回溯次数（这个就是预期的考点

让我们来思考一波了。利用考点2，只匹配一行数据的特性，我们知道换行在url编码之后就是**%0A**,可以成功绕过吗？

![image-20210226170212561](https://i.loli.net/2021/02/26/C7q1uFGipkgd6lZ.png)

```
?cmd={%0A"cmd":"/bin/ls%20/home/rceservice"%0A}
```

很不幸，可以。这就很NICE，但是我觉得这应该是个非预期。但是记下来（preg_replace只匹配一行数据。

继续利用考点3，PCRE的回溯次数来进行绕过，灵魂一问：**这是啥??**

## 0x03 知识积累

先放上p神的文章:https://www.leavesongs.com/PENETRATION/use-pcre-backtrack-limit-to-bypass-restrict.html

从P神的文章中，截取正则表达式的含义，已经具体的回溯过程。

正则表达式是一个可以被“有限状态自动机”接受的语言类。“有限状态自动机”，其拥有有限数量的状态，每个状态可以迁移到零个或多个状态，输入字串决定执行哪个状态的迁移。而常见的正则引擎，又被细分为DFA（确定性有限状态自动机）与NFA（非确定性有限状态自动机）。他们匹配输入的过程分别是：
	DFA: 从起始状态开始，一个字符一个字符地读取输入串，并根据正则来一步步确定至下一个转移状态，直到匹配不上或走完整个输入

![image-20210226171652775](https://i.loli.net/2021/02/26/zFSgkoAKqEcQZn7.png)	这样，他在进行判断的时候，就会根据第一个满足条件的字符找到后续满足的类别，判断下步还满不满足，满足就继续走下去，不满足就截止，这样的性能会十分的高。

NFA：从起始状态开始，一个字符一个字符地读取输入串，并与正则表达式进行匹配，如果匹配不上，则进行回溯，尝试其他状态

这个回溯则是，从正则表达式本身出发，以最后小单位来进行匹配，一个一个满足，然后再进行回溯，来满足后面的正则，最后达到完全的表达式匹配的目的。

​	由于NFA的执行过程存在回溯，所以其性能会劣于DFA，但它支持更多功能。大多数程序语言都使用了NFA作为正则引擎，其中也包括PHP使用的PCRE库。

这样我们其实在使用正则表达式的过程中，就会出现一个问题，如果数据贼tm长，发包率贼tm高，这样不就会被DDOS攻击吗？。我们的聪明的开发者当然也想到了这一点，他们就对此做出了改进。限制其最大的回溯次数。

```
var_dump(ini_get('pcre.backtrack_limit'));
```

这样可以查到最大的回溯次数是多少，或则直接翻php文档。（十万次

那假如我们的回溯次数超过了最大次数会怎么样呢？那就会返回绕过这个过滤，从最直观的角度来讲，就会返回FALSE。

```
.*（）.*
一般就是慎用.*,他会匹配许多的不需要的东西，然后根据他后面的内容，再进行删除
```

这个问题其实本身还是在正则表达式本身的设置中出现了问题（指开发者写的正则表达式

p神大大的扩展时间就还有

```
if(preg_match('/SELECT.+FROM.+/is', $input)) {
    die('SQL Injection');
}
```

```
if(preg_match('/UNION.+?SELECT/is', $input)) {
    die('SQL Injection');
}
```

非贪婪模式，具体分析就看p神的文章即可。这里的毛病也就是在于奇怪的超强匹配问题。

