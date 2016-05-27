package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.stream.Collectors;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.hooks.IResultHook;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents a parse command sent to Spoofax.
 */
public class ParseCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Parse an expression.";

    private ISpoofaxSyntaxService syntaxService;

    /**
     * Instantiate a {@link ParseCommand}.
     *
     * @param syntaxService
     *            The {@link SpoofaxSyntaxService}.
     * @param resultHook
     *            The {@link IResultHook} to send results of successful evaluations to.
     * @param resultFactory
     *            The {@link IResultFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    public ParseCommand(ISpoofaxSyntaxService syntaxService, IResultHook resultHook,
                        IResultFactory resultFactory,
                        @Assisted IProject project, @Assisted ILanguageImpl lang) {
        super(resultHook, resultFactory, project, lang);
        this.syntaxService = syntaxService;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Parses a program using the {@link ISpoofaxSyntaxService}.
     *
     * @param unit
     *            the input for the program
     * @return An {@link ISpoofaxParseUnit}.
     * @throws MetaborgException
     *             When parsing fails.
     */
    public ParseResult parse(InputResult unit) throws MetaborgException {
        ISpoofaxParseUnit parse = syntaxService.parse(unit.unit());
        ParseResult result = resultFactory.createParseResult(parse);
        if (!result.valid()) {
            throw new MetaborgException(result.messages().stream().map(IMessage::message)
                .collect(Collectors.joining("\n")));
        }
        return result;
    }

    @Override
    public void execute(String... args) throws MetaborgException {
        try {
            InputResult input = resultFactory.createInputResult(lang, write(args[0]), args[0]);
            resultHook.accept(parse(input));
        } catch (IOException e) {
            throw new MetaborgException("Cannot write to temporary source file.");
        }
    }
}
