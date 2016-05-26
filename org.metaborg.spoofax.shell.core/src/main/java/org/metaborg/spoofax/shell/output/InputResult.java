package org.metaborg.spoofax.shell.output;

import java.util.List;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class InputResult extends AbstractResult<ISpoofaxInputUnit> {

    @AssistedInject
    public InputResult(IStrategoCommon common, @Assisted ISpoofaxInputUnit unit) {
        super(common, unit);
    }

    @AssistedInject
    public InputResult(IStrategoCommon common, ISpoofaxUnitService unitService,
                       @Assisted ILanguageImpl lang,
                       @Assisted FileObject file, @Assisted String source) {
        super(common, unitService.inputUnit(file, source, lang, null));
    }

    @Override
    public Optional<IStrategoTerm> ast() {
        return Optional.empty();
    }

    @Override
    public Optional<IContext> context() {
        return Optional.empty();
    }

    @Override
    public List<IMessage> messages() {
        return Lists.newArrayList();
    }

    @Override
    public StyledText styled() {
        return new StyledText(unit().text());
    }

    @Override
    public boolean valid() {
        return true;
    }

}
