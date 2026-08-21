package org.example;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableVariant;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2Failure;
import org.spoofax.jsglr2.JSGLR2Implementation;
import org.spoofax.jsglr2.JSGLR2Result;
import org.spoofax.jsglr2.JSGLR2Success;
import org.spoofax.jsglr2.JSGLR2Variant;
import org.spoofax.jsglr2.imploder.ImploderVariant;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestConstruction;
import org.spoofax.jsglr2.parseforest.ParseForestRepresentation;
import org.spoofax.jsglr2.parser.ParserVariant;
import org.spoofax.jsglr2.reducing.Reducing;
import org.spoofax.jsglr2.stack.StackRepresentation;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.tokens.TokenizerVariant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Parser {
    static JSGLR2Implementation<IParseForest, ?, ?, IStrategoTerm, ?, ?> getParser(IParseTable parseTable) {
        final ParserVariant parserVariant = new ParserVariant(ActiveStacksRepresentation.standard(), ForActorStacksRepresentation.standard(), ParseForestRepresentation.standard(), ParseForestConstruction.standard(), StackRepresentation.standard(), Reducing.standard(), false);
        final JSGLR2Variant jsglr2Variant = new JSGLR2Variant(parserVariant, ImploderVariant.standard(), TokenizerVariant.standard());
        return (JSGLR2Implementation<IParseForest, ?, ?, IStrategoTerm, ?, ?>) jsglr2Variant.getJSGLR2(parseTable);
    }

    static IParseTable getParseTable() throws ParseTableReadException, IOException {
        final InputStream parseTableInputStream = Main.class.getClassLoader().getResourceAsStream("stratego.tbl");
        final ParseTableVariant tableVariant = new ParseTableVariant();
        return tableVariant.parseTableReader().read(parseTableInputStream);
    }

    static IStrategoTerm parseAndImplode(String fileContents, JSGLR2Implementation<?, ?, ?, IStrategoTerm, ?, ?> jsglr2) {
        JSGLR2Result<IStrategoTerm> result = jsglr2.parseResult(fileContents);

        if (result.isSuccess()) {
            return ((JSGLR2Success<IStrategoTerm>) result).ast;
        } else {
            throw new RuntimeException(((JSGLR2Failure<IStrategoTerm>) result).parseFailure.failureCause.causeMessage());
        }
    }

    static IStrategoTerm parse(Path path) throws IOException, ParseTableReadException {
        return parseAndImplode(Files.readString(path), getParser(getParseTable()));
    }
}
