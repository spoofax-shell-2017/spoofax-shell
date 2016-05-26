package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.Map;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.UnitWrapper;
import org.metaborg.spoofax.shell.core.EvaluateUnit;
import org.metaborg.spoofax.shell.core.IEvaluationStrategy;
import org.metaborg.spoofax.shell.hooks.IResultHook;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents an evaluate command sent to Spoofax.
 */
public class EvaluateCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Evaluate an expression";

    private IContextService contextService;
    private ParseCommand parseCommand;
    private AnalyzeCommand analyzeCommand;
    private IResultFactory unitFactory;

    @Inject
    private Map<String, IEvaluationStrategy> evaluationStrategies;
    private EvaluationInvocationStrategy invocationStrategy;

    /**
     * Interface for what happens before evaluation of parsed input (i.e. either analyze or evaluate
     * as-is).
     */
    private interface EvaluationInvocationStrategy {
        EvaluateResult performEvaluation(IContext context, ParseResult parsed,
                                         IEvaluationStrategy evalStrategy)
            throws MetaborgException;
    }

    /**
     * Analyze before invoking the evaluation strategy.
     */
    private class AnalyzedInvocationStrategy implements EvaluationInvocationStrategy {
        @Override
        public EvaluateResult performEvaluation(IContext context, ParseResult parsed,
                                                IEvaluationStrategy evalStrategy)
            throws MetaborgException {
            AnalyzeResult analyzed = analyzeCommand.analyze(parsed);
            IStrategoTerm ast = evalStrategy.evaluate(analyzed, context);
            // TODO: Normally this is done in the unit service, but since this is my own Unit this
            // is not possible.
            EvaluateUnit<ISpoofaxAnalyzeUnit> unit =
                new EvaluateUnit<ISpoofaxAnalyzeUnit>(((UnitWrapper) analyzed.unit()).unit, ast,
                                                      context, analyzed.unit());
            return unitFactory.createEvaluateResult(unit);
        }
    }

    /**
     * Analyze before invoking the evaluation strategy.
     */
    private class NonAnalyzedInvocationStrategy implements EvaluationInvocationStrategy {
        @Override
        public EvaluateResult performEvaluation(IContext context, ParseResult parsed,
                                                IEvaluationStrategy evalStrategy)
            throws MetaborgException {
            IStrategoTerm ast = evalStrategy.evaluate(parsed, context);
            EvaluateUnit<ISpoofaxParseUnit> unit =
                new EvaluateUnit<ISpoofaxParseUnit>(((UnitWrapper) parsed.unit()).unit, ast,
                                                    context, parsed.unit());
            return unitFactory.createEvaluateResult(unit);
        }
    }

    /**
     * Instantiate an {@link EvaluateCommand}.
     *
     * @param contextService
     *            The {@link IContextService}.
     * @param commandFactory
     *            The {@link ICommandFactory} for creating delegate commands.
     * @param resultHook
     *            Called upon success of evaluation of this command.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    // CHECKSTYLE.OFF: |
    public EvaluateCommand(IContextService contextService, ICommandFactory commandFactory,
                           IResultFactory unitFactory,
                           IResultHook resultHook,
                           @Assisted IProject project, @Assisted ILanguageImpl lang,
                           @Assisted boolean analyzed) {
        // CHECKSTYLE.ON: |
        super(resultHook, unitFactory, project, lang);
        this.contextService = contextService;
        this.parseCommand = commandFactory.createParse(project, lang);
        this.analyzeCommand = commandFactory.createAnalyze(project, lang);
        this.unitFactory = unitFactory;

        this.invocationStrategy =
            analyzed ? new AnalyzedInvocationStrategy() : new NonAnalyzedInvocationStrategy();
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    private EvaluateResult evaluate(ParseResult parsed) throws MetaborgException {
        IContext context =
            parsed.context().orElse(contextService.get(parsed.source(), project, lang));
        ShellFacet facet = context.language().facet(ShellFacet.class);
        IEvaluationStrategy evalStrategy = evaluationStrategies.get(facet.getEvaluationMethod());
        return invocationStrategy.performEvaluation(context, parsed, evalStrategy);
    }

    @Override
    public void execute(String... args) throws MetaborgException {
        try {
            InputResult input = unitFactory.createInputResult(lang, write(args[0]), args[0]);
            ParseResult parse = parseCommand.parse(input);
            EvaluateResult result = this.evaluate(parse);
            resultHook.accept(result);
        } catch (IOException e) {
            throw new MetaborgException("Cannot write to temporary source file.");
        }
    }
}
