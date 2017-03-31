package org.metaborg.spoofax.shell.output;

import java.util.List;
import java.util.Optional;

import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * The result of the execution of an {@link AEvalFunction} or a {@link PEvalFunction}.
 */
public class EvaluateResult extends AbstractSpoofaxTermResult<IUnit> {
    private final ISpoofaxTermResult<?> wrappedDelegate;
    private final IStrategoTerm result;

    /**
     * Create a {@link EvaluateResult}.
     *
     * @param common
     *            the {@link IStrategoCommon} service.
     * @param wrappedResult
     *            the wrapped {@link ISpoofaxTermResult}.
     * @param result
     *            the result of the evaluation.
     */
    @Inject
    public EvaluateResult(IStrategoCommon common,
                          @Assisted ISpoofaxTermResult<?> wrappedResult,
                          @Assisted IStrategoTerm result) {
        super(common, wrappedResult.unit());
        this.wrappedDelegate = wrappedResult;
        this.result = result;
    }

    @Override
    public Optional<IStrategoTerm> ast() {
        return Optional.ofNullable(result);
    }

    @Override
    public String sourceText() {
        return wrappedDelegate.sourceText();
    }

    @Override
    public boolean valid() {
        return wrappedDelegate.valid();
    }

    @Override
    public Optional<IContext> context() {
        return wrappedDelegate.context();
    }

    @Override
    public List<IMessage> messages() {
        return wrappedDelegate.messages();
    }
}
