package learn;


public class Person {
    private  String name;
    private  int age;
    public Person(){
        System.out.println("空参构造方法======");
    }
    public Person(String name){
        System.out.println(name);
    }

    public String getName() {
        System.out.println("get方法====");
        return name;
    }

    public void setName(String name) {
        System.out.println("====set方法");
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        System.out.println("===set方法");
        this.age = age;
    }
}