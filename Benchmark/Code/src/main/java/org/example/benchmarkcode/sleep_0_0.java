package org.example.benchmarkcode;

import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

/**
 * Sleep for the number of microseconds that the current term has as an int value. Fail if the current term isn't an
 *  int.
 */
public class sleep_0_0 extends Strategy {
    public static final sleep_0_0 instance = new sleep_0_0();

    public static IStrategoTerm callStatic(Context context, IStrategoTerm term) {
        if (term instanceof IStrategoInt) {
            IStrategoInt i = (IStrategoInt) term;
            if(i.intValue() == 0) return term;
            if(i.intValue() < 10_000) {
                long start = System.nanoTime();
                long delay = 1000L * i.intValue();
                while(System.nanoTime() - start < delay) {}
            } else {
                try {
                    Thread.sleep(i.intValue() / 1000);
                } catch (InterruptedException e) {
                    throw new StrategoException("Passed non-int to sleep");
                }
            }
            return term;
        }
        return null;
    }

    @Override
    public IStrategoTerm invoke(Context context, IStrategoTerm term) {
        return callStatic(context, term);
    }
}