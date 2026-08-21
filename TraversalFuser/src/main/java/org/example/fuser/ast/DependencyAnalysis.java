package org.example.fuser.ast;

import org.example.Main;
import org.example.fuser.tree.Node;
import org.example.fuser.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyAnalysis {

    // Performs the dependency analysis FOR REWRITE RULES
    // I.e. constructs the access paths
    // These are later converted to automata in the dependence graph
    // The dependence graph also creates the automata for calls

    // Find all the reads for a Node
    // (That is on the LHS of a rewrite rule)
    public static List<AccessPath> getReads(Node n) {
        List<AccessPath> accessPaths = new ArrayList<>();

        for(int i = 0; i < n.children.size(); i++) {
            Object child = n.children.get(i);
            if(child instanceof Node && (!Main.LITERAL_TYPES.contains(((Node) child).name))) {
                // This is not one of the Main.LITERAL_TYPES
                Node cn = (Node) child;
                // Var and Wld are never read
                if(cn.name.equals("Var") || cn.name.equals("Wld")) continue;
                // So, we are matching on a concrete value.
                // Create an access path, reading this node's name
                // And the values of its children if it has any
                AccessPath p = new AccessPath();
                p.add(i);
                p.add("name");
                accessPaths.add(p);
                List<AccessPath> childReads = getReads(cn);
                for(AccessPath c : childReads) {
                    c.prepend(i);
                    accessPaths.add(c);
                }
            } else {
                // This is one of the Main.LITERAL_TYPES
                // Create an access path for the contained value only
                // These nodes cannot have children
                AccessPath p = new AccessPath();
                p.add(i);
                accessPaths.add(p);
            }
        }

        return accessPaths;
    }

    // Find all the reads for a Node that is on the RHS of a rewrite rule
    // These can only be vars referencing a different path on the LHS
    // (the check whether the LHS=RHS happens elsewhere)
    public static List<AccessPath> getReadsRhs(Node n, Map<String, AccessPath> varMap,
                                               Map<String, List<AccessPath>> xVarMap) {
        List<AccessPath> accessPaths = new ArrayList<>();

        if(Main.LITERAL_TYPES.contains(n.name)) {
            return accessPaths;
        }

        if(n.name.equals("Var") && n.children.size() == 1 && n.children.get(0) instanceof String) {
            String cN = (String) n.children.get(0);
            if(varMap.containsKey(cN)) {
                AccessPath p = varMap.get(cN);
                accessPaths.add(p);
            } else if(xVarMap.containsKey(cN)) {
                accessPaths.addAll(xVarMap.get(cN));
            } else {
                throw new RuntimeException("Unknown variable: " + cN);
            }
            return accessPaths;
        }

        for(int i = 0; i < n.children.size(); i++) {
            Object child = n.children.get(i);
            if(child instanceof Node) {
                Node cn = (Node) child;
                List<AccessPath> childReads = getReadsRhs(cn, varMap, xVarMap);
                accessPaths.addAll(childReads);
            }
        }

        return accessPaths;
    }

    // Build a map of Var->Path for every variable in this Node
    public static Map<String, AccessPath> getVarMap(Node n) {
        Map<String, AccessPath> varMap = new HashMap<>();

        for(int i = 0; i < n.children.size(); i++) {
            Object child = n.children.get(i);
            if(child instanceof Node) {
                Node cn = (Node) child;
                if(cn.name.equals("Var")) {
                    AccessPath p = new AccessPath();
                    p.add(i);
                    Object varName = cn.children.get(0);
                    if(!(varName instanceof String)) {
                        throw new RuntimeException("Expected a String as Var name!");
                    }
                    String varNameStr = (String) varName;
                    varMap.put(varNameStr, p);
                } else {
                    Map<String, AccessPath> childVarMap = getVarMap(cn);
                    for(Map.Entry<String, AccessPath> e : childVarMap.entrySet()) {
                        AccessPath p = e.getValue();
                        p.prepend(i);
                        varMap.put(e.getKey(), p);
                    }
                }
            }
        }

        return varMap;
    }

    // Get the reads and writes that happen due to the RHS
    public static Pair<List<AccessPath>, List<AccessPath>> getRhsRW(Node lhs, Node rhs,
                                                                    Map<String, AccessPath> varMap,
                                                                    Map<String, List<AccessPath>> xVarMap) {
        List<AccessPath> reads = new ArrayList<>();
        List<AccessPath> writes = new ArrayList<>();

        // Nodes without children
        if(Main.LITERAL_TYPES.contains(rhs.name)) {
            if(lhs.equals(rhs)) return Pair.of(reads, writes);
            writes.add(new AccessPath());
            return Pair.of(reads, writes);
        }

        // Vars
        if(rhs.name.equals("Var")) {
            if(lhs.name.equals("Var") && lhs.children.get(0).equals(rhs.children.get(0))) {
                return Pair.of(reads, writes);
            }

            writes.add(new AccessPath());
            return Pair.of(getReadsRhs(rhs, varMap, xVarMap), writes);
        }

        // Otherwise (must be a match on a concrete value)
        if(!rhs.name.equals(lhs.name)) {
            AccessPath p = new AccessPath();
            p.add("name");
            writes.add(p);
        }
        for(int i = 0; i < rhs.children.size(); i++) {
            Object rh = rhs.children.get(i);
            if(i < lhs.children.size()) {
                Object lhsChildObject = lhs.children.get(i);
                if(lhsChildObject.equals(rh)) continue;
                if(!(rh instanceof Node)) {
                    AccessPath p = new AccessPath();
                    p.add(i);
                    writes.add(p);
                } else if(!(lhsChildObject instanceof Node) || ((Node) lhsChildObject).name.equals("Wld")) {
                    Node rhn = (Node) rh;
                    AccessPath p = new AccessPath();
                    p.add(i);
                    writes.add(p);
                    reads.addAll(getReadsRhs(rhn, varMap, xVarMap));
                } else {
                    Node rhn = (Node) rh;
                    Node lhn = (Node) lhsChildObject;
                    Pair<List<AccessPath>, List<AccessPath>> childRW = getRhsRW(lhn, rhn, varMap, xVarMap);
                    reads.addAll(childRW.getLeft());
                    for(AccessPath w : childRW.getRight()) {
                        w.prepend(i);
                        writes.add(w);
                    }
                }
            } else {
                if(!(rh instanceof Node)) {
                    AccessPath p = new AccessPath();
                    p.add(i);
                    writes.add(p);
                } else {
                    Node rhn = (Node) rh;
                    AccessPath p = new AccessPath();
                    p.add(i);
                    writes.add(p);
                    reads.addAll(getReadsRhs(rhn, varMap, xVarMap));
                }
            }
        }
        return Pair.of(reads, writes);
    }

    // Get all reads and writes for a rewrite rule
    public static Pair<List<AccessPath>, List<AccessPath>> getRuleRW(Node lhs, Node rhs,
                                                                     Map<String, List<String>> aliases) {
        // Get reads due to the LHS
        List<AccessPath> reads = DependencyAnalysis.getReads(lhs);
        // Build a var map of the LHS
        Map<String, AccessPath> varMap = getVarMap(lhs);
        // Build a direct var map for things like 'lift_app_in_build'
        Map<String, List<AccessPath>> xVarMap = getXVarMap(varMap, aliases);

        // Get all the reads and writes that happen due to the RHS
        Pair<List<AccessPath>, List<AccessPath>> rhsRW = getRhsRW(lhs, rhs, varMap, xVarMap);
        // Combine LHS and RHS reads
        reads.addAll(rhsRW.getLeft());

        return Pair.of(reads, rhsRW.getRight());
    }

    // Merge two RW pairs
    public static Pair<List<AccessPath>, List<AccessPath>> mergeRW(Pair<List<AccessPath>, List<AccessPath>> a,
                                                                   Pair<List<AccessPath>, List<AccessPath>> b) {
        List<AccessPath> l = a.getLeft();
        for(AccessPath p : b.getLeft()) {
            if(!l.contains(p)) l.add(p);
        }

        List<AccessPath> r = a.getRight();
        for(AccessPath p : b.getRight()) {
            if(!r.contains(p)) r.add(p);
        }

        return Pair.of(l, r);
    }

    // Build the direct var map for aliases
    public static Map<String, List<AccessPath>> getXVarMap(Map<String, AccessPath> varMap,
                                                           Map<String, List<String>> aliases) {
        Map<String, List<AccessPath>> xVarMap = new HashMap<>();

        for(Map.Entry<String, List<String>> e : aliases.entrySet()) {
            String varName = e.getKey();
            List<String> aliasList = e.getValue();
            List<AccessPath> aps = new ArrayList<>();
            for(String alias : aliasList) {
                aps.add(varMap.get(alias));
            }
            xVarMap.put(varName, aps);
        }

        return xVarMap;
    }

}

