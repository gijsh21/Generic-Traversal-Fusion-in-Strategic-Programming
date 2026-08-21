package org.example.fuser.nfa;

import org.example.fuser.ast.AccessPath;
import org.example.fuser.util.Pair;

import java.util.*;

/*
Represents an NFA (used for dependency analysis)
Contains utilities for constructing NFAs and checking intersections
 */
public class NFA {

    /*
    NFA States
    IDs are automatically assigned
     */
    public static class State {
        static int NEXT_ID = 0;
        public final int id = NEXT_ID++;

        // Is this an accept state or not
        public boolean accept;

        // Edges. Symbol -> Target states
        Map<String, Set<State>> transitions = new HashMap<>();

        // Epsilon edges
        Set<State> epsilon = new HashSet<>();

        @Override
        public String toString() {
            return "S" + id + (accept ? "(A)" : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if(o instanceof State) {
                State s = (State) o;
                return id == s.id;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    // NFA Start state
    public State start;

    public NFA(State start) {
        this.start = start;
    }

    // Create read automaton from rewrite rule read and write access paths
    public static NFA readNFAFromAccessPaths(List<AccessPath> reads, List<AccessPath> writes) {
        State s = new State();
        NFA res = new NFA(s);

        for(AccessPath ap : reads) {
            if(ap.path.isEmpty()) continue;
            State curr = s;
            for(int i = 0; i < ap.path.size(); i++) {
                curr = symbolModifying(curr, ap.path.get(i));
            }
        }

        for(AccessPath ap : writes) {
            if(ap.path.isEmpty()) continue;
            State curr = s;
            for(int i = 0; i < ap.path.size() - 1; i++) {
                curr = symbolModifying(curr, ap.path.get(i));
            }
        }

        return res;
    }

    // Create write automaton from rewrite rule write access paths
    public static NFA writeNFAFromAccessPaths(List<AccessPath> aps) {
        State s = new State();
        NFA res = new NFA(s);

        for(AccessPath ap : aps) {
            if(ap.path.isEmpty()) continue;
            State curr = s;
            for(int i = 0; i < ap.path.size(); i++) {
                curr = symbolModifyingNoAccept(curr, ap.path.get(i));
            }
            curr.accept = true;
        }

        return res;
    }

    // Effectively adds the start state of NFA b as a start state of NFA a
    public static void addToStart(NFA a, NFA b) {
        State s = a.start;
        s.epsilon.add(b.start);
    }

    // Adds an edge labeled t pointing to the start state of NFA b,
    // to the start state of NFA a
    public static void addToStartWith(NFA a, String t, NFA b) {
        State s = a.start;
        s.transitions
                .computeIfAbsent(t, k -> new HashSet<>())
                .add(b.start);
    }

    // Adds an edge from the State s labeled sym, to a new accept state
    // that is also returned
    public static State symbolModifying(State s, String sym) {
        State f = symbolModifyingNoAccept(s, sym);
        f.accept = true;
        return f;
    }

    // Adds an edge from the State s labeled sym, to a new state that is also returned
    public static State symbolModifyingNoAccept(State s, String sym) {
        State f = new State();
        s.transitions
                .computeIfAbsent(sym, k -> new HashSet<>())
                .add(f);
        return f;
    }

    // Computes the epsilon closure of a set of states
    public static Set<State> epsilonClosure(Set<State> states) {
        Stack<State> stack = new Stack<>();
        Set<State> closure = new HashSet<>(states);

        stack.addAll(states);

        while (!stack.isEmpty()) {
            State s = stack.pop();
            for (State e : s.epsilon) {
                if (!closure.contains(e)) {
                    closure.add(e);
                    stack.push(e);
                }
            }
        }

        return closure;
    }

    // Computes the states reachable by moving on sym from the
    // set of starting states
    public static Set<State> move(Set<State> states, String sym) {
        Set<State> result = new HashSet<>();
        for (State s : states) {
            Set<State> targets = s.transitions.get(sym);
            if (targets != null) {
                result.addAll(targets);
            }
        }
        return result;
    }

    // Checks whether two NFAs intersect
    // That is, is there a path that leads to an accept state for both?
    public static boolean intersects(NFA a, NFA b) {
        Set<State> start1 = epsilonClosure(Set.of(a.start));
        Set<State> start2 = epsilonClosure(Set.of(b.start));

        Queue<Pair<Set<State>, Set<State>>> work = new LinkedList<>();
        Set<Pair<Set<State>, Set<State>>> visited = new HashSet<>();

        Pair<Set<State>, Set<State>> startPair = Pair.of(start1, start2);
        work.add(startPair);
        visited.add(startPair);

        while (!work.isEmpty()) {
            Pair<Set<State>, Set<State>> current = work.poll();

            // Are any of the current states accept states
            boolean accept1 = current.getLeft().stream().anyMatch(s -> s.accept);
            boolean accept2 = current.getRight().stream().anyMatch(s -> s.accept);

            if (accept1 && accept2) {
                return true;
            }

            // If not, collect outgoing edges
            Set<String> symbols = new HashSet<>();
            for (State s : current.getLeft()) symbols.addAll(s.transitions.keySet());
            for (State s : current.getRight()) symbols.addAll(s.transitions.keySet());

            // Move on all outgoing edges
            for (String sym : symbols) {
                Set<State> next1 = epsilonClosure(move(current.getLeft(), sym));
                Set<State> next2 = epsilonClosure(move(current.getRight(), sym));

                if (next1.isEmpty() || next2.isEmpty())
                    continue;

                Pair<Set<State>, Set<State>> nextPair = Pair.of(next1, next2);

                if (!visited.contains(nextPair)) {
                    visited.add(nextPair);
                    work.add(nextPair);
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NFA[start=").append(start).append(", [");

        if(start.epsilon.isEmpty() && start.transitions.isEmpty()) {
            sb.append("]");
            return sb.toString();
        }

        Set<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            State s = queue.poll();
            visited.add(s);

            for(State t : s.epsilon) {
                if(!visited.contains(t)) {
                    queue.add(t);
                }
                sb.append("[").append(s).append(" -> ").append(t).append("], ");
            }

            for(Map.Entry<String, Set<State>> entry : s.transitions.entrySet()) {
                for(State t : entry.getValue()) {
                    if(!visited.contains(t)) {
                        queue.add(t);
                    }
                    sb.append("[").append(s).append(" ->{").append(entry.getKey()).append("} ").append(t).append("], ");
                }
            }
        }

        sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
        return sb.toString();
    }
}

