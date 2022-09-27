import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class App {
    public static void main( String[] args ) {
        
        // String reg1 = "(00|1)2(3|4)5";
        // String reg2 = "(0|1)23(4|5)";

        // DFA_state q = parseRegex( reg1 );
        // DFA_state q = DFA_sample();


        // NFA_state q1 = new NFA_state( "1" );
        // NFA_state q2 = new NFA_state( "2" );
        // NFA_state q3 = new NFA_state( "3", true );
        // NFA_state q4 = new NFA_state( "4" );
        // NFA_state q5 = new NFA_state( "5" );


        // q1.addEdge( 'c', q2 );
        // q1.addEdge( 'a', q4 );
        // q2.addEdge( q4 );
        // q2.addEdge( q5 );
        // q2.addEdge( 'd', q3 );
        // q2.addEdge( 'd', q5 );
        // q4.addEdge( 'b', q5 );
        // q5.addEdge( 'f', q3 );


        // NFA_state.removeEpsilonEdges( q1 );

        // // System.out.println( NFA_state.getStringRepresentation( q1 ) );

        // System.out.println("-".repeat(10));

        // DFA_state dfa = q1.toDFA();
        
        // System.out.println("-".repeat(10));

        // System.out.println( DFA_state.getStringRepresentation( dfa ) );

        // DFA_state dfaq = q.toDFA();

        // testRun( dfaq );



        // NFA_state q1 = new NFA_state( "1" );
        // q1.registerWord( "William" );
        // q1.registerWord( "Juhl" );
        // q1.registerWord( "Will.i.am" );

        // NFA_state.removeEpsilonEdges( q1 );
        // DFA_state dfa = q1.toDFA();

        // testRun( dfa );


        // NFA_state start = new NFA_state();
        // NFA_state end = new NFA_state( "", true );
        // NFA_state mid = new NFA_state();
        // start.addEdge( mid );
        // mid.addEdge( start );
        // mid.addEdge( 'a', end );

        // end.addEdge( mid );
        // NFA_state.removeEpsilonEdges( start );

        // System.out.println( NFA_state.getStringRepresentation(start));

        String reg = "(01|2)3";
        // reg = "01|3";
        // reg = "123";
        reg = "(01|23)*4";
        NFA_state nfa = Regex.parse( reg );
        // NFA_state.removeEpsilonEdges( nfa );


        System.out.println();
        System.out.println( NFA_state.getStringRepresentation( nfa ) );
        System.out.println( NFA_state.c );

        System.out.println( "\n".repeat(4));

        DFA_state dfa = nfa.toDFA();

        System.out.println( DFA_state.getStringRepresentation( dfa ) ); 

    }

    public static DFA_state DFA_sample() {
        DFA_state start = new DFA_state( "Q" );

        DFA_state accept = new DFA_state( "F", true );
        
        start.addEdge( 'a', accept );        

        start.registerWord( "Hej" );
        start.registerWord( "Her" );
        return start;
    }

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

    public static DFA_state parseRegex( String reg ) {

        // Read char by char
        DFA_state start_state = new DFA_state( "q" );
        DFA_state end = start_state;
        for ( int i = 0; i < reg.length(); ++i ) {
            char c = reg.charAt( i );
            System.out.println( "\tChecking " + c );
            if ( c == '(' ) {
                int start = i+1;
                int close = reg.indexOf( ')', start );
                i = close + 1;
                String subreg = reg.substring( start, close );
                System.out.println( "\tSubreg: " + subreg );
                if ( subreg.contains( "|" ) ) {
                    List<DFA_state> union = parseRegex_union( end, subreg );

                    // Check if there is a concatenation next. 
                    // If so: make each of the union_states point to that next state.
                    // Also check bounds...
                    if ( i < reg.length() ) {
                        char checkMe = reg.charAt( i );
                        if ( checkMe != '(' || checkMe != '|' || checkMe != '*' ) {
                            
                            for ( DFA_state state : union ) {
                                System.out.println( state.name );
                                end = parseRegex_concat( state, checkMe + "" );
                            }
                            
                        }
                    }

                    
                }
            } else {
                int start = i;
                int close = reg.indexOf( '(' ); // Also check * and |
                System.out.println( "Close is: " + close );
                close = close == 0 ? reg.length() : close;
                System.out.println( "Close is: " + close );

                String subreg = reg.substring( start, close );
                end = parseRegex_concat( end, subreg );
            }
        }
        

        return start_state;
    } 

    public static List<DFA_state> parseRegex_union( DFA_state from, String reg ) {
        System.out.println( reg );
        List<DFA_state> states = new ArrayList<>();
        // for ( int i = 0; i < reg.length(); ++i ) {
            // char c = reg.charAt( i );

            int edge = reg.indexOf( "|" );

            DFA_state left = parseRegex_concat( from, reg.substring( 0, edge ) );
            DFA_state right = parseRegex_concat( from, reg.substring( edge + 1 ) );
            states.add( left );
            states.add( right );
        // }

        return states;
    }

    public static DFA_state parseRegex_concat( DFA_state from, String reg ) {
        System.out.println( reg );

        return from.registerWord( reg ); // Return the last added state.

        // return from.consumeWord( reg ); // Return the last added state.
    }

    public static DFA_state parseRegex_star( String reg ) {

        return null;
    }
}

class Regex {

    /**
     * reg := concat + reg | concat
     * concat := token * concat | token
     * token := (reg) | state
     * 
     *  0123
     *  (01|2)*3
     * 
     * @param regex
     * @return
     */
    public static NFA_state parse( String regex ) {

        NFA_state start = new NFA_state();
        System.out.println( "Made state with name: " + start.name );
        List<Character> list = new LinkedList<>();
        System.out.println( regex.strip() );
        for ( char c : regex.strip().toCharArray() ) {// remove whitespace.
            list.add( c );
            // System.out.println( c );
        }
        System.out.println( list );
        
        NFA_state last = start;
        
        while ( list.size() > 0 ) 
            last = parseStar( list, last );
        last.isFinal = true;

        return start;
    }

    private static NFA_state parseStar( List<Character> list, NFA_state head_state ) {
        
        System.out.println( "parseStar" );
        System.out.println( "\t" + list );
        
        NFA_state state = parseUnion( list, head_state );
        System.out.println( "parseStar After parseUnion" );
        System.out.println( "Got " + state.name );
        System.out.println( list );
        if ( list.size() > 0 && list.get(0) == '*' ) {
            state.addEdge( head_state );
            head_state.addEdge( state );
            list.remove(0);
        }

        return state;
    }

    private static NFA_state parseUnion( List<Character> list, NFA_state head_state ) {
        System.out.println( "parseUnion" );
        System.out.println( "\t" + list );
        NFA_state state = parseConcat( list, head_state );
        System.out.println( "parseUnion After parseConcat" );
        System.out.println( "Got " + state.name );
        if ( list.size() > 0 && list.get(0) == '|' ) {
            list.remove(0);
            
            NFA_state right = parseConcat( list, head_state );
            NFA_state left = state;
            // head_state.addEdge( left );
            // head_state.addEdge( right );
            
            state = new NFA_state( "UnionState" );
            System.out.println( "Made state with name: " + state.name );
            // System.out.println( "\tAdding edge State: " + left.name + " -" + "epsilon2" + "> State: " + state.name );
            // System.out.println( "\tAdding edge State: " + right.name + " -" + "epsilon3" + "> State: " + state.name );
            left.addEdge( state );
            right.addEdge( state );

        }
        System.out.println( "Returning from parseUnion with " + state.name );
        return state;
    }

    // mult
    private static NFA_state parseConcat( List<Character> list, NFA_state head_state ) {
        System.out.println( "parseConcat" );
        System.out.println( "\t" + list );
        NFA_state state = parseToken( list, head_state );

        // Add epsilon edge from head_state to state.

        

        // NFA_state start = new NFA_state();

        // System.out.println( "parseConcat after parseToken" );
        String s = "";
        while ( !list.isEmpty() && !(list.get(0) == '|' || list.get(0) == '(' || list.get(0) == ')' || list.get(0) == '*' ) ) {
            s += list.remove(0);
        }
        System.out.println( "S is: " + s);
        if ( s.length() > 0 ) { 
            // head_state.addEdge( state ); 
            // head_state = state.registerWord( s ); 
            state = state.registerWord( s, false );
        }
        System.out.println( "Returning from parseConcat with: " + state.name );
        return state;
    }

    private static NFA_state parseToken( List<Character> list, NFA_state head_state ) {
        if ( list.size() == 0 ) return null;
        System.out.println( "parseToken " );
        NFA_state state;
        
        System.out.println( "\t" + list );
        char head = list.get(0);
        System.out.println( "\t" + head );
        if ( head == '(' ) {
            System.out.println( "\tParen" );
            list.remove(0);

            state = parseStar( list, head_state );
            // head_state.addEdge( head, state );
            
            list.remove(0); // ')'
        } else {
            System.out.println( "\tNot paren" );
            // state = new NFA_state();
            state = head_state;
            // System.out.println( "\tMade state with name " + state.name );
            // head_state.addEdge( head, state );

            // list.remove(0);
        }
        System.out.println( "parseToken Done" );
        System.out.println( "Returning from parseToken with " + state.name );
        return state;
    }


}

class NFA_state {
    String name;
    
    boolean isFinal = false;

    List<NFA_edge> out;
    List<NFA_edge> in;
    public static int c = 0;

    public NFA_state() {
        this( "", false );
    }

    public NFA_state( String name ) {
        this( name, false );
    }

    public NFA_state( String name, boolean isFinal ) {
        this.name = name + (c++);
        this.isFinal = isFinal;
        out = new ArrayList<>();
        in = new ArrayList<>();

        System.out.println( "-".repeat(10));
        
    }

    public boolean canConsume( char c ) {

        boolean found = false;

        for ( NFA_edge e : out ) {
            if ( e.accept == c )
                return true;
            if ( e.isEpsilon )
                found = e.to.canConsume( c );
        }

        return found;
    }

    public boolean hasEpsilonEdge() {
        for ( NFA_edge e : out ) 
            if ( e.isEpsilon )
                return true;
        return false;
    }

    public NFA_state registerWord( String s ) {
        NFA_state start = new NFA_state();

        addEdge( start );
        return start._registerWord( s );
    }

    private NFA_state _registerWord( String s ) {
        if ( s.length() == 0 ) return this;

        char c = s.charAt( 0 );

        NFA_state newState = new NFA_state( "", s.length() == 1 ); 
        addEdge( c, newState );

        return newState._registerWord( s.substring( 1 ) );
    }


    public NFA_state registerWord( String s, boolean shouldAccept ) { // Add should accept state.
        NFA_state start = new NFA_state();

        addEdge( start );
        return start._registerWord( s, shouldAccept );
    }

    
    private NFA_state _registerWord( String s, boolean shouldAccept ) {
        if ( s.length() == 0 ) return this;

        char c = s.charAt( 0 );

        NFA_state newState = new NFA_state( "", s.length() == 1 ? shouldAccept : false ); 
        addEdge( c, newState );

        return newState._registerWord( s.substring( 1 ), shouldAccept );
    }

    /**
     * Adds 
     * @param c
     * @param to
     */
    public void addEdge( char c, NFA_state to ) {
        System.out.println( "Adding edge " + name + " -" + c + "> " + to.name );
        NFA_edge e = new NFA_edge( c );
        e.from = this;
        e.to = to;

        out.add( e );
        to.in.add( e );

    }

    /**
     * Adds an epsilon edge to the given state.
     * @param to The state to go to.
     */
    public void addEdge( NFA_state to ) {
        System.out.println( "Adding edge " + name + " -epsilon1> " + to.name );

        NFA_edge e = new NFA_edge();
        e.from = this;
        e.to = to;

        out.add( e );
        to.in.add( e );

    }

    /**
     * Consumes all edges matching c from this state.
     * @param c The char to accept.
     * @return The list of states reached by traversing the edges with c.
     */
    public List<NFA_state> consume( char c ) {
        List<NFA_state> list = new ArrayList<>();
        
        for ( NFA_edge e : out ) {
            if ( e.accept == c ) 
                list.add( e.to );
        }

        return list;
    }

    /**
     * Consumes all epsilon edges from this state.
     * @return
     */
    public List<NFA_state> consume() {

        List<NFA_state> list = new ArrayList<>();
        
        for ( NFA_edge e : out ) {
            if ( e.isEpsilon ) 
                list.add( e.to );
        }

        return list;
    }


    public DFA_state toDFA() {

        List<DFA_state> dfa_states = new LinkedList<>();
        Set<String> visitedDFA = new HashSet<>();
        Set<NFA_state> visitedNFA = new HashSet<>();

        DFA_state start = new DFA_state( name );
        
        DFA_state current_dfa = start;
        // NFA_state current_nfa = this;

        dfa_states.add( start );
        

        Map<Character, List<String>> newStatesNames = new HashMap<>();
        Map<String, DFA_state> newStates = new HashMap<>();
        Map<DFA_state, List<NFA_state>> dfa_nfa = new HashMap<>();
        dfa_nfa.put( start, NFA_state.toList( this ) );
        
        while ( dfa_states.size() > 0 ) {
            visitedNFA.clear();
            // List<NFA_state> states = NFA_state.collectStates( this );
            current_dfa = dfa_states.remove( 0 );

            List<NFA_state> nfas = dfa_nfa.get( current_dfa );

            System.out.println( "Current DFA: " + current_dfa.name );
            nfas.forEach( (n) -> System.out.println( "Current NFA: " + n.name ) );

            newStatesNames.clear();
            for ( int i = 0; i < nfas.size(); ++i ) {
                NFA_state current_nfa = nfas.get(i);
                visitedNFA.add( current_nfa );

                System.out.println( "Visiting NFA " + current_nfa.name );
            // }
                for ( NFA_edge e : current_nfa.out ) {
                    // Check for epsilon edge. If it is, go to that state and try again.
                    if ( e.isEpsilon && !visitedNFA.contains( e.to ) ) {
                        nfas.add( e.to );
                    } else {
                        System.out.println( "Edge: " + e.from.name + " -" + e.accept + "> " + e.to.name );
                        newStatesNames.merge( e.accept, NFA_state.toList( e.to.name ), (old, ne) -> { old.addAll( ne ); return old; } );
                    }
                }
            }
            
            System.out.println( "\ncurrent_dfa: " + current_dfa.name );
            System.out.println( "newStatesNames:" );
            newStatesNames.forEach( (c, names) -> System.out.println( "\t(" + c + ", " + names + ")"));

            // newStates.clear();
            for ( char c : newStatesNames.keySet() ) {
                if ( c == ' ' ) continue;
                List<String> strings = newStatesNames.get( c );
                // System.out.println( strings );
                String s = "";
                for ( String ss : strings ) s += ss + ",";
                DFA_state state;
                if ( newStates.containsKey( s ) )
                    state = newStates.get( s );
                else
                    state = new DFA_state( s.substring(0, s.length() - 1) );
                
                System.out.println( "Creating state: " + state.name );
                System.out.println( "Adding edge " + current_dfa.name + " -" + c + "> " + state.name );
                newStates.put( s, state );

                current_dfa.addEdge( c, state );
                // newStates.put( s, state );
                if ( !visitedDFA.contains( state.name ) ) {
                    dfa_states.add( state );
                    visitedDFA.add( state.name );
                }
                // Find the correct NFA_state that this DFA_state maps to. Might be a list of states.
                for ( NFA_state current_nfa : nfas ) {
                    for ( NFA_edge e : current_nfa.out ) {
                        if ( state.name.contains( e.to.name ) ) // Some DFA states contains multiple of the same NFA state....
                            dfa_nfa.merge( state, NFA_state.toList( e.to ), (old, ne) -> { 
                                if ( !old.contains( ne.get( 0 ) ) ) // Bad solution..... Mayve use an extra HashSet for O(1) performance instead of O(n)...
                                    old.addAll( ne ); 
                                return old; 
                            } );
                    }
                }
                // nfa_dfa.put( current_nfa.out, state );

            }
            System.out.println( "-".repeat(10) );
        }
        

        // Make the new states the correct isFinal
        dfa_nfa.forEach( (dfa, nfas) -> {
            nfas.forEach( (nfa) -> { if ( nfa.isFinal ) dfa.isFinal = true; } );
        } );


        return start;
    }

    /*
     
    public DFA_state toDFA() {

        List<DFA_state> dfa_states = new LinkedList<>();
        Set<String> visitedDFA = new HashSet<>();
        
        DFA_state start = new DFA_state( name );
        
        DFA_state current_dfa = start;
        // NFA_state current_nfa = this;

        dfa_states.add( start );
        

        Map<Character, List<String>> newStatesNames = new HashMap<>();
        Map<String, DFA_state> newStates = new HashMap<>();
        Map<DFA_state, List<NFA_state>> dfa_nfa = new HashMap<>();
        dfa_nfa.put( start, List.of( this ) );
        
        while ( dfa_states.size() > 0 ) {
            // List<NFA_state> states = NFA_state.collectStates( this );
            current_dfa = dfa_states.remove( 0 );

            List<NFA_state> nfas = dfa_nfa.get( current_dfa );

            System.out.println( "Current DFA: " + current_dfa.name );
            nfas.forEach( (n) -> System.out.println( "Current NFA: " + n.name ) );

            newStatesNames.clear();
            for ( NFA_state current_nfa : nfas ) {
                for ( NFA_edge e : current_nfa.out ) {
                    System.out.println( "Edge: " + e.from.name + " -" + e.accept + "> " + e.to.name );
                    newStatesNames.merge( e.accept, NFA_state.toList( e.to.name ), (old, ne) -> { old.addAll( ne ); return old; } );
                }
            }
            
            

            // newStates.clear();
            for ( char c : newStatesNames.keySet() ) {
                List<String> strings = newStatesNames.get( c );
                // System.out.println( strings );
                String s = "";
                for ( String ss : strings ) s += ss + ",";
                DFA_state state;
                if ( newStates.containsKey( s ) )
                    state = newStates.get( s );
                else
                    state = new DFA_state( s.substring(0, s.length() - 1) );
                
                System.out.println( "Creating state: " + state.name );
                System.out.println( "Adding edge " + current_dfa.name + " -" + c + "> " + state.name );
                newStates.put( s, state );

                current_dfa.addEdge( c, state );
                // newStates.put( s, state );
                if ( !visitedDFA.contains( state.name ) ) {
                    dfa_states.add( state );
                    visitedDFA.add( state.name );
                }
                // Find the correct NFA_state that this DFA_state maps to. Might be a list of states.
                for ( NFA_state current_nfa : nfas ) {
                    for ( NFA_edge e : current_nfa.out ) {
                        if ( state.name.contains( e.to.name ) ) // Some DFA states contains multiple of the same NFA state....
                            dfa_nfa.merge( state, NFA_state.toList( e.to ), (old, ne) -> { 
                                if ( !old.contains( ne.get( 0 ) ) ) // Bad solution..... Mayve use an extra HashSet for O(1) performance instead of O(n)...
                                    old.addAll( ne ); 
                                return old; 
                            } );
                    }
                }
                // nfa_dfa.put( current_nfa.out, state );

            }
            System.out.println( "-".repeat(10) );
        }
        

        // Make the new states the correct isFinal
        dfa_nfa.forEach( (dfa, nfas) -> {
            nfas.forEach( (nfa) -> { if ( nfa.isFinal ) dfa.isFinal = true; } );
        } );


        return start;
    }


     */

    private static<T> List<T> toList( T el ) {
        List<T> list = new ArrayList<T>();
        list.add( el );
        return list;
    }

    public static void removeEpsilonEdges( NFA_state nfa ) {
        List<NFA_state> q = collectStates( nfa );
        _removeEpsilonEdges( nfa, q );
    }

    public static List<NFA_state> collectStates( NFA_state nfa ) {
        List<NFA_state> q = new LinkedList<>();
        _collectStates( nfa, q );

        return q;
    }

    private static void _collectStates( NFA_state nfa, List<NFA_state> q ) {
        if ( q.contains( nfa ) ) return;
        q.add( nfa );
        for ( NFA_edge e : nfa.out )
            _collectStates( e.to, q );
    }

    private static void _removeEpsilonEdges( NFA_state nfa, List<NFA_state> q ) {
        List<NFA_state> allStates = collectStates( nfa );
        Set<NFA_state> visited = new HashSet<>();

        for ( NFA_state current_state : allStates ) {
            visited.add( current_state );
            List<NFA_edge> epsilonEdges = new ArrayList<>();

            for ( NFA_edge e : current_state.out ) 
                if ( e.isEpsilon ) epsilonEdges.add( e );

            

            List<NFA_state> copyFrom = new ArrayList<>();

            for ( NFA_edge e : epsilonEdges ) {
                NFA_state state = e.to;
                for ( NFA_edge fromE : state.out ) {

                }
                copyFrom.add( state );
                // Add the edges from this to copyFrom 

                

            }


        }

    }

    // private static List<NFA_state> collectStatesReachableWithEpsilon( NFA_state from ) {
    //     List<NFA_state> states = new ArrayList<>();

    //     for ( NFA_edge e : from.out ) {
    //         if ( e.isEpsilon ) {

    //         }
    //     }



    //     return states;

    // }

    // private static void _removeEpsilonEdges( NFA_state nfa, List<NFA_state> q ) {

    //     List<NFA_state> copyFromState = new ArrayList<>();

    //     while ( q.size() > 0 ) { // Might be able to use (NFA_state current : q)
    //         NFA_state current = q.remove(0);
            
    //         for ( NFA_edge e : current.out ) {
    //             // Copy all the states that are interesting.
    //             if ( e.isEpsilon )
    //                 copyFromState.add( e.to );
    //         }
            
    //         for ( NFA_edge e : new ArrayList<NFA_edge>( current.out ) ) {
    //             if ( e.isEpsilon ) {
                    
    //                 System.out.println( "Removing " + e.toString() );
                    
    //                 // traverse edge to the state pointed to.
    //                 removeEpsilonEdges( e.to );
                    
    //                 // If no epsilon edge, pop        
                    
    //                 // Copy all attributes from the popped state to this current.
    //                 // There should be no epsilon edges because of the DFS
    //                 for ( NFA_edge addE : e.to.out ) {
    //                     boolean shouldAdd = true;
    //                     for ( NFA_edge oe : current.out ) {
    //                         if ( oe.accept == addE.accept && oe.from.equals( current ) && oe.to.equals( addE.to ) )
    //                             shouldAdd = false;
    //                     }
    //                     if ( shouldAdd ) {
    //                         current.addEdge( addE.accept, addE.to );
    //                         // System.out.println("Edge " + current.name + " -" + (addE.accept) + "> " + addE.to.name);
    //                     }

    //                 }
    //                 // Check if the visited state is a final state. If so, we need to make this current state final aswell.
    //                 if ( e.to.isFinal ) 
    //                     current.isFinal = true;
                    
    //                 // Remove epsilon edge
    //                 current.out.remove( e );
    //             }
    //         }
    //     }
    // }

    public NFA_state copy() {
        NFA_state q = new NFA_state( name, isFinal );

        for ( NFA_edge e : out ) {
            NFA_edge ne = new NFA_edge( e.accept );
            ne.isEpsilon = e.isEpsilon;
            ne.from = this;
            
            NFA_state nto = e.to.copy();
            
            ne.to = nto;

            q.out.add( ne );
            nto.in.add( ne );
        }


        return q;
    }

    public static String getStringRepresentation( NFA_state nfa ) {
        String s = "";
        List<NFA_state> states = NFA_state.collectStates( nfa );
        for ( NFA_state state : states ) {
            s += ( "State " + state.name + "\n" );
            s += ( "\tisFinal = " + state.isFinal + "\n" );
            s += ( "\tout edges:" + "\n" );
            for ( NFA_edge e : state.out ) {
                s += ( "\t\t[" + (e.isEpsilon ? "epsilon" : e.accept) + " -> State " + e.to.name + "]" + "\n" );
            }
        }

        return s;
    }
    
}


class NFA_edge {
    char accept;
    boolean isEpsilon = false;

    NFA_state to;
    NFA_state from;

    public NFA_edge( char c ) {
        accept = c;
    }

    public NFA_edge() {
        accept = ' ';
        this.isEpsilon = true;
    }

    public String toString() {
        return "Edge " + from.name + " -" + (isEpsilon ? "epsilon" : accept) + "> " + to.name;
    }

}

class GNFA_state {
    String name;
    
    boolean isFinal = false;
    List<GNFA_edge> out;

    public GNFA_state( String name ) {
        this.name = name;
        out = new ArrayList<>();
    }

    public GNFA_state( String name, boolean isFinal ) {
        this( name );
        this.isFinal = isFinal;
    }



}

class GNFA_edge {
    String accept;

    boolean isEmpty = false;
    boolean isEpsilon = false;

    GNFA_state from;
    GNFA_state to;

    public GNFA_edge( String a ) {
        accept = a;
    }
}

class DFA_state {
    private static int c = 0;
    String name;

    boolean isFinal = false;
    List<DFA_edge> out;

    public DFA_state( String name ) {
        c++;
        this.name = name;
        out = new ArrayList<>();
    }

    public DFA_state( String name, boolean isFinal ) {
        this( name );
        this.isFinal = isFinal;
    }

    public DFA_state registerWord( String word ) {
        if ( word.length() == 0 ) return this;

        char c = word.charAt( 0 );

        DFA_state newState = null;

        for ( DFA_edge e : out ) {
            if ( e.accept == c ) {
                newState = e.to;
            }
        }

        if ( newState == null ) {
            newState = new DFA_state( "State: " + (DFA_state.c+1), word.length() == 1 ); 
            addEdge( c, newState );
        }

        return newState.registerWord( word.substring( 1 ) );
    }

    public void addEdge( char c, DFA_state to ) {
        DFA_edge e = new DFA_edge( c );
        e.to = to;
        out.add( e );
    }

    public boolean canConsume( char c )  {
        for ( DFA_edge e : out )
            if ( e.accept == c )
                return true;
        return false;
    }

    public DFA_state consume( char c ) {
        DFA_edge acceptEdge = null;
        for ( DFA_edge e : out ) {
            if ( e.accept == c ) {
                acceptEdge = e;
                break;
            }
        }
        if ( acceptEdge == null ) return null;

        return acceptEdge.to;
    }

    /**
     * Precondition: Can consume the word
     * @param s The string to be consumed
     * @return The last state
     */
    public DFA_state consumeWord( String s ) {
        DFA_state current = this;
        for ( char c : s.toCharArray() )
            current = current.consume( c );
        return current;
    }

    public GNFA_state toGNFA() {
        
        DFA_state q = copy();

        // Choose a state to remove.
        // 

        return null;
    }

    public DFA_state copy() {

        DFA_state q = new DFA_state( name, isFinal );
        for ( DFA_edge e : out ) {
            DFA_state s = e.to.copy();
            DFA_edge ecopy = new DFA_edge( e.accept );
            ecopy.from = q;
            ecopy.to = s;
            s.out.add( ecopy );
        }

        return q;
    }

    public String toString() {
        String s = isFinal ? "[[" + name + "]]" : "[" + name + "]";
        s += "\t";
        if ( out.size() > 0 ) {
            s += "Can go to the following states:\n";
            for ( DFA_edge e : out ) {
                s += "\t[" + e.accept + "] -> " + e.to.name + "\n";
            }
        } else {
            s += "This state has no outgoing edges.";
        }

        return s;
    }

    public static List<DFA_state> collectStates( DFA_state dfa ) {
        List<DFA_state> q = new LinkedList<>();
        _collectStates( dfa, q );

        return q;
    }

    private static void _collectStates( DFA_state dfa, List<DFA_state> q ) {
        if ( q.contains( dfa ) ) return;
        q.add( dfa );
        for ( DFA_edge e : dfa.out )
            _collectStates( e.to, q );
    }

    public static String getStringRepresentation( DFA_state nfa ) {
        String s = "";
        List<DFA_state> states = DFA_state.collectStates( nfa );
        for ( DFA_state state : states ) {
            s += ( "State " + state.name + "\n" );
            s += ( "\tisFinal = " + state.isFinal + "\n" );
            s += ( "\tout edges:" + "\n" );
            for ( DFA_edge e : state.out ) {
                s += ( "\t\t[" + e.accept + " -> State " + e.to.name + "]" + "\n" );
            }
        }

        return s;
    }
}

class DFA_edge {
    char accept;
    DFA_state to;
    DFA_state from;

    public DFA_edge( char c ) {
        accept = c;
    }

}