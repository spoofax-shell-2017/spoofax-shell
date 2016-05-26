package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.core.unit.UnitService;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

/**
 * Represents a parse command sent to Spoofax.
 */
public class ParseCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Parse an expression.";

    private ISpoofaxSyntaxService syntaxService;
    private ISpoofaxUnitService unitService;

    /**
     * Instantiate a {@link ParseCommand}.
     * @param common        The {@link IStrategoCommon} service.
     * @param syntaxService The {@link SpoofaxSyntaxService}.
     * @param unitService   The {@link UnitService}.
     * @param onSuccess     Called upon success by the created {@link SpoofaxCommand}.
     * @param onError       Called upon an error by the created {@link SpoofaxCommand}.
     * @param project       The project in which this command should operate.
     * @param lang          The language to which this command applies.
     */
    @Inject
    public ParseCommand(IStrategoCommon common,
                        ISpoofaxSyntaxService syntaxService,
                        ISpoofaxUnitService unitService,
                        @Named("onSuccess") Consumer<StyledText> onSuccess,
                        @Named("onError") Consumer<StyledText> onError,
                        @Assisted IProject project,
                        @Assisted ILanguageImpl lang) {
        super(common, onSuccess, onError, project, lang);
        this.syntaxService = syntaxService;
        this.unitService = unitService;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Parses a program using the {@link ISpoofaxSyntaxService}.
     *
     * @param source
     *            The source of the program.
     * @param sourceFile
     *            The temporary file containing the source of the program.
     * @return An {@link ISpoofaxParseUnit}.
     * @throws MetaborgException
     *             When parsing fails.
     */
    public ISpoofaxParseUnit parse(String source, FileObject sourceFile) throws MetaborgException {
        ISpoofaxInputUnit inputUnit = this.unitService.inputUnit(sourceFile, source, lang, null);
        ISpoofaxParseUnit parseUnit = this.syntaxService.parse(inputUnit);

        if (!parseUnit.valid()) {
            throw new MetaborgException("The resulting parse unit is invalid.");
        }
        return parseUnit;
    }

    @Override
    public void execute(String... args) {
        try {
            ISpoofaxParseUnit term = this.parse(args[0], write(args[0]));

            this.onSuccess.accept(new StyledText(common.toString(term.ast())));
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
