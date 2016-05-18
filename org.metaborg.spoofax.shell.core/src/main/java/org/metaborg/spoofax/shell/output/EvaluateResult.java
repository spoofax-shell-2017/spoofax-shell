package org.metaborg.spoofax.shell.output;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.shell.commands.EvaluateCommand;
import org.metaborg.spoofax.shell.core.EvaluateUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * The result of the execution of an {@link EvaluateCommand}.
 */
public class EvaluateResult extends AbstractSpoofaxResult<EvaluateUnit<?>> {

    /**
     * Create a {@link EvaluateResult}.
     *
     * @param common
     *            the {@link IStrategoCommon} service
     * @param unit
     *            the wrapped {@link EvaluateUnit}
     */
    @AssistedInject
    public EvaluateResult(IStrategoCommon common, @Assisted EvaluateUnit<?> unit) {
        super(common, unit);
    }

    @Override
    public Optional<IStrategoTerm> ast() {
        return Optional.of(unit().ast());
    }

    @Override
    public Optional<IContext> context() {
        return Optional.of(unit().context());
    }

    @Override
    public List<IMessage> messages() {
        return Collections.emptyList();
    }

    @Override
    public StyledText styled() {
        return toString(unit().ast());
    }

    @Override
    public boolean valid() {
        return true;
    }
}
