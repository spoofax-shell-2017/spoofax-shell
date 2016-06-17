package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * An {@link AbstractSpoofaxFunction} that parses a given {@link InputResult}, creating an
 * {@link ParseResult}.
 */
public class ParseFunction extends AbstractSpoofaxFunction<InputResult, ParseResult> {
    private final ISpoofaxSyntaxService syntaxService;
    private final ISpoofaxUnitService unitService;

    /**
     * Instantiate a {@link ParseFunction}.
     *
     * @param syntaxService
     *            The {@link ISpoofaxSyntaxService}.
     * @param unitService
     *            The {@link ISpoofaxUnitService}, for creating a new {@link IInputUnit} when
     *            retrying with the default start symbol.
     * @param resultFactory
     *            The {@link IResultFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    public ParseFunction(ISpoofaxSyntaxService syntaxService, ISpoofaxUnitService unitService,
                         IResultFactory resultFactory, @Assisted IProject project,
                         @Assisted ILanguageImpl lang) {
        super(resultFactory, project, lang);
        this.syntaxService = syntaxService;
        this.unitService = unitService;
    }

    @Override
    protected FailOrSuccessResult<ParseResult, IResult> applyThrowing(InputResult a)
        throws ParseException {
        ISpoofaxInputUnit unit = a.unit();
        ISpoofaxParseUnit parse = syntaxService.parse(unit);

        if (!parse.valid()) {
            // Retry parsing with the default start symbol.
            unit = unitService.inputUnit(unit.source(), unit.text(), unit.langImpl(), null);
            parse = syntaxService.parse(unit);
        }

        return FailOrSuccessResult.ofSpoofaxResult(resultFactory.createParseResult(parse));
    }
}
