package org.example.fuser.extract;

import org.example.fuser.ast.AccessPath;
import org.example.fuser.ast.DependencyAnalysis;
import org.example.fuser.graph.DependenceGraphF;
import org.example.fuser.graph.Edge;
import org.example.fuser.graph.Vertex;
import org.example.fuser.tree.AllStrategoNode;
import org.example.fuser.tree.Node;
import org.example.fuser.tree.RStrategoNode;
import org.example.fuser.tree.StrategoNode;
import org.example.fuser.util.Pair;
import org.example.fuser.util.Triple;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.*;
import java.util.stream.Collectors;

public class ExtractFuserInfo {

    // This class is responsible for extracting the necessary information
    // from the source program. It then immediately starts the fusion process,
    // and prints the results after it finishes.

    // It extracts the information by building up a list of
    // 'StrategoNode's. This list is the program to be analyzed.
    // And effectively also encodes the execution order.

    // There only exists an AllStrategoNode representing a call to
    // 'all', and an RStrategoNode representing a rule
    // application. All other supported syntax features are de-
    // sugared into these nodes. E.g. topdown is desugared into
    // topdown(s) = s; all(topdown(s))
    // Which becomes
    // def_n = s; all(def_n)
    // = RStrategoNode(s); AllStrategoNode(def_n)
    // Then, the final program becomes a list that, at some point
    // in the sequence, contains these nodes.

    // Constructor Types
    public static abstract class AType {
        public abstract String getResultingType();
        public abstract int getChildAmount();
        public abstract List<String> getChildren();
    }

    // Constructor Const Type (constructor with no subterms)
    // e.g.
    // Null : Exp
    public static class ConstType extends AType {
        String t;

        public ConstType(String t) {
            this.t = t;
        }

        public String getResultingType() {
            return t;
        }

        public int getChildAmount() {
            return 0;
        }

        public List<String> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return t;
        }
    }

    // Constructor function type (constructor with subterms)
    // e.g.
    // E : Int * Exp * Exp -> Exp
    public static class FunType extends AType {
        List<String> from;
        String to;

        public FunType(List<String> from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getResultingType() {
            return to;
        }

        public int getChildAmount() {
            return from.size();
        }

        public List<String> getChildren() {
            return from;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(String s : from) {
                sb.append(s).append(" * ");
            }
            String argTypes = sb.substring(0, sb.length() - 3);
            argTypes += " -> " + to;

            return argTypes;
        }
    }

    // Extract the declared information about the sorts and constructors
    // from the signature
    public static Pair<Set<String>, List<Pair<String, AType>>> extractTypesInfo(IStrategoTerm ast) {
        IStrategoTerm[] typesList = ast.getSubterm(1).getSubterm(0)
                .getSubterm(0).getSubterm(0).getSubterm(0).getAllSubterms();

        Set<String> sorts = new HashSet<>();
        List<Pair<String, AType>> constructors = new ArrayList<>();
        for(IStrategoTerm t : typesList) {
            String constructorName = t.getSubterm(0).toString();
            constructorName = constructorName.substring(1, constructorName.length() - 1);

            IStrategoAppl constructorAppl = (IStrategoAppl) t.getSubterm(1);
            if(constructorAppl.getConstructor().getName().equals("ConstType")) {
                String typeName = constructorAppl.getSubterm(0).getSubterm(0).toString();
                typeName = typeName.substring(1, typeName.length() - 1);
                sorts.add(typeName);
                constructors.add(Pair.of(constructorName, new ConstType(typeName)));
            } else if(constructorAppl.getConstructor().getName().equals("FunType")) {
                IStrategoTerm[] argTypes = constructorAppl.getSubterm(0).getAllSubterms();
                IStrategoTerm rType = constructorAppl.getSubterm(1);

                List<String> argTypeStrings = new ArrayList<>();
                for(IStrategoTerm argType : argTypes) {
                    String argTypeName = argType.getSubterm(0).getSubterm(0).toString();
                    argTypeName = argTypeName.substring(1, argTypeName.length() - 1);
                    argTypeStrings.add(argTypeName);
                    sorts.add(argTypeName);
                }
                String rTypeName = rType.getSubterm(0).getSubterm(0).toString();
                rTypeName = rTypeName.substring(1, rTypeName.length() - 1);
                sorts.add(rTypeName);

                constructors.add(Pair.of(constructorName, new FunType(argTypeStrings, rTypeName)));
            } else {
                throw new RuntimeException("Unknown constructor type declaration in " + constructorName);
            }
        }

        return Pair.of(sorts, constructors);
    }


    // Function that builds up the list of StrategoNodes from the source program
    // Then initiates the fusion
    public static void extractFuserInfo(IStrategoTerm ast) {
        Pair<Set<String>, List<Pair<String, AType>>> typeInfo = extractTypesInfo(ast);

        // Module(String, List)
        //                 |-> [Constructors, Strategies]
        //                                      |-> APPL Strategies`1
        //                                                 |-> LIST of SDefT
        IStrategoTerm defList = ast.getSubterm(1).getSubterm(1).getSubterm(0);

        // Create list of definitions
        // We assume that the definition with name prog_0_0 is the program
        Pair<IStrategoTerm, IStrategoTerm> program = null;
        List<Pair<IStrategoTerm, IStrategoTerm>> definitions = new ArrayList<>();
        for(IStrategoTerm def : defList.getAllSubterms()) {
            if(def instanceof IStrategoAppl) {
                IStrategoAppl defAppl = (IStrategoAppl) def;
                if(!defAppl.getConstructor().getName().equals("SDefT")) continue;
                IStrategoTerm defName = defAppl.getSubterm(0);
                IStrategoTerm definition = defAppl.getSubterm(3);
                if(defName.toString().equals("\"prog_0_0\"")) {
                    program = Pair.of(defName, definition);
                } else if(definition instanceof IStrategoAppl) {
                    definitions.add(Pair.of(defName, definition));
                }
            }
        }

        if(program == null) {
            throw new RuntimeException("No suitable program found!");
        }

        // Build the list of StrategoNodes
        Map<String, List<StrategoNode>> emptyFuncDefs = new HashMap<>();
        Triple<List<String>, Pair<Map<String, List<StrategoNode>>, Integer>, Map<String, Boolean>> nodes =
                buildProgramNodes(program, emptyFuncDefs, 0, definitions);

        // Extract information about the rewrite rules
        // Rule aliases refer to the things like
        // 'where' and 'lift_app_in_build' that Stratego
        // generates when desugaring
        List<Pair<IStrategoTerm, List<Pair<IStrategoTerm, IStrategoTerm>>>> ruleLR = definitions.stream()
                .map(p -> Pair.of(p.getLeft(), extractRules(p.getRight())))
                .collect(Collectors.toList());
        Map<String, List<String>> ruleAliases = extractRuleAliases(definitions.stream()
                .map(Pair::getRight).collect(Collectors.toList()));


        // Compute read and write access paths for the rewrite rules
        // Per constructor!
        Map<String, Map<String, Pair<List<AccessPath>, List<AccessPath>>>> ruleRWs = new HashMap<>();
        for(Pair<IStrategoTerm, List<Pair<IStrategoTerm, IStrategoTerm>>> rule : ruleLR) {
            Map<String, Pair<List<AccessPath>, List<AccessPath>>> rws = new HashMap<>();
            for(Pair<IStrategoTerm, IStrategoTerm> lr : rule.getRight()) {
                Node lhs = termToNode(lr.getLeft().getSubterm(0));
                Node rhs = termToNode(lr.getRight().getSubterm(0));
                if(!rws.containsKey(lhs.name)) {
                    rws.put(lhs.name, DependencyAnalysis.getRuleRW(lhs, rhs, ruleAliases));
                } else {
                    rws.put(lhs.name, DependencyAnalysis
                                    .mergeRW(rws.get(lhs.name),
                                            DependencyAnalysis.getRuleRW(lhs, rhs, ruleAliases)));
                }
            }

            for(Pair<String, AType> ct : typeInfo.getRight()) {
                String constructor = ct.getLeft();
                rws.computeIfAbsent(constructor, k -> Pair.of(List.of(), List.of()));
            }

            int ruleNameLen = rule.getLeft().toString().length();
            String ruleNameClean = rule.getLeft().toString().substring(1, ruleNameLen - 1);
            ruleRWs.put(ruleNameClean, rws);
        }

        // Build unfused graph (code synthesis without fusion or code motion)
        // The buildInOrder method ensures no code motion takes place
        // And then we simply don't call fuse either
        DependenceGraphF unfusedGraph = new DependenceGraphF();
        unfusedGraph.setInfo(nodes.getMiddle().getLeft(), ruleRWs, typeInfo, nodes.getLeft());
        unfusedGraph.buildInOrder();

        // Build graph on which fusion will be performed
        // Keep in mind there actually is one graph generated per constructor
        // So, getGraph returns a map indexed by constructor
        DependenceGraphF graph = new DependenceGraphF();
        graph.setInfo(nodes.getMiddle().getLeft(), ruleRWs, typeInfo, nodes.getLeft());
        graph.build();

        // Print the nodes and edges for every graph
        /*for(Map.Entry<String, Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>>
                entry : graph.getGraph().entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        System.out.println("\n--------------------\n");*/

        // Fuse the graph
        // Actually finds ALL POSSIBLE FUSES
        // someFuse will hold the 'best' fuse (for now, the one with the most
        // fused nodes)
        Map<String, List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>>> fuses =
                graph.fuse();
        Map<String, Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> someFuse =
                new HashMap<>();
        List<Pair<String, AType>> constructors = new ArrayList<>();
        for(Map.Entry<String, List<Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>>>
                entry : fuses.entrySet()) {
            // Find the 'best' fuse
            int currMin = Integer.MAX_VALUE;
            for(Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>> fuse
                : entry.getValue()) {
                if(fuse.getLeft().getLeft().size() < currMin) {
                    currMin = fuse.getLeft().getLeft().size();
                    someFuse.put(entry.getKey(), fuse);
                }
            }

            // Print the fused graph
            /*System.out.println(entry.getKey());
            System.out.println(someFuse.get(entry.getKey()));
            System.out.println("\n--------------------\n");*/

            // Build a list of constructors and their signatures
            // Necessary for the code synthesis
            // Just easy to do here, not related to the fusion
            AType constructorAType = typeInfo.getRight()
                    .stream().filter(p -> p.getLeft().equals(entry.getKey()))
                    .findFirst().orElseThrow().getRight();
            Pair<String, AType> constructor = Pair.of(entry.getKey(), constructorAType);
            constructors.add(constructor);
        }

        // Synthesize the unfused traversal
        synthesizeTraversal(unfusedGraph.getGraph(), constructors, nodes.getRight());
        System.out.println("\n--------------------\n");

        // Synthesize the fused traversal
        // Based on the selected 'best' fusion
        synthesizeTraversal(someFuse, constructors, nodes.getRight());
    }

    // Builds the list of StrategoNodes
    //
    // Note that the support for try is not 100% correct
    // It simply keeps track of if a rule is ever used in
    // a try statement. This only matters for the code
    // synthesis. If it is, it will always be synthesized
    // back inside of a try. But, of course, it is possible
    // that a rule is used normally in one place, but
    // inside a try in another place. This also does not
    // matter for dependency analysis. But will have to keep
    // track of this information in order to be able to
    // synthesize a correct traversal in the end.
    private static Triple<List<String>, Pair<Map<String, List<StrategoNode>>, Integer>, Map<String, Boolean>> buildProgramNodes(
            Pair<IStrategoTerm, IStrategoTerm> program,
            Map<String, List<StrategoNode>> functionDefinitionsMap,
            int ctr,
            List<Pair<IStrategoTerm, IStrategoTerm>> defs) {
        List<String> prog = new ArrayList<>();
        Map<String, Boolean> tryMap = new HashMap<>();

        Stack<IStrategoTerm> stack = new Stack<>();
        stack.push(program.getRight());

        while(!stack.isEmpty()) {
            IStrategoTerm curr = stack.pop();
            if(curr instanceof IStrategoAppl) {
                IStrategoAppl appl = (IStrategoAppl) curr;
                if(appl.getConstructor().getName().equals("Seq")) {
                    // s1; s2
                    // Simply put them on the stack
                    stack.push(appl.getSubterm(1));
                    stack.push(appl.getSubterm(0));
                } else if(appl.getConstructor().getName().equals("CallT")) {
                    // Call to some strategy
                    // Must be either topdown, bottomup, or a custom strategy
                    IStrategoTerm tName = appl.getSubterm(0).getSubterm(0);

                    IStrategoTerm rName;
                    boolean isTry = false;
                    if(!(tName.toString().equals("\"topdown_1_0\"")
                            || tName.toString().equals("\"bottomup_1_0\""))) {
                        // Custom user strategy
                        // Call ourselves recursively to build a list of StrategoNodes for
                        // that strategy
                        // Then, just insert a call in our list
                        String defName = "def_" + (ctr++);
                        Triple<List<String>, Pair<Map<String, List<StrategoNode>>, Integer>, Map<String, Boolean>> res =
                                buildProgramNodes(defs
                                .stream()
                                .filter(p -> p.getLeft().toString().equals(tName.toString()))
                                .findAny().get(), functionDefinitionsMap, ctr, defs);
                        functionDefinitionsMap = res.getMiddle().getLeft();
                        ctr = res.getMiddle().getRight();
                        List<StrategoNode> func = new ArrayList<>();
                        for(String s : res.getLeft()) {
                            func.addAll(functionDefinitionsMap.get(s));
                        }
                        functionDefinitionsMap.put(defName, func);
                        prog.add(defName);
                    } else if(appl.getSubterm(1).getSubterm(0) instanceof IStrategoAppl) {
                        // topdown or bottomup
                        // Build a function specified to the argument
                        // Then, insert a call to that function
                        // Also checks whether the rule is in a try
                        IStrategoAppl rAppl = (IStrategoAppl) appl.getSubterm(1).getSubterm(0);
                        if(rAppl.getConstructor().getName().equals("CallT")
                        && rAppl.getSubterm(0).getSubterm(0).toString().equals("\"try_1_0\"")) {
                            rName = appl.getSubterm(1).getSubterm(0).getSubterm(1)
                                    .getSubterm(0).getSubterm(0).getSubterm(0);
                            isTry = true;
                        } else {
                            rName = appl.getSubterm(1).getSubterm(0).getSubterm(0).getSubterm(0);
                        }

                        int rNameLen = rName.toString().length();
                        String rNameClean = rName.toString().substring(1, rNameLen - 1);

                        if(tName.toString().equals("\"topdown_1_0\"")) {
                            String defName = "def_" + (ctr++);
                            functionDefinitionsMap.put(defName,
                                    List.of(new RStrategoNode(rNameClean), new AllStrategoNode(defName)));
                            prog.add(defName);
                            tryMap.put(rNameClean, isTry);
                        } else if(tName.toString().equals("\"bottomup_1_0\"")) {
                            String defName = "def_" + (ctr++);
                            functionDefinitionsMap.put(defName,
                                    List.of(new AllStrategoNode(defName), new RStrategoNode(rNameClean)));
                            prog.add(defName);
                            tryMap.put(rNameClean, isTry);
                        }
                    } else throw new RuntimeException();
                }
            }
        }

        return Triple.of(prog, Pair.of(functionDefinitionsMap, ctr), tryMap);
    }

    // Create a Node (not a StrategoNode!) from a rewrite rule term
    // Used for computing the access paths of a rewrite rule
    private static Node termToNode(IStrategoTerm term) {
        if(!(term instanceof IStrategoAppl)) {
            throw new RuntimeException("Error parsing rule!");
        }

        Node n = null;
        IStrategoAppl a = (IStrategoAppl) term;

        if(a.getConstructor().getName().equals("Anno")) {
            a = (IStrategoAppl) a.getSubterm(0);
        }
        if(a.getConstructor().getName().equals("Op")) {
            String name = a.getSubterm(0).toString();
            n = new Node(name.substring(1, name.length() - 1));
            for(IStrategoTerm child : a.getSubterm(1).getAllSubterms()) {
                n.children.add(termToNode(child));
            }
        }

        if(a.getConstructor().getName().equals("Var")) {
            n = new Node("Var");
            String name = a.getSubterm(0).toString();
            n.children.add(name.substring(1, name.length() - 1));
        }

        // Wld
        if(a.getConstructor().getName().equals("Wld")) {
            n = new Node("Wld");
        }

        // Int
        if(a.getConstructor().getName().equals("Int")) {
            n = new Node("Int");
            String value = a.getSubterm(0).toString();
            value = value.substring(1, value.length() - 1);
            n.children.add(Integer.parseInt(value));
        }

        if(n == null) {
            throw new RuntimeException("Error parsing rule!");
        }
        return n;
    }



    // Functions that extract rules from the source program
    // With rule aliases I am referring to things like
    // 'where' and 'lift_app_in_build' that Stratego generates
    // when desugaring
    private static List<Pair<IStrategoTerm, IStrategoTerm>> extractRules(IStrategoTerm rule) {
        List<Pair<IStrategoTerm, IStrategoTerm>> rules = new ArrayList<>();
        if(rule instanceof IStrategoAppl) {
            IStrategoAppl ruleAppl = (IStrategoAppl) rule;
            if(ruleAppl.getConstructor().getName().equals("Scope")) {
                rules.add(extractRuleFromSingle(ruleAppl));
            } else if(ruleAppl.getConstructor().getName().equals("GuardedLChoice")) {
                IStrategoTerm lChild = ruleAppl.getSubterm(0);
                IStrategoTerm rChild = ruleAppl.getSubterm(2);
                List<Pair<IStrategoTerm, IStrategoTerm>> lChildRules = extractRules(lChild);
                List<Pair<IStrategoTerm, IStrategoTerm>> rChildRules = extractRules(rChild);
                rules.addAll(lChildRules);
                rules.addAll(rChildRules);
            }
        }

        return rules;
    }

    private static Map<String, List<String>> extractRuleAliases(List<IStrategoTerm> defs) {
        Map<String, List<String>> result = new HashMap<>();
        for(IStrategoTerm def : defs) {
            Map<String, List<String>> aliases = extractRuleAliases(def);
            for(String alias : aliases.keySet()) {
                if(result.containsKey(alias)) {
                    throw new RuntimeException("Duplicate key when extracting aliases!");
                }
            }
            result.putAll(aliases);
        }
        return result;
    }

    private static Map<String, List<String>> extractRuleAliases(IStrategoTerm rule) {
        Map<String, List<String>> aliases = new HashMap<>();

        if(rule instanceof IStrategoAppl) {
            IStrategoAppl ruleAppl = (IStrategoAppl) rule;
            if(ruleAppl.getConstructor().getName().equals("Scope")) {
                aliases = extractRuleAliasesFromSingle(ruleAppl);
            } else if(ruleAppl.getConstructor().getName().equals("GuardedLChoice")) {
                IStrategoTerm lChild = ruleAppl.getSubterm(0);
                IStrategoTerm rChild = ruleAppl.getSubterm(2);
                Map<String, List<String>> lChildAliases = extractRuleAliases(lChild);
                Map<String, List<String>> rChildAliases = extractRuleAliases(rChild);
                aliases = lChildAliases;
                for(String alias : rChildAliases.keySet()) {
                    if(aliases.containsKey(alias)) {
                        throw new RuntimeException("Duplicate key when extracting aliases!");
                    }
                }
                aliases.putAll(rChildAliases);
            }
        }

        return aliases;
    }

    private static Pair<IStrategoTerm, IStrategoTerm> extractRuleFromSingle(IStrategoAppl rule) {
        IStrategoTerm actual = rule.getSubterm(1);
        if(actual instanceof IStrategoAppl) {
            IStrategoAppl actualAppl = (IStrategoAppl) actual;
            if(actualAppl.getConstructor().getName().equals("Seq")) {
                IStrategoTerm s1 = actualAppl.getSubterm(0);
                IStrategoTerm s2 = actualAppl.getSubterm(1);
                if(s1 instanceof IStrategoAppl && s2 instanceof IStrategoAppl) {
                    IStrategoAppl s1Appl = (IStrategoAppl) s1;
                    IStrategoAppl s2Appl = (IStrategoAppl) s2;
                    if(s1Appl.getConstructor().getName().equals("Match")
                            && s2Appl.getConstructor().getName().equals("Build")) {
                        return Pair.of(s1Appl, s2Appl);
                    } else if(s1Appl.getConstructor().getName().equals("Match")
                        && s2Appl.getConstructor().getName().equals("Seq")) {
                        IStrategoAppl ssAppl = s2Appl;
                        while(ssAppl.getConstructor().getName().equals("Seq")) {
                            ssAppl = (IStrategoAppl) ssAppl.getSubterm(1);
                        }
                        return Pair.of(s1Appl, ssAppl);
                    }
                }
            }
        }

        throw new RuntimeException("Error parsing rule!");
    }

    private static Map<String, List<String>> extractRuleAliasesFromSingle(IStrategoAppl rule) {
        List<IStrategoAppl> sequence = new ArrayList<>();
        IStrategoTerm actual = rule.getSubterm(1);
        if(actual instanceof IStrategoAppl) {
            IStrategoAppl actualAppl = (IStrategoAppl) actual;
            if(actualAppl.getConstructor().getName().equals("Seq")) {
                //sequence.add((IStrategoAppl) actualAppl.getSubterm(0));
                IStrategoAppl ssAppl = (IStrategoAppl) actualAppl.getSubterm(1);
                while(ssAppl.getConstructor().getName().equals("Seq")) {
                    sequence.add((IStrategoAppl) ssAppl.getSubterm(0));
                    ssAppl = (IStrategoAppl) ssAppl.getSubterm(1);
                }
                //sequence.add(ssAppl);
            }
        }

        Map<String, List<String>> res = new HashMap<>();

        Stack<String> matchStack = new Stack<>();
        for(int i = 0; i < sequence.size(); i++) {
            IStrategoAppl s = sequence.get(i);

            if (s.getConstructor().getName().equals("Match")) {
                if (!((IStrategoAppl) s.getSubterm(0)).getConstructor().getName().equals("Var")) continue;
                String varName = s.getSubterm(0).getSubterm(0).toString();
                varName = varName.substring(1, varName.length() - 1);
                matchStack.push(varName);
            } else if (s.getConstructor().getName().equals("Build")) {
                if (matchStack.isEmpty()) throw new RuntimeException("Match stack is empty!");
                if (((IStrategoAppl) s.getSubterm(0)).getConstructor().getName().equals("Var")) {
                    String varName = s.getSubterm(0).getSubterm(0).toString();
                    varName = varName.substring(1, varName.length() - 1);
                    if (res.containsKey(varName)) {
                        res.put(matchStack.pop(), res.get(varName));
                    } else {
                        res.put(matchStack.pop(), new ArrayList<>(List.of(varName)));
                    }
                } else if (((IStrategoAppl) s.getSubterm(0)).getConstructor().getName().equals("Anno")) {
                    IStrategoTerm[] varsArray = s.getSubterm(0).getSubterm(0).getSubterm(1).getAllSubterms();
                    List<String> vars = new ArrayList<>();
                    for (IStrategoTerm var : varsArray) {
                        if (var instanceof IStrategoAppl) {
                            IStrategoAppl varAppl = (IStrategoAppl) var;
                            if (varAppl.getConstructor().getName().equals("Var")) {
                                String v = varAppl.getSubterm(0).toString();
                                v = v.substring(1, v.length() - 1);
                                if (res.containsKey(v)) {
                                    vars.addAll(res.get(v));
                                } else {
                                    vars.add(v);
                                }
                            }
                        }
                    }
                    res.put(matchStack.pop(), vars);
                }
            }
        }

        return res;
    }




    // Functions that synthesize code for the fused traversal
    // From the dependence graph
    private static void synthesizeTraversal(
            Map<String, Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> f,
            List<Pair<String, AType>> constructors,
            Map<String, Boolean> tryMap) {
        Map<String, List<Vertex>> orders = new HashMap<>();
        Map<String, List<Pair<List<Integer>, Integer>>> defs = new HashMap<>();
        int max = 0;

        // For every vertex, find the (list of) traversal(s) it originates
        // from, as well as the index of the child it calls if it is a
        // call vertex. These are stored in defs, indexed per constructor.
        // The lists in def are in topological order!
        // Also find the maximum amount of traversals from the
        // list (to decide how many functions to synthesize).
        for(Map.Entry<String, Pair<Pair<List<Vertex>, List<Edge>>, Map<Vertex, Pair<List<Integer>, Integer>>>> e
                : f.entrySet()) {
            List<Vertex> order = DependenceGraphF.topoSort(e.getValue().getLeft());
            orders.put(e.getKey(), order);

            List<Pair<List<Integer>, Integer>> def = order.stream()
                    .map(s -> f.get(e.getKey()).getRight().get(s)).collect(Collectors.toList());
            defs.put(e.getKey(), def);

            for(Pair<List<Integer>, Integer> d : def) {
                int cMax = d.getLeft().stream().max(Integer::compareTo).get();
                if(cMax > max) {
                    max = cMax;
                }
            }
        }

        // Synthesize the main selector function (=the new program
        // entry point)
        String fMain = synthesizeSelector("f_main", constructors);

        List<Pair<String, String>> selectors = new ArrayList<>();
        List<Pair<String, String>> typeSpecifics = new ArrayList<>();

        // Synthesize the main functions specified to every constructor
        for(Pair<String, AType> constructor : constructors) {
            List<Boolean> all = new ArrayList<>();
            for(int i = 0; i <= max; i++) {
                all.add(true);
            }
            String fAll = fusedTraversalHelper(orders.get(constructor.getLeft()),
                    defs.get(constructor.getLeft()), all, max, constructor, tryMap);
            typeSpecifics.add(Pair.of("f_main_" + constructor.getLeft(), fAll));
        }

        // Find which functions need to be synthesized. A function needs
        // to be synthesized for every possible combination of
        // enabled traversals that is used in the fused program.
        Set<List<Integer>> needed = new HashSet<>();
        for(Map.Entry<String, List<Pair<List<Integer>, Integer>>> e : defs.entrySet()) {
            for(Pair<List<Integer>, Integer> def : e.getValue()) {
                if(def.getRight() < 0) continue;
                Set<Integer> unique = new HashSet<>(def.getLeft());
                if(unique.size() > max) continue;
                List<Integer> sortedUnique = new ArrayList<>(unique);
                sortedUnique.sort(Integer::compareTo);
                needed.add(sortedUnique);
            }
        }

        // Synthesize selectors and specified functions for all the
        // functions that we determined need to be synthesized
        for(List<Integer> n : needed) {
            StringBuilder name = new StringBuilder("f_");
            for(int q : n) {
                name.append(q).append("_");
            }
            name.deleteCharAt(name.length() - 1);

            List<Boolean> actives = new ArrayList<>();
            for(int i = 0; i <= max; i++) {
                if(n.contains(i)) {
                    actives.add(true);
                } else {
                    actives.add(false);
                }
            }

            String selector = synthesizeSelector(name.toString(), constructors);
            selectors.add(Pair.of(name.toString(), selector));

            name.append("_");
            for(Pair<String, AType> constructor : constructors) {
                String fT = fusedTraversalHelper(orders.get(constructor.getLeft()),
                        defs.get(constructor.getLeft()), actives, max, constructor, tryMap);
                typeSpecifics.add(Pair.of(name + constructor.getLeft(), fT));
            }
        }

        // Print the program!
        System.out.println("f_main = " + fMain);
        for(Pair<String, String> selector : selectors) {
            System.out.println(selector.getLeft() + " = " + selector.getRight());
        }
        for(Pair<String, String> t : typeSpecifics) {
            System.out.println(t.getLeft() + " = " + t.getRight());
        }
    }

    // Synthesizes the selector functions
    private static String synthesizeSelector(String baseName, List<Pair<String, AType>> constructors) {
        if(constructors.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for(Pair<String, AType> constructor : constructors) {
            sb.append("?").append(constructor.getLeft()).append("(");
            if(constructor.getRight().getChildAmount() > 0) {
                sb.append("_, ".repeat(constructor.getRight().getChildAmount()));
                sb.delete(sb.length() - 2, sb.length());
            }
            sb.append(") < ");
            sb.append(baseName).append("_").append(constructor.getLeft());
            sb.append(" + ");
        }
        sb.append("id");

        return sb.toString();
    }

    // Helper class used when synthesizing type-specified functions
    // Its purpose is to keep track of calls on children of
    // a constructor that happen in a row. For example, if a call
    // happens on child 0, followed by a call on child 1, these
    // can be combined into a single application of the congruence operator.
    private static class CallCreator {
        String constructor;
        String[] cs;
        boolean[] isSet;
        boolean used;

        CallCreator(Pair<String, AType> constructor) {
            this.constructor = constructor.getLeft();
            this.cs = new String[constructor.getRight().getChildAmount()];
            this.isSet = new boolean[constructor.getRight().getChildAmount()];
            for(int i = 0; i < constructor.getRight().getChildAmount(); i++) {
                this.cs[i] = "id";
                this.isSet[i] = false;
            }
            this.used = false;
        }

        boolean isSet(int i) {
            return isSet[i];
        }

        void set(int i, String c) {
            cs[i] = c;
            isSet[i] = true;
            used = true;
        }

        String build() {
            StringBuilder sb = new StringBuilder();
            sb.append(constructor).append("(");
            for(String c : cs) {
                sb.append(c).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
            return sb.toString();
        }
    }

    // Synthesize type-specified traversal functions
    private static String fusedTraversalHelper(List<Vertex> names, List<Pair<List<Integer>, Integer>> defs,
                                               List<Boolean> active, int max,
                                               Pair<String, AType> constructor,
                                               Map<String, Boolean> tryMap) {
        List<String> fusedTraversal = new ArrayList<>();
        CallCreator cc = new CallCreator(constructor);

        // Go through in topological order
        for(int i = 0; i < names.size(); i++) {
            Pair<List<Integer>, Integer> def = defs.get(i);
            if(def.getRight() < 0 && def.getLeft().size() == 1) {
                // This is a rewrite rule application, not a call!
                // Only insert it if the traversal it originates from
                // is active
                if(active.get(def.getLeft().get(0))) {
                    // If any calls happened before this statement,
                    // make sure to insert them first
                    if(cc.used) {
                        fusedTraversal.add(cc.build());
                        cc = new CallCreator(constructor);
                    }
                    // Insert the rewrite rule application
                    String rName = names.get(i).toStringPreferInfo(true, true);
                    if(tryMap.containsKey(names.get(i).toStringPreferInfo(false, false))
                            && tryMap.get(names.get(i).toStringPreferInfo(false, false))) {
                        fusedTraversal.add("try(" + rName + ")");
                    } else {
                        fusedTraversal.add(rName);
                    }
                }
            } else {
                // This is a call node!
                // Only insert parts of the call that are active
                if(active.stream().noneMatch(b -> b)) continue;

                // Find the parts that are active
                String callName = "";
                Set<Integer> unique = new HashSet<>(def.getLeft());
                unique = unique.stream().filter(active::get).collect(Collectors.toSet());
                if(unique.size() > max) {
                    callName = "f_main";
                } else if(!unique.isEmpty()) {
                    List<Integer> sortedUnique = new ArrayList<>(unique);
                    sortedUnique.sort(Integer::compareTo);
                    StringBuilder name = new StringBuilder("f_");
                    for(int q : sortedUnique) {
                        name.append(q).append("_");
                    }
                    name.deleteCharAt(name.length() - 1);
                    callName = name.toString();
                }

                if(callName.isEmpty()) continue;

                // Append call to the CallCreator
                if(cc.isSet(def.getRight())) {
                    fusedTraversal.add(cc.build());
                    cc = new CallCreator(constructor);
                }
                cc.set(def.getRight(), callName);
            }
        }
        if(cc.used) {
            fusedTraversal.add(cc.build());
        }

        StringBuilder res = new StringBuilder();
        for(String s : fusedTraversal) {
            res.append(s).append("; ");
        }
        res.delete(res.length() - 2, res.length());
        return res.toString();
    }

}
