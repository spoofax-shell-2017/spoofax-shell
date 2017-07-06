package org.metaborg.spoofax.shell.functions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FoldResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.ISpoofaxTermResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Creates a {@link FoldResult} from a given {@link ISpoofaxTermResult}.
 */
public class FoldFunction implements FailableFunction<ISpoofaxTermResult<?>, FoldResult, IResult> {

    /**
     * Instantiate a {@link FoldFunction}.
     * Explicit
     */
    public FoldFunction() {
    }

    @Override
    public FailOrSuccessResult<FoldResult, IResult> apply(ISpoofaxTermResult<?> input) {

        Helper helper = new Helper();

        try {
            IStrategoTerm term = input.ast().get();

            term.writeAsString(helper, IStrategoTerm.INFINITE);

            List<ISourceRegion> out = new ArrayList<>(helper.outputRegions);

            return FailOrSuccessResult.successful(new FoldResult(term, out));
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper to wrap {@link IStrategoTerm#writeAsString}.
     *
     * As {@link IStrategoTerm#prettyPrint(org.spoofax.interpreter.terms.ITermPrinter)} is
     * deprecated {@link IStrategoTerm#writeAsString(Appendable, int)} is used instead.
     *
     * As {@link FoldFunction} should be state-less a private static class is used over
     * an anonymous class with members or single-length-arrays to contain the state within the
     * method scope.
     *
     * The {@link #Helper} recognizes <tt>(</tt> and <tt>)</tt> and uses them to
     * recognize structures.
     */
    private static class Helper implements Appendable {

        private Stack<Integer> regionStack = new Stack<>();
        private List<ISourceRegion> outputRegions = new ArrayList<>();

        private int index = 0;
        private int lastSeqIndex = -1;

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            return this.append(csq.subSequence(start, end));
        }

        @Override
        public Appendable append(char c) throws IOException {
            switch (c) {
            case '(':
                regionStack.push(lastSeqIndex == -1 ? 0 : lastSeqIndex);
                lastSeqIndex = -1;
                break;
            case ')':
                int startOffset = regionStack.pop();
                int endOffset = index;
                outputRegions.add(new SourceRegion(startOffset, endOffset));
                lastSeqIndex = -1;
                break;
            case ',':
                break;
            default:
                if (lastSeqIndex == -1) {
                    lastSeqIndex = index;
                }
                break;
            }
            index++;
            return this;
        }

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            lastSeqIndex = index;
            index += csq.length();
            return this;
        }
    }

}
