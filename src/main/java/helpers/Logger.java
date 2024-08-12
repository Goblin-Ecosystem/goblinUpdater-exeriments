package helpers;

public class Logger {
    public static void error(String msg){
        System.out.println("[error] "+msg);
    }

    public static void fatal(String msg){
        System.out.println("[fatal] "+msg);
    }

    public static void warn(String msg){
        System.out.println("[warning] "+msg);
    }

    public static void info(String msg){
        System.out.println("[info] "+msg);
    }
}
