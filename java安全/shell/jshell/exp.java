import jdk.jshell.JShell;
// jdk9 以后使用
//
//public class Jshellexp {
//    public static void main(String[] args) {
//        Jshell("calc");
//    }
//    public static void Jshell(String cmd){
//        try {
//            JShell.builder().build().eval(cmd);
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//    }
//}