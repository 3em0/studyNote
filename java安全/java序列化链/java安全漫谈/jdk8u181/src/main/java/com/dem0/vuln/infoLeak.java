package com.dem0.vuln;

import com.dem0.internal.ReflectUtils;
import de.qtc.rmg.networking.RMIRegistryEndpoint;
import de.qtc.rmg.plugin.PluginSystem;
import de.qtc.rmg.utils.RemoteObjectWrapper;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class infoLeak {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("192.168.59.1", 1099);
//            System.out.println(registry.list());
            ReflectUtils.enableCustomRMIClassLoader();
            PluginSystem.init(null);
            RMIRegistryEndpoint rmiRegistry = new RMIRegistryEndpoint("192.168.59.1", 1099);
//            Remote[] remoteObjList = rmiRegistry.packup(registry.list());
            RemoteObjectWrapper[] rows = rmiRegistry.lookup(registry.list());
            for ( RemoteObjectWrapper row: rows) {
                System.out.println(row.className +"\tport:" +  row.endpoint.getPort());
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
    }
}
