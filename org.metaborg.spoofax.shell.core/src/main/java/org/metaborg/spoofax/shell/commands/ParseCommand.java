package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

/**
 * Represents a parse command sent to Spoofax.
 */
public class ParseCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Parse an expression.";

    private ISpoofaxSyntaxService syntaxService;
    private IResultFactory unitFactory;

    /**
     * Instantiate a {@link ParseCommand}.
     * @param syntaxService  The {@link SpoofaxSyntaxService}.
     * @param unitFactory    The {@link IResultFactory}.
     * @param onSuccess      Called upon success by the created {@link SpoofaxCommand}.
     * @param onError        Called upon an error by the created {@link SpoofaxCommand}.
     * @param project        The project in which this command should operate.
     * @param lang           The language to which this command applies.
     */
    @Inject
    public ParseCommand(ISpoofaxSyntaxService syntaxService,
                        IResultFactory unitFactory,
                        @Named("onSuccess") Consumer<StyledText> onSuccess,
                        @Named("onError") Consumer<StyledText> onError,
                        @Assisted IProject project,
                        @Assisted ILanguageImpl lang) {
        super(onSuccess, onError, project, lang);
        this.syntaxService = syntaxService;
        this.unitFactory = unitFactory;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Parses a program using the {@link ISpoofaxSyntaxService}.
     *
     * @param unit the input for the program
     * @return An {@link ISpoofaxParseUnit}.
     * @throws MetaborgException
     *             When parsing fails.
     */
    public ParseResult parse(InputResult unit) throws MetaborgException {
        ISpoofaxParseUnit parse = syntaxService.parse(unit.unit());
        ParseResult result = unitFactory.createParseResult(parse);

        if (!result.valid()) {
            throw new MetaborgException("Invalid parse result!");
        }
        return result;
    }

    @Override
    public void execute(String... args) {
        try {
            InputResult input = unitFactory.createInputResult(lang, write(args[0]), args[0]);
            ParseResult parse = parse(input);

            this.onSuccess.accept(parse.styled());
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
