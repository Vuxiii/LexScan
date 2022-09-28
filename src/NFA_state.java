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


    public static DFA_state toDFA( NFA_state begin_state ) {

        List<DFA_state> dfa_states = new LinkedList<>();
        Set<String> visitedDFA = new HashSet<>();
        Set<NFA_state> visitedNFA = new HashSet<>();

        DFA_state start = new DFA_state( begin_state.name );
        
        DFA_state current_dfa = start;
        // NFA_state current_nfa = this;

        dfa_states.add( start );
        

        Map<Character, List<String>> newStatesNames = new HashMap<>();
        Map<String, DFA_state> newStates = new HashMap<>();
        Map<DFA_state, List<NFA_state>> dfa_nfa = new HashMap<>();
        dfa_nfa.put( start, Utils.toList( begin_state ) );
        
        while ( dfa_states.size() > 0 ) {
            visitedNFA.clear();
            // List<NFA_state> states = NFA_state.collectStates( this );
            current_dfa = dfa_states.remove( 0 );

            List<NFA_state> nfas = dfa_nfa.get( current_dfa );

            Utils.log( "Current DFA: " + current_dfa.name );
            nfas.forEach( (n) -> Utils.log( "Current NFA: " + n.name ) );

            newStatesNames.clear();
            // Creates the new to states reachable from this current dfa.
            NFA_state.visitReachableStates( nfas, visitedNFA, newStatesNames );
                        
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
                            dfa_nfa.merge( state, Utils.toList( e.to ), (old, ne) -> { 
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


    private static void visitReachableStates( List<NFA_state> nfas, Set<NFA_state> visitedNFA, Map<Character, List<String>> newStatesNames ) {
        for ( int i = 0; i < nfas.size(); ++i ) {
            NFA_state current_nfa = nfas.get(i);
            visitedNFA.add( current_nfa );

            Utils.log( "Visiting NFA " + current_nfa.name );
        
            for ( NFA_edge e : current_nfa.out ) {
                // Check for epsilon edge. If it is, go to that state and try again.
                if ( e.isEpsilon && !visitedNFA.contains( e.to ) ) {
                    nfas.add( e.to );
                } else {
                    Utils.log( "Edge: " + e.from.name + " -" + e.accept + "> " + e.to.name );
                    newStatesNames.merge( e.accept, Utils.toList( e.to.name ), (old, ne) -> { old.addAll( ne ); return old; } );
                }
            }
        }
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

