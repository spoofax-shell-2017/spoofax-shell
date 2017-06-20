package org.metaborg.spoofax.shell.functions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FoldResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.PrintResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Creates a {@link PrintResult} from a given {@link FoldResult}.
 */
public class PrettyPrintFunction implements FailableFunction<FoldResult, PrintResult, IResult> {

    private static final CharSequence INDENTATION = " ";
    private static final int START = 0;
    private static final int END = 1;

    @Override
    public FailOrSuccessResult<PrintResult, IResult>
            apply(FoldResult input) {

        Helper helper = new Helper();

        IStrategoTerm term = input.getTerm();

        input.getRegions().forEach(region -> {
            helper.regionOffsets.add(new int[] {
                    region.startOffset(),
                    region.endOffset()
            });
        });

        try {
            term.writeAsString(helper, IStrategoTerm.INFINITE);
            List<ISourceRegion> newFolds = new ArrayList<>();
            for (int[] regionInts : helper.regionOffsets) {
                newFolds.add(new SourceRegion(
                        regionInts[START],
                        regionInts[END]));
            }
            return FailOrSuccessResult.successful(
                    new PrintResult(helper.text.toString(), newFolds));
        } catch (IOException e) {
            // should not happen
            return FailOrSuccessResult.excepted(new ExceptionResult(e));
        }
    }

    /**
     * Helper to wrap {@link IStrategoTerm#writeAsString}.
     *
     * As {@link IStrategoTerm#prettyPrint(org.spoofax.interpreter.terms.ITermPrinter)} is
     * deprecated {@link IStrategoTerm#writeAsString(Appendable, int)} is used instead.
     *
     * As {@link PrettyPrintFunction} should be state-less a private static class is used over
     * an anonymous class with members or single-length-arrays to contain the state within the
     * method scope.
     *
     * The {@link #Helper} recognizes <tt>(</tt> and <tt>)</tt> and uses them to
     * indent.
     */
    private static class Helper implements Appendable {

        private StringBuilder text = new StringBuilder();
        private List<int[]> regionOffsets = new ArrayList<>();

        private int indentationLevel = 0;
        private boolean shouldIndent = false;
        private boolean isClosing = false;

        private void appendAndUpdate(CharSequence csq, boolean updateFolds) {
            appendAndUpdate(csq, updateFolds, false);
        }

        private void appendAndUpdate(CharSequence csq, boolean updateFolds, boolean inclusive) {
            int preOffset = text.length();
            text.append(csq);
            if (updateFolds) {
                if (inclusive && preOffset > 0) {
                    preOffset = preOffset - 1;
                }
                updateOffsets(preOffset, csq.length());
            }
        }
        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            return this.append(csq.subSequence(start, end));
        }

        private void indent() {
            if (shouldIndent) {
                isClosing = false;
                shouldIndent = false;
                appendAndUpdate(
                        String.join("", Collections.nCopies(indentationLevel, INDENTATION)),
                        true);
            }
        }

        private void updateOffsets(int offset, int value) {
            for (int[] ints : regionOffsets) {
                if (ints[START] >= offset) {
                    ints[START] += value;
                }
                if (ints[END] >= offset) {
                    ints[END] += value;
                }
            }
        }

        @Override
        public Appendable append(char c) throws IOException {
            switch (c) {
                case '(':
                    indent();
                    appendAndUpdate(Character.toString(c), false);
                    appendAndUpdate(System.lineSeparator(), true);
                    shouldIndent = true;
                    indentationLevel++;
                    break;
                case ')':
                    if (!isClosing) {
                        indent();
                        appendAndUpdate(System.lineSeparator(), true);
                    }
                    indentationLevel--;
                    shouldIndent = true;
                    indent();
                    appendAndUpdate(Character.toString(c), false);
                    appendAndUpdate(System.lineSeparator(), true, true);
                    shouldIndent = true;
                    isClosing = true;
                    break;
                default:
                    indent();
                    appendAndUpdate(Character.toString(c), false);
                    break;
            }
            return this;
        }

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            indent();
            appendAndUpdate(csq.toString(), false);
            return this;
        }
    }

}
