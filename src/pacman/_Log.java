package pacman;

/**
 *
 * @author Falie
 */
public class _Log {
    private final static int LOG_LEVEL=10;
    public final static boolean LOG_ACTIVE=false;
    
    public static void a(String msg){
        System.out.println("LOG_ASSERT:\t"+msg);
    }
    
    public static void a(String tag, String msg){
        System.out.println("LOG_ASSERT: ["+tag+"]\t"+msg);
    }
    
    public static void e(String msg){
        if(LOG_LEVEL>=0)
            System.out.println("LOG_ERROR:\t"+msg);
    }
    
    public static void e(String tag, String msg){
        if(LOG_LEVEL>=0)
            System.out.println("LOG_ERROR: ["+tag+"]\t"+msg);
    }
    
    public static void w(String msg){
        if(LOG_LEVEL>=1)
            System.out.println("LOG_WARNING:\t"+msg);
    }
    
    public static void w(String tag, String msg){
        if(LOG_LEVEL>=1)
            System.out.println("LOG_WARNING: ["+tag+"]\t"+msg);
    }
    
    public static void i(String msg){
        if(LOG_LEVEL>=2)
            System.out.println("LOG_INFO:\t"+msg);
    }
    
    public static void i(String tag, String msg){
        if(LOG_LEVEL>=2)
            System.out.println("LOG_INFO: ["+tag+"]\t"+msg);
    }
    
    public static void d(String msg){
        if(LOG_LEVEL>=3)
            System.out.println("LOG_DEBUG:\t"+msg);
    }
    
    public static void d(String tag, String msg){
        if(LOG_LEVEL>=3)
            System.out.println("LOG_DEBUG: ["+tag+"]\t"+msg);
    }
    
    public static void v(String msg){
        if(LOG_LEVEL>=4)
            System.out.println("LOG_VERBOSE:\t"+msg);
    }
    
    public static void v(String tag, String msg){
        if(LOG_LEVEL>=4)
            System.out.println("LOG_VERBOSE: ["+tag+"]\t"+msg);
    }
}
