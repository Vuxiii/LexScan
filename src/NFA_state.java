package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NFA_state {
    public String name;
    
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

        Utils.log( "-".repeat(10));
        
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
        Utils.log( "Adding edge " + name + " -" + c + "> " + to.name );
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
        Utils.log( "Adding edge " + name + " -epsilon1> " + to.name );

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

            Utils.log( "Current DFA: " + current_dfa.name );
            nfas.forEach( (n) -> Utils.log( "Current NFA: " + n.name ) );

            newStatesNames.clear();
            for ( int i = 0; i < nfas.size(); ++i ) {
                NFA_state current_nfa = nfas.get(i);
                visitedNFA.add( current_nfa );

                Utils.log( "Visiting NFA " + current_nfa.name );
            // }
                for ( NFA_edge e : current_nfa.out ) {
                    // Check for epsilon edge. If it is, go to that state and try again.
                    if ( e.isEpsilon && !visitedNFA.contains( e.to ) ) {
                        nfas.add( e.to );
                    } else {
                        Utils.log( "Edge: " + e.from.name + " -" + e.accept + "> " + e.to.name );
                        newStatesNames.merge( e.accept, NFA_state.toList( e.to.name ), (old, ne) -> { old.addAll( ne ); return old; } );
                    }
                }
            }
            
            Utils.log( "\ncurrent_dfa: " + current_dfa.name );
            Utils.log( "newStatesNames:" );
            newStatesNames.forEach( (c, names) -> Utils.log( "\t(" + c + ", " + names + ")"));

            // newStates.clear();
            for ( char c : newStatesNames.keySet() ) {
                if ( c == ' ' ) continue;
                List<String> strings = newStatesNames.get( c );
                // Utils.log( strings );
                String s = "";
                for ( String ss : strings ) s += ss + ",";
                DFA_state state;
                if ( newStates.containsKey( s ) )
                    state = newStates.get( s );
                else
                    state = new DFA_state( s.substring(0, s.length() - 1) );
                
                Utils.log( "Creating state: " + state.name );
                Utils.log( "Adding edge " + current_dfa.name + " -" + c + "> " + state.name );
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
            Utils.log( "-".repeat(10) );
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

            Utils.log( "Current DFA: " + current_dfa.name );
            nfas.forEach( (n) -> Utils.log( "Current NFA: " + n.name ) );

            newStatesNames.clear();
            for ( NFA_state current_nfa : nfas ) {
                for ( NFA_edge e : current_nfa.out ) {
                    Utils.log( "Edge: " + e.from.name + " -" + e.accept + "> " + e.to.name );
                    newStatesNames.merge( e.accept, NFA_state.toList( e.to.name ), (old, ne) -> { old.addAll( ne ); return old; } );
                }
            }
            
            

            // newStates.clear();
            for ( char c : newStatesNames.keySet() ) {
                List<String> strings = newStatesNames.get( c );
                // Utils.log( strings );
                String s = "";
                for ( String ss : strings ) s += ss + ",";
                DFA_state state;
                if ( newStates.containsKey( s ) )
                    state = newStates.get( s );
                else
                    state = new DFA_state( s.substring(0, s.length() - 1) );
                
                Utils.log( "Creating state: " + state.name );
                Utils.log( "Adding edge " + current_dfa.name + " -" + c + "> " + state.name );
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
            Utils.log( "-".repeat(10) );
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
                    
    //                 Utils.log( "Removing " + e.toString() );
                    
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
    //                         // Utils.log("Edge " + current.name + " -" + (addE.accept) + "> " + addE.to.name);
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

