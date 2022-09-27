package src;

public class DFA_edge {
    char accept;
    DFA_state to;
    DFA_state from;

    public DFA_edge( char c ) {
        accept = c;
    }

}