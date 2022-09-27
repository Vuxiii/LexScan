package src;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DFA_state {
    private static int c = 0;
    public String name;

    public boolean isFinal = false;
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
