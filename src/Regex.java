package src;

import java.util.LinkedList;
import java.util.List;

public class Regex {
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
        Utils.log( "Made state with name: " + start.name );
        List<Character> list = new LinkedList<>();
        Utils.log( regex.strip() );
        for ( char c : regex.strip().toCharArray() ) {// remove whitespace.
            list.add( c );
            // Utils.log( c );
        }
        Utils.log( list.toString() );
        
        NFA_state last = start;
        
        while ( list.size() > 0 ) 
            last = parseStar( list, last );
        last.isFinal = true;

        return start;
    }

    private static NFA_state parseStar( List<Character> list, NFA_state head_state ) {
        
        Utils.log( "parseStar" );
        Utils.log( "\t" + list );
        
        NFA_state state = parseUnion( list, head_state );
        Utils.log( "parseStar After parseUnion" );
        Utils.log( "Got " + state.name );
        Utils.log( list.toString() );
        if ( list.size() > 0 && list.get(0) == '*' ) {
            state.addEdge( head_state );
            head_state.addEdge( state );
            list.remove(0);
        }

        return state;
    }

    private static NFA_state parseUnion( List<Character> list, NFA_state head_state ) {
        Utils.log( "parseUnion" );
        Utils.log( "\t" + list );
        NFA_state state = parseConcat( list, head_state );
        Utils.log( "parseUnion After parseConcat" );
        Utils.log( "Got " + state.name );
        if ( list.size() > 0 && list.get(0) == '|' ) {
            list.remove(0);
            
            NFA_state right = parseConcat( list, head_state );
            NFA_state left = state;
            // head_state.addEdge( left );
            // head_state.addEdge( right );
            
            state = new NFA_state( "UnionState" );
            Utils.log( "Made state with name: " + state.name );
            // Utils.log( "\tAdding edge State: " + left.name + " -" + "epsilon2" + "> State: " + state.name );
            // Utils.log( "\tAdding edge State: " + right.name + " -" + "epsilon3" + "> State: " + state.name );
            left.addEdge( state );
            right.addEdge( state );

        }
        Utils.log( "Returning from parseUnion with " + state.name );
        return state;
    }

    // mult
    private static NFA_state parseConcat( List<Character> list, NFA_state head_state ) {
        Utils.log( "parseConcat" );
        Utils.log( "\t" + list );
        NFA_state state = parseToken( list, head_state );

        // Add epsilon edge from head_state to state.

        

        // NFA_state start = new NFA_state();

        // Utils.log( "parseConcat after parseToken" );
        String s = "";
        while ( !list.isEmpty() && !(list.get(0) == '|' || list.get(0) == '(' || list.get(0) == ')' || list.get(0) == '*' ) ) {
            s += list.remove(0);
        }
        Utils.log( "S is: " + s);
        if ( s.length() > 0 ) { 
            // head_state.addEdge( state ); 
            // head_state = state.registerWord( s ); 
            state = state.registerWord( s, false );
        }
        Utils.log( "Returning from parseConcat with: " + state.name );
        return state;
    }

    private static NFA_state parseToken( List<Character> list, NFA_state head_state ) {
        if ( list.size() == 0 ) return null;
        Utils.log( "parseToken " );
        NFA_state state;
        
        Utils.log( "\t" + list );
        char head = list.get(0);
        Utils.log( "\t" + head );
        if ( head == '(' ) {
            Utils.log( "\tParen" );
            list.remove(0);

            state = parseStar( list, head_state );
            // head_state.addEdge( head, state );
            
            list.remove(0); // ')'
        } else {
            Utils.log( "\tNot paren" );
            // state = new NFA_state();
            state = head_state;
            // Utils.log( "\tMade state with name " + state.name );
            // head_state.addEdge( head, state );

            // list.remove(0);
        }
        Utils.log( "parseToken Done" );
        Utils.log( "Returning from parseToken with " + state.name );
        return state;
    }
}
