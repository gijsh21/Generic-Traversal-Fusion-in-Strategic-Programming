package org.example;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.lang.Context;
import stratego.lang.trans.trans;
import stratego.lang.trans.translate_core_0_2;

public class Stratego {
    static IStrategoTerm desugar(IStrategoTerm strategoSugarAST, String path) {
        // Initialise the Stratego code. The custom context is to pass in the special term factory that can do origin tracking.
        final Context c = trans.init(new Context(new ImploderOriginTermFactory(new TermFactory())));
        final ITermFactory tf = c.getFactory();

        return translate_core_0_2.instance.invoke(c, strategoSugarAST, tf.makeString(path), tf.makeInt(2));
    }
}
