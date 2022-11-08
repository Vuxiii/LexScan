import src.*;

import java.util.Scanner;

public class App {
    public static void main( String[] args ) {
        init( args );

        Regex reg = new Regex( "123" );

        reg.match()

        // DFA_state dfa = reg_sample();

        // testRun( dfa );

    }

    /**
     * Sets up logging functionality
     * @param args from stdin
     */
    public static void init( String[] args ) {
        if ( args.length < 1 ) Utils.debug = true;
        else if ( args[0].toLowerCase().equals( "-log" ) ) Utils.debug = true;
        else if ( args[0].toLowerCase().equals( "-nolog" ) ) Utils.debug = false;
    }

    /**
     * Creates a small DFA sample
     * @return The start state in the DFA
     */
    public static DFA_state DFA_sample() {
        DFA_state start = new DFA_state( "Q" );

        DFA_state accept = new DFA_state( "F", true );
        
        start.addEdge( 'a', accept );        

        start.registerWord( "Hej" );
        start.registerWord( "Her" );
        return start;
    }

    /**
     * Creates a small DFA sample given by regular expression
     * @return The start state in the DFA
     */
    public static DFA_state reg_sample() {
        String reg = "(01|2)3";
        // reg = "01|3";
        // reg = "012asd";
        reg = "1(2)*3";
        // reg = "(01|23)*4";
        NFA_state nfa = Regex.parse( reg );
        Utils.lines( 3 );
        Utils.log( NFA_state.getStringRepresentation( nfa ) );
        Utils.lines( 3 );
        DFA_state dfa = NFA_state.toDFA( nfa );
        return dfa;
    }

    /**
     * This method enables the user to traverse the DFA via stdin.
     * @param start The start state
     */
    public static void testRun( DFA_state start ) {
        

        DFA_state current = start;

        Scanner in = new Scanner( System.in );
        System.out.println( "Following commands are available:" );
        System.out.println( "\t[RESET] -> To restart from the starting state." );
        System.out.println( "\t[GET] -> To retrieve the final state." );
        System.out.println( "\t[C-d] -> To stop the program." );


        System.out.println( "\n" + current.toString() );
        System.out.print( "\n> " );
        
        while ( in.hasNext() ) {
            
            String s = in.next();
            
            if ( s.toUpperCase().equals( "RESET" ) ) {
                current = start;
            } else if ( s.toUpperCase().equals( "GET" ) && current.isFinal ) {
                System.out.println( "Retrieving: " + current.name );
                current = start;
            } else {
            
                if ( current.canConsume( s.charAt( 0 ) ) )
                current = current.consume( s.charAt( 0 ) );
                else
                    System.out.println( "Cannot consume " + s );
            }
            System.out.println( "\n" + current.toString() );
            System.out.print( "\n> " );
        }

        in.close();
    }
}