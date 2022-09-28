package src;

import java.util.ArrayList;
import java.util.List;

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

    public static<T> List<T> toList( T el ) {
        List<T> list = new ArrayList<T>();
        list.add( el );
        return list;
    }

    public static void lines( int n ) {
        log( "\n".repeat( n ) );
    }
}
