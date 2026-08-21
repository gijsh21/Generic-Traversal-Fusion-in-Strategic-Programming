package org.example.fuser.graph;

import org.example.Main;
import org.example.fuser.ast.AccessPath;
import org.example.fuser.extract.ExtractFuserInfo;
import org.example.fuser.nfa.NFA;
import org.example.fuser.tree.AllStrategoNode;
import org.example.fuser.tree.RStrategoNode;
import org.example.fuser.tree.StrategoNode;
import org.example.fuser.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class DependenceGraphF {

    // Represents the dependence graph
    // Builds a graph from program nodes
    // Creates the automata for call nodes
    // Inserts edges etc.

    // This was an attempt to perform fusion more greedily
    // Avoiding having to compute all possible fusions
    // It is faster, but it does miss a lot
    // Would not necessarily recommend using it
    private static final int GREEDY_FUSION = 0;

    Map<String, Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> graph;

    Map<String, List<StrategoNode>> functionDefinitions;
    Map<String, Map<String, Pair<List<AccessPath>, List<AccessPath>>>> ruleAccessesPerConstructor;
    Pair<Set<String>, List<Pair<String, ExtractFuserInfo.AType>>> typeInfo;
    Map<String, List<String>> constructorsForTypes;
    Map<String, Map<String, NFA>> readNFAMap;
    Map<String, Map<String, NFA>> writeNFAMap;
    List<String> programOrder;

    public DependenceGraphF() {
        this.graph = new HashMap<>();
    }

    public Map<String, Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> getGraph() {
        return graph;
    }

    public Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> getGraph(
            String constructor) {
        return graph.get(constructor);
    }

    public void setInfo(Map<String, List<StrategoNode>> functionDefinitions,
                        Map<String, Map<String, Pair<List<AccessPath>, List<AccessPath>>>>
                                ruleAccessesPerConstructor,
                        Pair<Set<String>, List<Pair<String, ExtractFuserInfo.AType>>> typeInfo,
                        List<String> programOrder) {
        this.functionDefinitions = functionDefinitions;
        this.ruleAccessesPerConstructor = ruleAccessesPerConstructor;
        this.typeInfo = typeInfo;

        Map<String, List<String>> constructorsForTypes = new HashMap<>();
        for(Pair<String, ExtractFuserInfo.AType> constructor : typeInfo.getRight()) {
            String resType = constructor.getRight().getResultingType();
            if(constructorsForTypes.containsKey(resType)) {
                constructorsForTypes.get(resType).add(constructor.getLeft());
            } else {
                constructorsForTypes.put(resType, new ArrayList<>());
                constructorsForTypes.get(resType).add(constructor.getLeft());
            }
        }
        this.constructorsForTypes = constructorsForTypes;
        this.programOrder = programOrder;
    }

    // Build the graph!
    public void build() {
        readNFAMap = new HashMap<>();
        writeNFAMap = new HashMap<>();

        Map<String, Map<String, List<Pair<Vertex, Pair<NFA, NFA>>>>> verticesPerConstructor = new HashMap<>();

        // Build a graph for every constructor
        for(Pair<String, ExtractFuserInfo.AType> constructor : typeInfo.getRight()) {

            // Create nodes per function (=per traversal essentially)
            for(String functionDefinition : functionDefinitions.keySet()) {
                List<Pair<Vertex, Pair<NFA, NFA>>> vertices = new ArrayList<>();

                List<StrategoNode> nodes = functionDefinitions.get(functionDefinition);
                for(int i = 0; i < nodes.size(); i++) {
                    StrategoNode node = nodes.get(i);

                    // Insert a statement node and link it
                    // to R/W automata created from its access paths
                    if(node instanceof RStrategoNode) {
                        RStrategoNode rNode = (RStrategoNode) node;
                        Vertex v = new Vertex(functionDefinition + "[" + constructor.getLeft() + "]-s" + i,
                                rNode.name, Vertex.VertexType.STATEMENT);

                        Pair<List<AccessPath>, List<AccessPath>> aps = ruleAccessesPerConstructor
                                .get(rNode.name).get(constructor.getLeft());
                        vertices.add(Pair.of(v,
                                Pair.of(NFA.readNFAFromAccessPaths(aps.getLeft(), aps.getRight()),
                                        NFA.writeNFAFromAccessPaths(aps.getRight()))));
                    } else if(node instanceof AllStrategoNode) {
                        // Insert call nodes for every child
                        // Build R/W automata for these calls and link them
                        AllStrategoNode cNode = (AllStrategoNode) node;
                        for(int j = 0; j < constructor.getRight().getChildAmount(); j++) {
                            if(Main.LITERAL_TYPES.contains(constructor.getRight().getChildren().get(j))) continue;
                            Vertex c = new Vertex(functionDefinition + "[" + constructor.getLeft() + "]-c"
                                    + i + "-" + j, cNode.arg, Vertex.VertexType.CALL, j);
                            NFA.State readNFAInitialState = new NFA.State();
                            NFA readNFA = new NFA(readNFAInitialState);
                            NFA.State writeNFAInitialState = new NFA.State();
                            NFA writeNFA = new NFA(writeNFAInitialState);
                            for(String childConstructor :
                                    constructorsForTypes.getOrDefault(constructor.getRight().getChildren().get(j), List.of())) {
                                ExtractFuserInfo.AType childConstructorAType = typeInfo.getRight()
                                        .stream().filter(p -> p.getLeft().equals(childConstructor))
                                        .findFirst().orElseThrow().getRight();

                                extendReadNFAForAll(readNFA, cNode,
                                        Pair.of(childConstructor, childConstructorAType),
                                        j);

                                extendWriteNFAForAll(writeNFA, cNode,
                                        Pair.of(childConstructor, childConstructorAType),
                                        j);
                            }
                            vertices.add(Pair.of(c, Pair.of(readNFA, writeNFA)));
                        }
                    }
                }

                verticesPerConstructor
                        .computeIfAbsent(constructor.getLeft(), k -> new HashMap<>())
                        .put(functionDefinition, vertices);
            }
        }

        // The buildGraph method finally computes and inserts the edges into the graph
        // Based on the linked automata
        for(Map.Entry<String, Map<String, List<Pair<Vertex, Pair<NFA, NFA>>>>> entry
                : verticesPerConstructor.entrySet()) {
            graph.put(entry.getKey(), buildGraph(entry.getValue()));
        }
    }

    // Inserts edges into a graph based on conflicts in the linked R/W
    // automata
    private Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>
        buildGraph(Map<String, List<Pair<Vertex, Pair<NFA, NFA>>>> verticesPerFunction) {
        List<Pair<Vertex, Pair<NFA, NFA>>> vertices = new ArrayList<>();
        Map<Vertex, Pair<List<Integer>, Integer>> map = new HashMap<>();

        // Flatten the vertices for every function call
        // Into a single list containing all the vertices
        // for the entire program in program order
        int sCounter = 0;
        int cCounter = 0;
        for(int i = 0; i < programOrder.size(); i++) {
            String defCall = programOrder.get(i);
            for(Pair<Vertex, Pair<NFA, NFA>> vertex : verticesPerFunction.get(defCall)) {
                Pair<Vertex, Pair<NFA, NFA>> res;
                if(vertex.getLeft().type == Vertex.VertexType.CALL) {
                    Vertex v = new Vertex("c" + (cCounter++),
                            vertex.getLeft().info, Vertex.VertexType.CALL, vertex.getLeft().index);
                    res = Pair.of(v, vertex.getRight());
                } else {
                    Vertex v = new Vertex("s" + (sCounter++),
                            vertex.getLeft().info, Vertex.VertexType.STATEMENT);
                    res = Pair.of(v, vertex.getRight());
                }
                vertices.add(res);
                map.put(res.getLeft(), Pair.of(List.of(i), res.getLeft().index));
            }
        }

        // Compute the edges in program order
        List<Vertex> vxs = vertices.stream().map(Pair::getLeft).collect(Collectors.toList());
        List<Edge> edges = new ArrayList<>();
        for(int i = 0; i < vertices.size(); i++) {
            for(int j = i + 1; j < vertices.size(); j++) {
                if(dependencyConflict(vertices.get(i).getRight(), vertices.get(j).getRight())) {
                    edges.add(new Edge(vertices.get(i).getLeft(), vertices.get(j).getLeft()));
                }
            }
        }

        return Pair.of(Pair.of(vxs, edges), map);
    }

    public void extendReadNFAForAll(NFA nfa, AllStrategoNode call,
                                    Pair<String, ExtractFuserInfo.AType> constructorActingOn, int idx) {
        if(readNFAMap.containsKey(call.arg) &&
                readNFAMap.get(call.arg).containsKey(constructorActingOn.getLeft())) {
            NFA.addToStartWith(nfa, String.valueOf(idx), readNFAMap.get(call.arg).get(constructorActingOn.getLeft()));
        } else {
            NFA functionNFA = buildReadNFAForFunction(call.arg, constructorActingOn);
            NFA.addToStartWith(nfa, String.valueOf(idx), functionNFA);
        }
    }

    public void extendWriteNFAForAll(NFA nfa, AllStrategoNode call,
                                     Pair<String, ExtractFuserInfo.AType> constructorActingOn, int idx) {
        if(writeNFAMap.containsKey(call.arg) &&
            writeNFAMap.get(call.arg).containsKey(constructorActingOn.getLeft())) {
            NFA.addToStartWith(nfa, String.valueOf(idx), writeNFAMap.get(call.arg).get(constructorActingOn.getLeft()));
        } else {
            NFA functionNFA = buildWriteNFAForFunction(call.arg, constructorActingOn);
            NFA.addToStartWith(nfa, String.valueOf(idx), functionNFA);
        }
    }

    public NFA buildReadNFAForFunction(String function, Pair<String, ExtractFuserInfo.AType> constructor) {
        //NFA.State functionInitial = new NFA.State();
        //NFA functionNFA = new NFA(functionInitial);
        readNFAMap.computeIfAbsent(function, k -> new HashMap<>());
        /*if(readNFAMap.get(function).containsKey(constructor.getLeft())) {
            return readNFAMap.get(function).get(constructor.getLeft());
        }*/

        NFA.State functionInitial = new NFA.State();
        NFA functionNFA = new NFA(functionInitial);
        //functionNFA.acceptStates.add(functionInitial);
        functionInitial.accept = true;
        readNFAMap.get(function).put(constructor.getLeft(), functionNFA);

        List<StrategoNode> nodes = functionDefinitions.get(function);
        for(int i = 0; i < nodes.size(); i++) {
            StrategoNode node = nodes.get(i);

            if(node instanceof RStrategoNode) {
                RStrategoNode rNode = (RStrategoNode) node;
                Pair<List<AccessPath>, List<AccessPath>> aps = ruleAccessesPerConstructor
                        .get(rNode.name).get(constructor.getLeft());
                NFA.addToStart(functionNFA, NFA.readNFAFromAccessPaths(aps.getLeft(), aps.getRight()));
            } else if(node instanceof AllStrategoNode) {
                AllStrategoNode cNode = (AllStrategoNode) node;
                for(int j = 0; j < constructor.getRight().getChildAmount(); j++) {
                    for(String childConstructor :
                            constructorsForTypes.getOrDefault(constructor.getRight().getChildren().get(j), List.of())) {
                        ExtractFuserInfo.AType childConstructorAType = typeInfo.getRight()
                                .stream().filter(p -> p.getLeft().equals(childConstructor))
                                .findFirst().orElseThrow().getRight();
                        extendReadNFAForAll(functionNFA, cNode, Pair.of(childConstructor, childConstructorAType), j);
                        /*if(readNFAMap.containsKey(cNode.arg) &&
                                readNFAMap.get(cNode.arg).containsKey(childConstructor)) {
                            NFA.addToStartWith(functionNFA, String.valueOf(j), readNFAMap.get(cNode.arg).get(childConstructor));
                        } else {
                            ExtractFuserInfo.AType childConstructorAType = typeInfo.getRight()
                                    .stream().filter(p -> p.getLeft().equals(childConstructor))
                                    .findFirst().orElseThrow().getRight();
                            NFA childNFA = buildReadNFAForFunction(cNode.arg, Pair.of(childConstructor, childConstructorAType));
                            NFA.addToStartWith(functionNFA, String.valueOf(j), childNFA);
                        }*/
                    }
                }
            }
        }

        return functionNFA;
    }

    public NFA buildWriteNFAForFunction(String function, Pair<String, ExtractFuserInfo.AType> constructor) {
        //NFA.State functionInitial = new NFA.State();
        //NFA functionNFA = new NFA(new NFA.State());
        //writeNFAMap.computeIfAbsent(function, k -> new HashMap<>())
        //        .computeIfAbsent(constructor.getLeft(), k -> new NFA(new NFA.State()));
        //NFA functionNFA = writeNFAMap.get(function).get(constructor.getLeft());

        writeNFAMap.computeIfAbsent(function, k -> new HashMap<>());
        /*if(writeNFAMap.get(function).containsKey(constructor.getLeft())) {
            return writeNFAMap.get(function).get(constructor.getLeft());
        }*/

        NFA.State functionInitial = new NFA.State();
        NFA functionNFA = new NFA(functionInitial);
        writeNFAMap.get(function).put(constructor.getLeft(), functionNFA);

        List<StrategoNode> nodes = functionDefinitions.get(function);
        for(int i = 0; i < nodes.size(); i++) {
            StrategoNode node = nodes.get(i);

            if(node instanceof RStrategoNode) {
                RStrategoNode rNode = (RStrategoNode) node;
                Pair<List<AccessPath>, List<AccessPath>> aps = ruleAccessesPerConstructor
                        .get(rNode.name).get(constructor.getLeft());
                NFA.addToStart(functionNFA, NFA.writeNFAFromAccessPaths(aps.getRight()));
            } else if(node instanceof AllStrategoNode) {
                AllStrategoNode cNode = (AllStrategoNode) node;
                for(int j = 0; j < constructor.getRight().getChildAmount(); j++) {
                    for(String childConstructor :
                            constructorsForTypes.getOrDefault(constructor.getRight().getChildren().get(j), List.of())) {
                        ExtractFuserInfo.AType childConstructorAType = typeInfo.getRight()
                                .stream().filter(p -> p.getLeft().equals(childConstructor))
                                .findFirst().orElseThrow().getRight();
                        extendWriteNFAForAll(functionNFA, cNode, Pair.of(childConstructor, childConstructorAType), j);
                        /*if(writeNFAMap.containsKey(cNode.arg) &&
                                writeNFAMap.get(cNode.arg).containsKey(childConstructor)) {
                            NFA.addToStartWith(functionNFA, String.valueOf(j), writeNFAMap.get(cNode.arg).get(childConstructor));
                        } else {
                            ExtractFuserInfo.AType childConstructorAType = typeInfo.getRight()
                                    .stream().filter(p -> p.getLeft().equals(childConstructor))
                                    .findFirst().orElseThrow().getRight();
                            NFA childNFA = buildWriteNFAForFunction(cNode.arg, Pair.of(childConstructor, childConstructorAType));
                            NFA.addToStartWith(functionNFA, String.valueOf(j), childNFA);
                        }*/
                    }
                }
            }
        }

        return functionNFA;
    }

    // Reads A intersect Writes B, Reads B intersect Writes A
    // Or Writes A intersect Writes B
    public boolean dependencyConflict(Pair<NFA, NFA> a, Pair<NFA, NFA> b) {
        if(NFA.intersects(a.getLeft(), b.getRight())) return true;
        if(NFA.intersects(a.getRight(), b.getLeft())) return true;
        return NFA.intersects(a.getRight(), b.getRight());
    }

    // Compute all the fusions!
    public Map<String, List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>>> fuse() {
        Map<String, List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>>> res =
                new HashMap<>();
        for(Map.Entry<String, Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> entry
            : graph.entrySet()) {
            List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> fuses =
                    fuseT(entry.getValue());
            res.put(entry.getKey(), fuses);
        }
        return res;
    }

    // Compute all the fusions for this specific graph (i.e. specified to a constructor)
    public List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> fuseT(
            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> graph
    ) {
        List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> fuses
                = new ArrayList<>();
        fuses.add(graph);

        Queue<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> queue
                = new ArrayDeque<>();
        queue.offer(graph);

        Set<Set<Vertex>> seen = new HashSet<>();
        while(!queue.isEmpty()) {
            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> curr
                    = queue.poll();

            List<Vertex> vertices = curr.getLeft().getLeft();
            List<Edge> edges = curr.getLeft().getRight();
            Map<Vertex, Pair<List<Integer>, Integer>> label = curr.getRight();

            // Only merge calls to the same child
            // The list of lists is indexed by child number
            // Then, the list at the index of a child will
            // contain all vertices that are calls on that child
            // And thus can theoretically be fused together
            List<List<Vertex>> mergeables = new ArrayList<>();
            for(Vertex v : vertices) {
                Pair<List<Integer>, Integer> l = label.get(v);
                if(v.type == Vertex.VertexType.CALL && l.getRight() >= 0) {
                    while(mergeables.size() <= l.getRight()) {
                        mergeables.add(new ArrayList<>());
                    }
                    mergeables.get(l.getRight()).add(v);
                }
            }

            // Now, compute all the possible combinations of fuses
            // Depending on these mergeables

            if(GREEDY_FUSION == 2) {
                // Just fuse all the mergeables together
                Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> merged = curr;
                for(List<Vertex> mergeable : mergeables) {
                    merged = mergeGreedy(merged, mergeable);
                }
                Set<Vertex> vs = new HashSet<>(merged.getLeft().getLeft());
                if(seen.contains(vs)) continue;
                if(!hasCycle(merged.getLeft())) {
                    fuses.add(merged);
                    queue.offer(merged);
                    seen.add(vs);
                }
            } else if(GREEDY_FUSION == 1) {
                // Fuse multiple mergeables from different mergeable lists at the same time
                // So fusions of multiple calls on different children are analyzed at the same time
                if(mergeables.isEmpty()) continue;
                int maxMergeableSize = mergeables.stream().mapToInt(List::size).max().getAsInt();
                for(int i = 0; i < maxMergeableSize; i++) {
                    for(int j = i + 1; j < maxMergeableSize; j++) {
                        Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> merged
                                = curr;
                        for(List<Vertex> mergeable : mergeables) {
                            if(mergeable.size() <= j) continue;
                            merged = merge(merged, mergeable.get(i), mergeable.get(j));
                        }
                        Set<Vertex> vs = new HashSet<>(merged.getLeft().getLeft());
                        if(seen.contains(vs)) continue;
                        if(!hasCycle(merged.getLeft())) {
                            fuses.add(merged);
                            queue.offer(merged);
                            seen.add(vs);
                        }
                    }
                }
            } else if(GREEDY_FUSION == 0) {
                // Compute every possibility, analyze every pair of mergeables independently
                for(List<Vertex> mergeable : mergeables) {
                    for(int i = 0; i < mergeable.size(); i++) {
                        for(int j = i + 1; j < mergeable.size(); j++) {
                            Set<Vertex> vs = mergeTest(curr.getLeft().getLeft(),
                                    mergeable.get(i), mergeable.get(j));
                            if(seen.contains(vs)) continue;
                            seen.add(vs);

                            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> merged =
                                    merge(curr, mergeable.get(i), mergeable.get(j));
                            if(!hasCycle(merged.getLeft())) {
                                fuses.add(merged);
                                queue.offer(merged);
                            }
                        }
                    }
                }
            }
        }

        return fuses;
    }

    // Used for testing whether this merge has already been seen before
    private Set<Vertex> mergeTest(List<Vertex> initial, Vertex a, Vertex b) {
        Set<Vertex> res = new HashSet<>(initial);
        res.remove(a);
        res.remove(b);

        res.add(new Vertex(a.name + " + " + b.name, a.info + " + " + b.info,
                a.type, a.index));

        return res;
    }

    // Merges two vertices in a graph
    // Takes care of removing the old vertices and inserting a new one
    // Removing and redirecting edges
    // And setting the correct vertex info (originating traversals etc.)
    private Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> merge(
            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> graph,
            Vertex v1, Vertex v2
    ) {
        if(v1.type != v2.type || v1.index != v2.index) {
            throw new RuntimeException("Attempted to merge vertices that cannot be merged!");
        }
        Vertex v12 = new Vertex(v1.name + " + " + v2.name, v1.info + " + " + v2.info,
                v1.type, v1.index);

        List<Vertex> vertices = new ArrayList<>(graph.getLeft().getLeft());
        vertices.remove(v1);
        vertices.remove(v2);
        vertices.add(v12);

        List<Edge> edges = new ArrayList<>();
        for(Edge e : graph.getLeft().getRight()) {
            if(e.v1.equals(v1) || e.v1.equals(v2) || e.v2.equals(v1) || e.v2.equals(v2)) {
                Edge ne = new Edge(e.v1, e.v2);
                if(ne.v1.equals(v1)) ne.v1 = v12;
                if(ne.v1.equals(v2)) ne.v1 = v12;
                if(ne.v2.equals(v1)) ne.v2 = v12;
                if(ne.v2.equals(v2)) ne.v2 = v12;
                if(ne.v1.equals(v12) && ne.v2.equals(v12)) continue;
                edges.add(ne);
            } else {
                edges.add(e);
            }
        }

        Map<Vertex, Pair<List<Integer>, Integer>> label = new HashMap<>(graph.getRight());
        Pair<List<Integer>, Integer> v1label = label.get(v1);
        Pair<List<Integer>, Integer> v2label = label.get(v2);
        List<Integer> v12t = new ArrayList<>(v1label.getLeft());
        v12t.addAll(v2label.getLeft());
        Pair<List<Integer>, Integer> v12label = Pair.of(v12t, v1label.getRight());
        label.remove(v1);
        label.remove(v2);
        label.put(v12, v12label);

        return Pair.of(Pair.of(vertices, edges), label);
    }

    // Merge an entire list of vertices together in a graph. Same logic as normal
    // merge method generalized to a list. But it's called mergeGreedy because it
    // is only used in the greedy fusion.
    private Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> mergeGreedy(
            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> graph,
            List<Vertex> vs
    ) {
        if(vs.size() <= 1) return graph;
        Vertex.VertexType vType = vs.get(0).type;
        int vIndex = vs.get(0).index;
        if(vs.stream().anyMatch(v -> v.type != vType || v.index != vIndex)) {
            throw new RuntimeException("Attempted to merge vertices that cannot be merged!");
        }

        StringBuilder mergedName = new StringBuilder();
        StringBuilder mergedInfo = new StringBuilder();
        for(Vertex v : vs) {
            mergedName.append(v.name).append(" + ");
            mergedInfo.append(v.info).append(" + ");
        }
        mergedName.delete(mergedName.length() - 3, mergedName.length());
        mergedInfo.delete(mergedInfo.length() - 3, mergedInfo.length());
        Vertex vMerged = new Vertex(mergedName.toString(), mergedInfo.toString(), vType, vIndex);

        List<Vertex> vertices = new ArrayList<>(graph.getLeft().getLeft());
        vertices.removeAll(vs);
        vertices.add(vMerged);

        List<Edge> edges = new ArrayList<>();
        for(Edge e : graph.getLeft().getRight()) {
            if(vs.stream().anyMatch(v -> e.v1.equals(v) || e.v2.equals(v))) {
                Edge ne = new Edge(e.v1, e.v2);
                if(vs.stream().anyMatch(v -> e.v1.equals(v))) {
                    ne.v1 = vMerged;
                }
                if(vs.stream().anyMatch(v -> e.v2.equals(v))) {
                    ne.v2 = vMerged;
                }
                if(ne.v1.equals(vMerged) && ne.v2.equals(vMerged)) continue;
                edges.add(ne);
            } else {
                edges.add(e);
            }
        }

        Map<Vertex, Pair<List<Integer>, Integer>> label = new HashMap<>(graph.getRight());
        List<Pair<List<Integer>, Integer>> labels = vs.stream().map(label::get).collect(Collectors.toList());

        List<Integer> vMergedT = new ArrayList<>();
        for(Pair<List<Integer>, Integer> lbl : labels) {
            vMergedT.addAll(lbl.getLeft());
        }

        Pair<List<Integer>, Integer> vMergedLabel = Pair.of(vMergedT, labels.get(0).getRight());

        for(Vertex v : vs) {
            label.remove(v);
        }

        label.put(vMerged, vMergedLabel);

        return Pair.of(Pair.of(vertices, edges), label);
    }

    // Tests whether a graph contains a cycle.
    private boolean hasCycle(Pair<List<Vertex>, List<Edge>> graph) {
        Map<Vertex, List<Vertex>> adjacencyList = new HashMap<>();

        Map<Vertex, Boolean> visited = new HashMap<>();
        Map<Vertex, Boolean> stack = new HashMap<>();
        for(Vertex v : graph.getLeft()) {
            adjacencyList.put(v, new ArrayList<>());
            visited.put(v, false);
            stack.put(v, false);
        }

        for(Edge e : graph.getRight()) {
            adjacencyList.get(e.v1).add(e.v2);
        }

        for(Vertex v : graph.getLeft()) {
            if(!visited.get(v)) {
                if(dfs(v, adjacencyList, visited, stack)) return true;
            }
        }

        return false;
    }

    // Returns a topological order of the graph
    public static List<Vertex> topoSort(Pair<List<Vertex>, List<Edge>> graph) {
        List<Vertex> res = new ArrayList<>();

        // Build in-degree map
        Map<Vertex, Integer> inDegree = new HashMap<>();
        for(Vertex v : graph.getLeft()) {
            inDegree.put(v, 0);
        }
        for(Edge e : graph.getRight()) {
            inDegree.put(e.v2, inDegree.get(e.v2) + 1);
        }

        // Find vertex without incoming edges
        // Remove it and its edges
        // Repeat
        while(!inDegree.isEmpty()) {
            for(Vertex v : inDegree.keySet()) {
                if(inDegree.get(v) == 0) {
                    res.add(v);
                    inDegree.remove(v);
                    for(Edge e : graph.getRight()) {
                        if(e.v1.equals(v)) {
                            inDegree.put(e.v2, inDegree.get(e.v2) - 1);
                        }
                    }
                    break;
                }
            }
        }

        return res;
    }

    // Depth first search from a vertex v for any repeats
    // Used for cycle detection
    private boolean dfs(Vertex v, Map<Vertex, List<Vertex>> adj,
                       Map<Vertex, Boolean> visited, Map<Vertex, Boolean> stack) {
        visited.put(v, true);
        stack.put(v, true);

        for(Vertex neighbor : adj.get(v)) {
            if(!visited.get(neighbor)) {
                if(dfs(neighbor, adj, visited, stack)) return true;
            } else if(stack.get(neighbor)) return true;
        }

        stack.put(v, false);
        return false;
    }


    // Same logic as the normal graph building, but does not compute automata
    // This is used only for the unfused version
    public void buildInOrder() {
        Map<String, Map<String, List<Pair<Vertex, Pair<NFA, NFA>>>>> verticesPerConstructor = new HashMap<>();

        for(Pair<String, ExtractFuserInfo.AType> constructor : typeInfo.getRight()) {
            for(String functionDefinition : functionDefinitions.keySet()) {
                List<Pair<Vertex, Pair<NFA, NFA>>> vertices = new ArrayList<>();

                List<StrategoNode> nodes = functionDefinitions.get(functionDefinition);
                for(int i = 0; i < nodes.size(); i++) {
                    StrategoNode node = nodes.get(i);

                    if(node instanceof RStrategoNode) {
                        RStrategoNode rNode = (RStrategoNode) node;
                        Vertex v = new Vertex(functionDefinition + "[" + constructor.getLeft() + "]-s" + i,
                                rNode.name, Vertex.VertexType.STATEMENT);

                        vertices.add(Pair.of(v, Pair.of(null, null)));
                    } else if(node instanceof AllStrategoNode) {
                        AllStrategoNode cNode = (AllStrategoNode) node;
                        for(int j = 0; j < constructor.getRight().getChildAmount(); j++) {
                            Vertex c = new Vertex(functionDefinition + "[" + constructor.getLeft() + "]-c"
                                    + i + "-" + j, cNode.arg, Vertex.VertexType.CALL, j);
                            vertices.add(Pair.of(c, Pair.of(null, null)));
                        }
                    }
                }

                verticesPerConstructor
                        .computeIfAbsent(constructor.getLeft(), k -> new HashMap<>())
                        .put(functionDefinition, vertices);
            }
        }

        for(Map.Entry<String, Map<String, List<Pair<Vertex, Pair<NFA, NFA>>>>> entry
                : verticesPerConstructor.entrySet()) {
            graph.put(entry.getKey(), buildGraphInOrder(entry.getValue()));
        }
    }

    // Same logic as normal version, does not insert dependency edges.
    // But inserts edges in program order at the end for every node
    // To ensure no code motion takes place
    private Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>
    buildGraphInOrder(Map<String, List<Pair<Vertex, Pair<NFA, NFA>>>> verticesPerFunction) {
        List<Pair<Vertex, Pair<NFA, NFA>>> vertices = new ArrayList<>();
        Map<Vertex, Pair<List<Integer>, Integer>> map = new HashMap<>();

        int sCounter = 0;
        int cCounter = 0;
        for(int i = 0; i < programOrder.size(); i++) {
            String defCall = programOrder.get(i);
            for(Pair<Vertex, Pair<NFA, NFA>> vertex : verticesPerFunction.get(defCall)) {
                Pair<Vertex, Pair<NFA, NFA>> res;
                if(vertex.getLeft().type == Vertex.VertexType.CALL) {
                    Vertex v = new Vertex("c" + (cCounter++),
                            vertex.getLeft().info, Vertex.VertexType.CALL, vertex.getLeft().index);
                    res = Pair.of(v, vertex.getRight());
                } else {
                    Vertex v = new Vertex("s" + (sCounter++),
                            vertex.getLeft().info, Vertex.VertexType.STATEMENT);
                    res = Pair.of(v, vertex.getRight());
                }
                vertices.add(res);
                map.put(res.getLeft(), Pair.of(List.of(i), res.getLeft().index));
            }
        }

        List<Vertex> vxs = vertices.stream().map(Pair::getLeft).collect(Collectors.toList());
        List<Edge> edges = new ArrayList<>();
        for(int i = 0; i < vertices.size() - 1; i++) {
            edges.add(new Edge(vertices.get(i).getLeft(), vertices.get(i + 1).getLeft()));
        }

        return Pair.of(Pair.of(vxs, edges), map);
    }

}

