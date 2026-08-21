package org.example;

import org.example.fuser.extract.ExtractFuserInfo;
import org.metaborg.parsetable.ParseTableReadException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    // These types are ignored in dependency analysis as they cannot have children
    // This improves performance, as otherwise call nodes would be generated for children
    // of these types. Additional call nodes create more fusion opportunities. And because
    // we check every possible fusion, this scales poorly.
    public static final List<String> LITERAL_TYPES = List.of("Int", "Real", "String");

    public static void main(String[] args) throws IOException, ParseTableReadException {
        if(args.length == 0) {
            // Source program
            args = new String[]{"java/ctree/src/main/resources/thesis-render-tree.str"};
        } else if (args.length != 1) {
            System.out.println("USAGE: provide 1 input file as argument.");
            System.exit(1);
        }

        Path path = Path.of(args[0]);
        // We're giving an "output path" dummy value to the front-end of the compiler
        Path ctreePath = path.resolveSibling(path.getFileName().toString().replace(".str", ".ctree"));
        IStrategoTerm strategoSugarAST = Parser.parse(path);
        //System.out.println(strategoSugarAST);

        IStrategoTerm ctreeAST = Stratego.desugar(strategoSugarAST, ctreePath.toString());
        //System.out.println(ctreeAST);

        // Not a great function name, but this extracts the info,
        // performs the fusion, and prints the results!
        ExtractFuserInfo.extractFuserInfo(ctreeAST);
    }

    public static void printAllSubterms(IStrategoTerm term) {
        System.out.println(term);
        System.out.println(term.getType());
        System.out.println(term.getAnnotations());
        System.out.println(term.getSubtermCount());
        if(term instanceof IStrategoAppl) {
            IStrategoAppl appl = (IStrategoAppl) term;
            System.out.println(appl.getConstructor());
        }
        System.out.println(" ----- ");
        for(IStrategoTerm subterm : term.getAllSubterms()) {
            printAllSubterms(subterm);
        }
    }

}
