package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.StyledText;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.util.concurrent.IClosableLock;
import org.spoofax.interpreter.terms.IStrategoTerm;

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
    private ParseCommand parseCommand;

    /**
     * Instantiate an {@link AnalyzeCommand}.
     * @param common           The {@link IStrategoCommon} service.
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
    public AnalyzeCommand(IStrategoCommon common,
                          IContextService contextService,
                          ISpoofaxAnalysisService analysisService,
                          ICommandFactory commandFactory,
                          @Named("onSuccess") Consumer<StyledText> onSuccess,
                          @Named("onError") Consumer<StyledText> onError,
                          @Assisted IProject project,
                          @Assisted ILanguageImpl lang) {
    // CHECKSTYLE.ON: |
        super(common, onSuccess, onError, project, lang);
        this.contextService = contextService;
        this.analysisService = analysisService;
        this.parseCommand = commandFactory.createParse(project, lang);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Analyzes a program using the {@link ISpoofaxAnalysisService}. Delegates parsing to the
     * {@link ParseCommand}.
     *
     * @param source              The source of the program.
     * @param sourceFile          The temporary file containing the source of the program.
     * @throws MetaborgException  When parsing or analyzing fails.
     * @return An {@link ISpoofaxAnalyzeUnit}.
     */
    public ISpoofaxAnalyzeUnit analyze(String source, FileObject sourceFile)
            throws MetaborgException {
        ISpoofaxParseUnit parse = parseCommand.parse(source, sourceFile);
        return this.analyze(parse);
    }

    /**
     * Analyzes a program using the {@link ISpoofaxAnalysisService}.
     *
     * @param parseUnit           A {@link ParseCommand} result.
     * @throws MetaborgException  When analyzing fails.
     * @return An {@link ISpoofaxAnalyzeUnit}.
     */
    public ISpoofaxAnalyzeUnit analyze(ISpoofaxParseUnit parseUnit) throws MetaborgException {
        IContext context = contextService.get(parseUnit.source(), project, lang);

        ISpoofaxAnalyzeUnit analyzeUnit;
        try (IClosableLock lock = context.write()) {
            analyzeUnit = analysisService.analyze(parseUnit, context).result();

            if (!analyzeUnit.valid()) {
                StringBuilder builder = new StringBuilder();
                analyzeUnit.messages().forEach(builder::append);
                throw new MetaborgException(builder.toString());
            }
        }
        return analyzeUnit;
    }

    @Override
    public void execute(String... args) {
        try {
            IStrategoTerm term = this.analyze(args[0], write(args[0])).ast();

            this.onSuccess.accept(new StyledText(common.toString(term)));
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
