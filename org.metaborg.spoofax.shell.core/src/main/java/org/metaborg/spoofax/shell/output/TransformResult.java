package org.metaborg.spoofax.shell.output;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Represents a {@link TransformResult} as returned by the {@link IReplCommand}. Wraps a
 * {@link ISpoofaxTransformUnit}.
 */
public abstract class TransformResult
    extends AbstractSpoofaxResult<ISpoofaxTransformUnit<?>> {

    /**
     * The result of the transformation of an analyzed AST.
     */
    public static class Analyzed extends TransformResult {

        /**
         * Create a {@link TransformResult}.
         *
         * @param common
         *            the {@link IStrategoCommon} service
         * @param analyzed
         *            the wrapped {@link ISpoofaxTransformUnit}
         */
        @Inject
        public Analyzed(IStrategoCommon common,
                        @Assisted ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> analyzed) {
            super(common, analyzed);
        }

        @Override
        public String sourceText() {
            return ((ISpoofaxAnalyzeUnit) unit().input()).input().input().text();
        }
    }

    /**
     * The result of the transformation of a parsed AST.
     */
    public static class Parsed extends TransformResult {

        /**
         * Create a {@link TransformResult}.
         *
         * @param common
         *            the {@link IStrategoCommon} service
         * @param parsed
         *            the wrapped {@link ISpoofaxTransformUnit}
         */
        @Inject
        public Parsed(IStrategoCommon common,
                      @Assisted ISpoofaxTransformUnit<ISpoofaxParseUnit> parsed) {
            super(common, parsed);
        }

        @Override
        public String sourceText() {
            return ((ISpoofaxParseUnit) unit().input()).input().text();
        }
    }


    /**
     * Create a {@link TransformResult}.
     *
     * @param common
     *            the {@link IStrategoCommon} service
     * @param unit
     *            the wrapped {@link ISpoofaxTransformUnit}
     */
    @AssistedInject
    private <T extends IUnit> TransformResult(IStrategoCommon common,
                                              @Assisted ISpoofaxTransformUnit<T> unit) {
        super(common, unit);
    }

    // Duplication here and in AnalyzeResult is intentional since no common ancestor of
    // ISpoofaxAnalyzeUnit and ISpoofaxTransformUnit exists with these functions.
    @SuppressWarnings("CPD-START")
    @Override
    public Optional<IStrategoTerm> ast() {
        return Optional.ofNullable(unit().ast());
    }

    @Override
    public Optional<IContext> context() {
        return Optional.of(unit().context());
    }

    @Override
    public List<IMessage> messages() {
        return StreamSupport.stream(unit().messages().spliterator(), false)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("CPD-END")
    @Override
    public boolean valid() {
        return unit().valid() && unit().success();
    }
}
