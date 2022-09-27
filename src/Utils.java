package src;

public class Utils {
    public static boolean debug = true;
    public static<T> void log( T s ) {
        if ( debug )
            System.out.println( s );
    }
    public static void log() {
        log( "" );
    }

    public static void logno( String s ) {
        if ( debug )
            System.out.print( s );
    }
}
