package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents a parse command sent to Spoofax.
 */
public class ParseFunction extends AbstractFunction<InputResult, ParseResult> {
    private final ISpoofaxSyntaxService syntaxService;

    /**
     * Instantiate a {@link ParseFunction}.
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
    public ParseFunction(ISpoofaxSyntaxService syntaxService, IResultFactory resultFactory,
                        @Assisted IProject project, @Assisted ILanguageImpl lang) {
        super(resultFactory, project, lang);
        this.syntaxService = syntaxService;
    }

    @Override
    public ParseResult execute(InputResult arg) throws MetaborgException {
        ISpoofaxParseUnit parse = syntaxService.parse(arg.unit());
        return resultFactory.createParseResult(parse);
    }
}
