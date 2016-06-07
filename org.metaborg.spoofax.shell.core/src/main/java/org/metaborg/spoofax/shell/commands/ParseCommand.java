package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.client.IHook;
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
     * @param resultFactory
     *            The {@link IResultFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    public ParseCommand(ISpoofaxSyntaxService syntaxService, IResultFactory resultFactory,
                        @Assisted IProject project, @Assisted ILanguageImpl lang) {
        super(resultFactory, project, lang);
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
        // TODO: pass the result to the client instead of throwing an exception -- The client needs
        // the result in order to do fancy stuff.
        if (!result.valid()) {
            throw new MetaborgException(result.messages().stream().map(IMessage::message)
                .collect(Collectors.joining("\n")));
        }
        return result;
    }

    @Override
    public IHook execute(String... args) throws MetaborgException {
        try {
            String source = args[0];
            FileObject file = write(args[0]);
            ShellFacet shellFacet = lang.facet(ShellFacet.class);

            InputResult input = resultFactory
                .createInputResult(lang, file, source,
                                   new JSGLRParserConfiguration(shellFacet.getShellStartSymbol()));

            try {
                ParseResult result = parse(input);
                return (display) -> display.displayResult(result);
            } catch (MetaborgException e) {
                ParseResult result = parse(resultFactory.createInputResult(lang, file, source));
                return (display) -> display.displayResult(result);
            }
        } catch (IOException e) {
            throw new MetaborgException("Cannot write to temporary source file.");
        }
    }
}
