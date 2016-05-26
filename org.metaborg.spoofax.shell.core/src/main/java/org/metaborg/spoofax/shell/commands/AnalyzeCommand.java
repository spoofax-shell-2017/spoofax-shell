package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.util.concurrent.IClosableLock;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

/**
 * Represents an analyze command sent to Spoofax.
 */
public class AnalyzeCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Analyze an expression.";

    private IContextService contextService;
    private ISpoofaxAnalysisService analysisService;
    private IResultFactory unitFactory;
    private ParseCommand parseCommand;

    /**
     * Instantiate an {@link AnalyzeCommand}.
     * @param contextService   The {@link IContextService}.
     * @param analysisService  The {@link IAnalysisService}
     * @param commandFactory   The {@link ICommandFactory} used to create a {@link ParseCommand}.
     * @param onSuccess        Called upon success by the created {@link SpoofaxCommand}.
     * @param onError          Called upon an error by the created {@link SpoofaxCommand}.
     * @param project          The project in which this command should operate.
     * @param lang             The language to which this command applies.
     */
    @Inject
    // CHECKSTYLE.OFF: |
    public AnalyzeCommand(IContextService contextService,
                          ISpoofaxAnalysisService analysisService,
                          ICommandFactory commandFactory,
                          IResultFactory unitFactory,
                          @Named("onSuccess") Consumer<StyledText> onSuccess,
                          @Named("onError") Consumer<StyledText> onError,
                          @Assisted IProject project,
                          @Assisted ILanguageImpl lang) {
    // CHECKSTYLE.ON: |
        super(onSuccess, onError, project, lang);
        this.contextService = contextService;
        this.analysisService = analysisService;
        this.unitFactory = unitFactory;
        this.parseCommand = commandFactory.createParse(project, lang);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Analyzes a {@link ProcessingUnit} using the {@link ISpoofaxAnalysisService}.
     * @param unit                The {@link ProcessingUnit} that is used as input.
     * @throws MetaborgException  When analyzing fails.
     * @return An analyzed {@link ProcessingUnit}.
     */
    public AnalyzeResult analyze(ParseResult unit) throws MetaborgException {
        IContext context = unit.context().orElse(contextService.get(unit.source(), project, lang));

        ISpoofaxAnalyzeUnit analyze;
        try (IClosableLock lock = context.write()) {
            analyze = analysisService.analyze(unit.unit(), context).result();
        }

        AnalyzeResult result = unitFactory.createAnalyzeResult(analyze);

        if (!result.valid()) {
            throw new MetaborgException("Invalid analysis result!");
        }
        return result;
    }

    @Override
    public void execute(String... args) {
        try {
            InputResult input = unitFactory.createInputResult(lang, write(args[0]), args[0]);
            ParseResult parse = parseCommand.parse(input);
            AnalyzeResult analyze = analyze(parse);

            this.onSuccess.accept(analyze.styled());
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
