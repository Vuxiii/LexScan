package src;

/**
 * This class represents edges between NFA_states
 * It can either accept a char or it is a epsilon edge.
 */
public class NFA_edge {
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
