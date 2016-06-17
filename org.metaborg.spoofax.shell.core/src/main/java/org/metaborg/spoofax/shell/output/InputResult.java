package org.metaborg.spoofax.shell.output;

import java.util.List;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Represents a {@link InputResult} as returned by the {@link IReplCommand}. Wraps a
 * {@link ISpoofaxInputUnit}.
 */
public class InputResult extends AbstractSpoofaxResult<ISpoofaxInputUnit> {

    /**
     * Create a {@link InputResult}.
     *
     * @param common
     *            the {@link IStrategoCommon} service
     * @param unit
     *            the wrapped {@link ISpoofaxInputUnit}
     */
    @AssistedInject
    public InputResult(IStrategoCommon common, @Assisted ISpoofaxInputUnit unit) {
        super(common, unit);
    }

    /**
     * Create a {@link InputResult} from source.
     *
     * @param common
     *            the {@link IStrategoCommon} service
     * @param unitService
     *            the {@link ISpoofaxUnitService}
     * @param lang
     *            the {@link ILanguageImpl} of the unit
     * @param file
     *            the source {@link FileObject}
     * @param source
     *            the source string
     * @param parserConfig
     *            the parser configuration
     */
    @AssistedInject
    public InputResult(IStrategoCommon common, ISpoofaxUnitService unitService,
                       @Assisted ILanguageImpl lang, @Assisted FileObject file,
                       @Assisted String source, @Assisted JSGLRParserConfiguration parserConfig) {
        super(common, unitService.inputUnit(file, source, lang, null, parserConfig));
    }

    /**
     * Create a {@link InputResult} from source.
     *
     * @param common
     *            the {@link IStrategoCommon} service
     * @param unitService
     *            the {@link ISpoofaxUnitService}
     * @param lang
     *            the {@link ILanguageImpl} of the unit
     * @param file
     *            the source {@link FileObject}
     * @param source
     *            the source string
     */
    @AssistedInject
    public InputResult(IStrategoCommon common, ISpoofaxUnitService unitService,
                       @Assisted ILanguageImpl lang, @Assisted FileObject file,
                       @Assisted String source) {
        this(common, unitService, lang, file, source, null);
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

    @Override
    public String sourceText() {
        return unit().text();
    }

}
