package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.stream.Collectors;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.shell.hooks.IResultHook;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.util.concurrent.IClosableLock;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents an analyze command sent to Spoofax.
 */
public class AnalyzeCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Analyze an expression.";

    private IContextService contextService;
    private ISpoofaxAnalysisService analysisService;
    private ParseCommand parseCommand;

    /**
     * Instantiate an {@link AnalyzeCommand}.
     *
     * @param contextService
     *            The {@link IContextService}.
     * @param analysisService
     *            The {@link IAnalysisService}
     * @param commandFactory
     *            The {@link ICommandFactory} used to create a {@link ParseCommand}.
     * @param resultHook
     *            The {@link IResultHook} to send results of successful evaluations to.
     * @param resultFactory
     *            The {@link ResultFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    public AnalyzeCommand(IContextService contextService, ISpoofaxAnalysisService analysisService,
                          ICommandFactory commandFactory, IResultHook resultHook,
                          IResultFactory resultFactory, @Assisted IProject project,
                          @Assisted ILanguageImpl lang) {
        super(resultHook, resultFactory, project, lang);
        this.contextService = contextService;
        this.analysisService = analysisService;
        this.parseCommand = commandFactory.createParse(project, lang);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Analyzes a {@link ProcessingUnit} using the {@link ISpoofaxAnalysisService}.
     *
     * @param unit
     *            The {@link ProcessingUnit} that is used as input.
     * @throws MetaborgException
     *             When analyzing fails.
     * @return An analyzed {@link ProcessingUnit}.
     */
    public AnalyzeResult analyze(ParseResult unit) throws MetaborgException {
        IContext context = unit.context().orElse(contextService.get(unit.source(), project, lang));

        ISpoofaxAnalyzeUnit analyze;
        try (IClosableLock lock = context.write()) {
            analyze = analysisService.analyze(unit.unit(), context).result();
        }
        AnalyzeResult result = resultFactory.createAnalyzeResult(analyze);

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
            ParseResult parse = parseCommand.parse(input);
            resultHook.accept(analyze(parse));
        } catch (IOException e) {
            throw new MetaborgException("Cannot write to temporary source file.");
        }
    }
}
