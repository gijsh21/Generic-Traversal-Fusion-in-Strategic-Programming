package org.example.fuser.graph;

import org.example.fuser.ast.AccessPath;
import org.example.fuser.ast.Traversal;
import org.example.fuser.extract.ExtractFuserInfo;
import org.example.fuser.nfa.NFA;
import org.example.fuser.tree.AllStrategoNode;
import org.example.fuser.tree.RStrategoNode;
import org.example.fuser.tree.StrategoNode;
import org.example.fuser.util.Pair;
import org.example.fuser.util.Triple;

import java.util.*;

public class DependenceGraph {

    /*Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> graph;
    int maxChildrenAmount;

    public DependenceGraph(int maxChildrenAmount) {
        this.graph = null;
        this.maxChildrenAmount = maxChildrenAmount;
    }

    public Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> getGraph() {
        return graph;
    }

    public void buildFromFunctionDefinitions(Map<String, List<StrategoNode>> functionDefinitions,
                                             Map<String, Map<String, Pair<List<AccessPath>, List<AccessPath>>>>
                                                     ruleAccessesPerConstructor,
                                             Pair<Set<String>, List<Pair<String, ExtractFuserInfo.AType>>> typeInfo) {

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

        Map<String, Map<String, List<List<Pair<Vertex, Pair<NFA, NFA>>>>>> verticesPerConstructor = new HashMap<>();
        for(String functionDefinition : functionDefinitions.keySet()) {
            verticesPerConstructor.put(functionDefinition, new HashMap<>());
        }

        for(Pair<String, ExtractFuserInfo.AType> constructor : typeInfo.getRight()) {
            for(String functionDefinition : functionDefinitions.keySet()) {
                List<List<Pair<Vertex, Pair<NFA, NFA>>>> vertices = new ArrayList<>();

                List<StrategoNode> nodes = functionDefinitions.get(functionDefinition);
                for(int i = 0; i < nodes.size(); i++) {
                    StrategoNode node = nodes.get(i);

                    if(node instanceof RStrategoNode) {
                        RStrategoNode rNode = (RStrategoNode) node;
                        List<Pair<Vertex, Pair<NFA, NFA>>> nVertices = new ArrayList<>();
                        Vertex v = new Vertex(functionDefinition + "[" + constructor.getLeft() + "]-s" + i,
                                rNode.name);

                        Pair<List<AccessPath>, List<AccessPath>> aps = ruleAccessesPerConstructor
                                .get(rNode.name).get(constructor.getLeft());
                        nVertices.add(Pair.of(v,
                                Pair.of(NFA.readNFAFromAccessPaths(aps.getLeft(), aps.getRight()),
                                        NFA.writeNFAFromAccessPaths(aps.getRight()))));
                        vertices.add(nVertices);
                    } else if(node instanceof AllStrategoNode) {
                        AllStrategoNode cNode = (AllStrategoNode) node;
                        for(int j = 0; j < constructor.getRight().getChildAmount(); j++) {
                            List<Pair<Vertex, Pair<NFA, NFA>>> nVertices = new ArrayList<>();
                            for(String childConstructor :
                                    constructorsForTypes.get(constructor.getRight().getChildren().get(j))) {
                                ExtractFuserInfo.AType childConstructorAType = typeInfo.getRight()
                                        .stream().filter(p -> p.getLeft().equals(childConstructor))
                                        .findFirst().orElseThrow().getRight();
                                Vertex c = new Vertex(functionDefinition + "[" + constructor.getLeft() + "]-c"
                                        + i + "-" + j + "[" + childConstructor + "]");

                                NFA.State readNFAInitialState = new NFA.State();
                                NFA readNFA = new NFA(readNFAInitialState);
                                Map<String, Map<String, NFA.State>> readNFAMap = new HashMap<>();
                                readNFA = buildReadNFAForCall(cNode, childConstructor, readNFA, readNFAMap,
                                        functionDefinitions, ruleAccessesPerConstructor,
                                        typeInfo, constructorsForTypes);

                                NFA.State writeNFAInitialState = new NFA.State();
                                NFA writeNFA = new NFA(writeNFAInitialState);
                                Map<String, Map<String, NFA>> writeNFAMap = new HashMap<>();
                                writeNFA = buildWriteNFAForCall(cNode,
                                        Pair.of(childConstructor, childConstructorAType),
                                        constructor.getRight().getChildren().get(j),
                                        j, writeNFA, writeNFAMap,
                                        functionDefinitions, ruleAccessesPerConstructor,
                                        typeInfo, constructorsForTypes);

                                nVertices.add(Pair.of(c, Pair.of(readNFA, writeNFA)));
                            }
                            vertices.add(nVertices);
                        }
                    }
                }

                verticesPerConstructor.get(functionDefinition).put(constructor.getLeft(), vertices);
            }
        }

    }

    public NFA buildReadNFAForCall(AllStrategoNode call, String constructorActingOn,
                                   NFA nfa, Map<String, Map<String, NFA.State>> fStates,
                                   Map<String, List<StrategoNode>> functionDefinitions,
                                   Map<String, Map<String, Pair<List<AccessPath>, List<AccessPath>>>>
                                           ruleAccessesPerConstructor,
                                   Pair<Set<String>, List<Pair<String, ExtractFuserInfo.AType>>> typeInfo,
                                   Map<String, List<String>> constructorsForTypes) {
        return null;
    }

    public NFA buildWriteNFAForCall(AllStrategoNode call, Pair<String, ExtractFuserInfo.AType> constructorActingOn,
                                    String typeActingOn,
                                    int idx,
                                    NFA nfa, Map<String, Map<String, NFA>> fStates,
                                    Map<String, List<StrategoNode>> functionDefinitions,
                                    Map<String, Map<String, Pair<List<AccessPath>, List<AccessPath>>>>
                                           ruleAccessesPerConstructor,
                                    Pair<Set<String>, List<Pair<String, ExtractFuserInfo.AType>>> typeInfo,
                                    Map<String, List<String>> constructorsForTypes) {
        //NFA.State iState = fStates.get(call.call).get(constructorActingOn);
        NFA.State iState = nfa.start;

        if(fStates.containsKey(call.call) &&
            fStates.get(call.call).containsKey(constructorActingOn.getLeft())) {
            NFA.addToStartWith(nfa, String.valueOf(idx), fStates.get(call.call).get(constructorActingOn.getLeft()));
        } else {
            NFA.State functionInitial = new NFA.State();
            NFA functionNFA = new NFA(functionInitial);
            fStates.computeIfAbsent(call.call, k -> new HashMap<>())
                    .put(constructorActingOn.getLeft(), functionNFA);

            List<StrategoNode> nodes = functionDefinitions.get(call.call);
            for(int i = 0; i < nodes.size(); i++) {
                StrategoNode node = nodes.get(i);

                if(node instanceof RStrategoNode) {
                    RStrategoNode rNode = (RStrategoNode) node;
                    Pair<List<AccessPath>, List<AccessPath>> aps = ruleAccessesPerConstructor
                            .get(rNode.name).get(constructorActingOn.getLeft());
                    NFA.addToStart(functionNFA, NFA.writeNFAFromAccessPaths(aps.getRight()));
                } else if(node instanceof AllStrategoNode) {
                    AllStrategoNode cNode = (AllStrategoNode) node;
                    for(int j = 0; j < constructorActingOn.getRight().getChildAmount(); j++) {
                        for(String childConstructor :
                                constructorsForTypes.get(constructorActingOn.getRight().getChildren().get(j))) {
                            if(fStates.containsKey(cNode.call) &&
                                fStates.get(cNode.call).containsKey(childConstructor)) {
                               NFA.addToStartWith(functionNFA, String.valueOf(j), fStates.get(cNode.call).get(childConstructor));
                            } else {
                                // ?
                            }
                        }
                    }
                }
            }

            NFA.addToStartWith(nfa, String.valueOf(idx), functionNFA);
        }

    }

    public void buildArbitraryAmount(
            List<Pair<Traversal, List<String>>> traversals,
            Map<String, Map<String, Pair<List<AccessPath>, List<AccessPath>>>> ruleAccessesPerConstructor,
            Pair<Set<String>, List<Pair<String, ExtractFuserInfo.AType>>> typeInfo) {

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

        List<Vertex> vertices = new ArrayList<>();
        //Map<Vertex, Pair<List<Integer>, Integer>> map = new HashMap<>();

        Map<String, List<Pair<Vertex, Pair<NFA, NFA>>>> statementsPerConstructor = new HashMap<>();

        for(Pair<String, ExtractFuserInfo.AType> constructor : typeInfo.getRight()) {
            for(int i = 0; i < traversals.size(); i++) {
                List<String> statementNames = traversals.get(i).getRight();
                List<Pair<Vertex, Pair<NFA, NFA>>> statementNFAs = new ArrayList<>();
                for (int j = 0; j < statementNames.size(); j++) {
                    Pair<List<AccessPath>, List<AccessPath>> statements = ruleAccessesPerConstructor
                            .get(statementNames.get(j)).get(constructor.getLeft());

                    Vertex v = new Vertex("s[" + constructor.getLeft() + "]" + i + "-" + j,
                            statementNames.get(j));
                    vertices.add(v);
                    //map.put(v, Pair.of(new ArrayList<>(List.of(i)), -1));

                    statementNFAs.add(Pair.of(v,
                            Pair.of(NFA.readNFAFromAccessPaths(statements.getLeft(), statements.getRight()),
                                    NFA.writeNFAFromAccessPaths(statements.getRight()))));
                }
            }
        }

        for(Pair<String, ExtractFuserInfo.AType> constructor : typeInfo.getRight()) {
            for(int i = 0; i < traversals.size(); i++) {
                List<String> statementNames = traversals.get(i).getRight();
                List<Pair<Vertex, Pair<NFA, NFA>>> statementNFAs = new ArrayList<>();
                for(int j = 0; j < statementNames.size(); j++) {
                    Pair<List<AccessPath>, List<AccessPath>> statements = ruleAccessesPerConstructor
                            .get(statementNames.get(j)).get(constructor.getLeft());

                    Vertex v = new Vertex("s[" + constructor.getLeft() + "]" + i + "-" + j,
                            statementNames.get(j));
                    vertices.add(v);
                    map.put(v, Pair.of(new ArrayList<>(List.of(i)), -1));

                    statementNFAs.add(Pair.of(v,
                            Pair.of(NFA.readNFAFromAccessPaths(statements.getLeft(), statements.getRight()),
                                    NFA.writeNFAFromAccessPaths(statements.getRight()))));
                }

                List<Vertex> currCalls = new ArrayList<>();

                for (int j = 0; j < constructor.getRight().getChildAmount(); j++) {
                    for(String childConstructor :
                            constructorsForTypes.get(constructor.getRight().getChildren().get(j))) {
                        Vertex c = new Vertex("c" + i + "[" + constructor.getLeft() + "]-"
                                + j + "[" + childConstructor + "]");

                        vertices.add(c);
                        currCalls.add(c);

                        map.put(c, Pair.of(new ArrayList<>(List.of(i)), j));
                    }
                }
            }
        }


        if(maxChildrenAmount < 1) {
            throw new RuntimeException("maxChildrenAmount < 1");
        }
        if(traversals.size() < 2) {
            throw new RuntimeException("traversals < 2");
        }

        List<Vertex> vertices = new ArrayList<>();
        Map<Vertex, Pair<List<Integer>, Integer>> map = new HashMap<>();

        List<Edge> edges = new ArrayList<>();

        List<Pair<Vertex, Pair<List<NFA>, List<NFA>>>> targetsOrdered = new ArrayList<>();

        NFA postFix = NFA.symbol("0");
        for(int i = 1; i < maxChildrenAmount; i++) {
            postFix = NFA.union(postFix, NFA.symbol(String.valueOf(i)));
        }
        postFix = NFA.star(postFix);



        for(int i = 0; i < traversals.size(); i++) {
            List<Vertex> currCalls = new ArrayList<>();

            for(int j = 0; j < maxChildrenAmount; j++) {
                Vertex c = new Vertex("c" + i + "-" + j);

                vertices.add(c);
                currCalls.add(c);

                map.put(c, Pair.of(new ArrayList<>(List.of(i)), j));
            }

            List<Triple<String, List<AccessPath>, List<AccessPath>>> statements = traversals.get(i).getRight();
            List<Pair<Vertex, Pair<List<NFA>, List<NFA>>>> statementTargets = new ArrayList<>();
            for(int j = 0; j < statements.size(); j++) {
                Vertex v = new Vertex("s" + i + "-" + j, statements.get(j).getLeft());
                vertices.add(v);
                map.put(v, Pair.of(new ArrayList<>(List.of(i)), -1));

                List<NFA> read = new ArrayList<>();
                for(AccessPath ap : statements.get(j).getMiddle()) {
                    read.add(NFA.fromAccessPath(ap));
                }
                List<NFA> write = new ArrayList<>();
                for(AccessPath ap : statements.get(j).getRight()) {
                    write.add(NFA.fromAccessPath(ap));
                }

                statementTargets.add(Pair.of(v, Pair.of(read, write)));
            }

            List<Pair<Vertex, Pair<List<NFA>, List<NFA>>>> callTargets = new ArrayList<>();
            for(int j = 0; j < maxChildrenAmount; j++) {
                NFA prefix = NFA.concat(NFA.symbol(String.valueOf(j)), postFix);

                List<NFA> cReads = new ArrayList<>();
                List<NFA> cWrites = new ArrayList<>();

                for(Pair<Vertex, Pair<List<NFA>, List<NFA>>> stm : statementTargets) {
                    for(NFA nfa : stm.getRight().getLeft()) {
                        cReads.add(NFA.concat(prefix, nfa));
                    }
                    for(NFA nfa : stm.getRight().getRight()) {
                        cWrites.add(NFA.concat(prefix, nfa));
                    }
                }

                callTargets.add(Pair.of(currCalls.get(j), Pair.of(cReads, cWrites)));
            }

            Traversal t = traversals.get(i).getLeft();
            if(t == Traversal.TOP_DOWN) {
                targetsOrdered.addAll(statementTargets);
                targetsOrdered.addAll(callTargets);
            } else if(t == Traversal.BOTTOM_UP) {
                targetsOrdered.addAll(callTargets);
                targetsOrdered.addAll(statementTargets);
            }
        }

        for(int i = 0; i < targetsOrdered.size(); i++) {
            Pair<Vertex, Pair<List<NFA>, List<NFA>>> target = targetsOrdered.get(i);
            edges.addAll(computeEdges(target.getLeft(), target.getRight().getLeft(),
                    target.getRight().getRight(), targetsOrdered.subList(i + 1, targetsOrdered.size())));
        }

        graph = Pair.of(Pair.of(vertices, edges), map);
    }

    public void build(Pair<List<AccessPath>, List<AccessPath>> r1,
                      Traversal t1,
                      Pair<List<AccessPath>, List<AccessPath>> r2,
                      Traversal t2,
                      String r1Name, String r2Name) {
        if(maxChildrenAmount < 1) {
            throw new RuntimeException("maxChildrenAmount < 1");
        }

        List<Vertex> vertices = new ArrayList<>();
        Map<Vertex, Pair<List<Integer>, Integer>> map = new HashMap<>();

        Vertex s1 = new Vertex(r1Name);
        Vertex s2 = new Vertex(r2Name);

        vertices.add(s1);
        vertices.add(s2);

        map.put(s1, Pair.of(new ArrayList<>(List.of(1)), -1));
        map.put(s2, Pair.of(new ArrayList<>(List.of(2)), -1));

        List<Vertex> calls1 = new ArrayList<>();
        List<Vertex> calls2 = new ArrayList<>();
        for(int i = 0; i < maxChildrenAmount; i++) {
            Vertex c1 = new Vertex("c1-" + i);
            Vertex c2 = new Vertex("c2-" + i);

            vertices.add(c1);
            calls1.add(c1);
            vertices.add(c2);
            calls2.add(c2);

            map.put(c1, Pair.of(new ArrayList<>(List.of(1)), i));
            map.put(c2, Pair.of(new ArrayList<>(List.of(2)), i));
        }

        List<Edge> edges = new ArrayList<>();

        List<NFA> r1read = new ArrayList<>();
        for(AccessPath ap : r1.getLeft()) {
            r1read.add(NFA.fromAccessPath(ap));
        }
        List<NFA> r1write = new ArrayList<>();
        for(AccessPath ap : r1.getRight()) {
            r1write.add(NFA.fromAccessPath(ap));
        }
        List<NFA> r2read = new ArrayList<>();
        for(AccessPath ap : r2.getLeft()) {
            r2read.add(NFA.fromAccessPath(ap));
        }
        List<NFA> r2write = new ArrayList<>();
        for(AccessPath ap : r2.getRight()) {
            r2write.add(NFA.fromAccessPath(ap));
        }

        List<Pair<List<NFA>, List<NFA>>> callRWs1 = new ArrayList<>();
        List<Pair<List<NFA>, List<NFA>>> callRWs2 = new ArrayList<>();
        NFA postFix = NFA.symbol("0");
        for(int i = 1; i < maxChildrenAmount; i++) {
            postFix = NFA.union(postFix, NFA.symbol(String.valueOf(i)));
        }
        postFix = NFA.star(postFix);

        for(int i = 0; i < maxChildrenAmount; i++) {
            NFA prefix = NFA.concat(NFA.symbol(String.valueOf(i)), postFix);

            List<NFA> c1Reads = new ArrayList<>();
            List<NFA> c1Writes = new ArrayList<>();

            for(NFA nfa : r1read) {
                c1Reads.add(NFA.concat(prefix, nfa));
            }
            for(NFA nfa : r1write) {
                c1Writes.add(NFA.concat(prefix, nfa));
            }

            List<NFA> c2Reads = new ArrayList<>();
            List<NFA> c2Writes = new ArrayList<>();

            for(NFA nfa : r2read) {
                c2Reads.add(NFA.concat(prefix, nfa));
            }
            for(NFA nfa : r2write) {
                c2Writes.add(NFA.concat(prefix, nfa));
            }

            callRWs1.add(Pair.of(c1Reads, c1Writes));
            callRWs2.add(Pair.of(c2Reads, c2Writes));
        }

        List<Pair<Vertex, Pair<List<NFA>, List<NFA>>>> targets1 = new ArrayList<>();
        List<Pair<Vertex, Pair<List<NFA>, List<NFA>>>> targets2 = new ArrayList<>();

        Pair<Vertex, Pair<List<NFA>, List<NFA>>> s1Target = Pair.of(s1, Pair.of(r1read, r1write));
        Pair<Vertex, Pair<List<NFA>, List<NFA>>> s2Target = Pair.of(s2, Pair.of(r2read, r2write));

        for(int i = 0; i < calls1.size(); i++) {
            Vertex v = calls1.get(i);
            Pair<Vertex, Pair<List<NFA>, List<NFA>>> target = Pair.of(v, callRWs1.get(i));
            targets1.add(target);
        }
        for(int i = 0; i < calls2.size(); i++) {
            Vertex v = calls2.get(i);
            Pair<Vertex, Pair<List<NFA>, List<NFA>>> target = Pair.of(v, callRWs2.get(i));
            targets2.add(target);
        }

        List<Pair<Vertex, Pair<List<NFA>, List<NFA>>>> targetsOrdered = new ArrayList<>();
        if(t1 == Traversal.TOP_DOWN) {
            targetsOrdered.add(s1Target);
            targetsOrdered.addAll(targets1);
        } else if(t1 == Traversal.BOTTOM_UP) {
            targetsOrdered.addAll(targets1);
            targetsOrdered.add(s1Target);
        }
        if(t2 == Traversal.TOP_DOWN) {
            targetsOrdered.add(s2Target);
            targetsOrdered.addAll(targets2);
        } else if(t2 == Traversal.BOTTOM_UP) {
            targetsOrdered.addAll(targets2);
            targetsOrdered.add(s2Target);
        }

        for(int i = 0; i < targetsOrdered.size(); i++) {
            Pair<Vertex, Pair<List<NFA>, List<NFA>>> target = targetsOrdered.get(i);
            edges.addAll(computeEdges(target.getLeft(), target.getRight().getLeft(),
                    target.getRight().getRight(), targetsOrdered.subList(i + 1, targetsOrdered.size())));
        }

        graph = Pair.of(Pair.of(vertices, edges), map);
    }

    // Target <R, W>
    private List<Edge> computeEdges(Vertex t, List<NFA> thisRead, List<NFA> thisWrite,
                                   List<Pair<Vertex, Pair<List<NFA>, List<NFA>>>> targets) {
        List<Edge> edges = new ArrayList<>();

        // R
        for(NFA tNFA : thisRead) {
            for(Pair<Vertex, Pair<List<NFA>, List<NFA>>> target : targets) {
                for(NFA oNFA : target.getRight().getRight()) {
                    if(NFA.mayCollide(tNFA, oNFA)) {
                        edges.add(new Edge(t, target.getLeft()));
                        break;
                    }
                }
            }
        }

        // W
        for(NFA tNFA : thisWrite) {
            targetLoop : for(Pair<Vertex, Pair<List<NFA>, List<NFA>>> target : targets) {
                if(edges.contains(new Edge(t, target.getLeft()))) continue;
                for(NFA oNFA : target.getRight().getLeft()) {
                    if(NFA.mayCollide(tNFA, oNFA)) {
                        edges.add(new Edge(t, target.getLeft()));
                        continue targetLoop;
                    }
                }
                for(NFA oNFA : target.getRight().getRight()) {
                    if(NFA.mayCollide(tNFA, oNFA)) {
                        edges.add(new Edge(t, target.getLeft()));
                        continue targetLoop;
                    }
                }
            }
        }

        return edges;
    }

    public List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> fuse() {
        List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> fuses
                = new ArrayList<>();
        Queue<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> queue
                = new ArrayDeque<>();
        queue.offer(graph);

        while(!queue.isEmpty()) {
            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> curr
                    = queue.poll();

            List<Vertex> vertices = curr.getLeft().getLeft();
            List<Edge> edges = curr.getLeft().getRight();
            Map<Vertex, Pair<List<Integer>, Integer>> label = curr.getRight();

            List<List<Vertex>> mergeables = new ArrayList<>();
            for(int i = 0; i < maxChildrenAmount; i++) {
                mergeables.add(new ArrayList<>());
            }
            for(Vertex v : vertices) {
                Pair<List<Integer>, Integer> l = label.get(v);
                if(l.getRight() >= 0) {
                    mergeables.get(l.getRight()).add(v);
                }
            }

            for(List<Vertex> mergeable : mergeables) {
                for(int i = 0; i < mergeable.size(); i++) {
                    for(int j = i + 1; j < mergeable.size(); j++) {
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

        return fuses;
    }

    /*private Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Boolean>>> computeFuses(
            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Boolean>>> graph) {

        List<Vertex> vertices = graph.getLeft().getLeft();
        List<Edge> edges = graph.getLeft().getRight();
        Map<Vertex, Pair<List<Integer>, Boolean>> label = graph.getRight();

        List<Vertex> mergeable1 = new ArrayList<>();
        List<Vertex> mergeable2 = new ArrayList<>();

        // For now, top-down only
        // This boolean indicates L/R descending
        for(Vertex v : vertices) {
            Pair<List<Integer>, Boolean> l = label.get(v);
            if(!l.getLeft().isEmpty()) {
                if(l.getRight()) {
                    mergeable1.add(v);
                } else {
                    mergeable2.add(v);
                }
            }
        }

        for(int i = 0; i < mergeable1.size() - 1; i++) {
            for(int j = i + 1; j < mergeable1.size(); j++) {
                Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Boolean>>> merged =
                        merge(graph, mergeable1.get(i), mergeable1.get(j));
                if(!hasCycle(merged.getLeft())) {
                    System.out.println("Merge opportunity found!");
                    System.out.println(merged);
                    System.out.println(topoSort(merged.getLeft()));
                    computeFuses(merged);
                }
            }
        }

        for(int i = 0; i < mergeable2.size() - 1; i++) {
            for(int j = i + 1; j < mergeable2.size(); j++) {
                Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Boolean>>> merged =
                        merge(graph, mergeable2.get(i), mergeable2.get(j));
                if(!hasCycle(merged.getLeft())) {
                    System.out.println("Merge opportunity found!");
                    System.out.println(merged);
                    System.out.println(topoSort(merged.getLeft()));
                    computeFuses(merged);
                }
            }
        }

        return graph;
    }*/

    /*
    private Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> merge(
            Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> graph,
            Vertex v1, Vertex v2
    ) {
        Vertex v12 = new Vertex(v1.name + " + " + v2.name);

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
    }*/

}

