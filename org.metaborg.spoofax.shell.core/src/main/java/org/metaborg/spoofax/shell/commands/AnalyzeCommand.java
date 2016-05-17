package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.StyledText;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Represents an analyze command sent to Spoofax.
 */
public class AnalyzeCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Analyze an expression.";

    @Inject
    private ISpoofaxAnalysisService analysisService;
    @Inject
    private ParseCommand parseCommand;

    /**
     * Instantiate an {@link AnalyzeCommand}.
     * @param onSuccess Called upon success by the created {@link SpoofaxCommand}.
     * @param onError   Called upon an error by the created {@link SpoofaxCommand}.
     */
    @Inject
    public AnalyzeCommand(@Named("onSuccess") Consumer<StyledText> onSuccess,
                          @Named("onError") Consumer<StyledText> onError) {
        super(onSuccess, onError);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Analyzes a program using the {@link ISpoofaxAnalysisService}. Delegates parsing to the
     * {@link ParseCommand}.
     *
     * @param source
     *            The source of the program.
     * @param sourceFile
     *            The temporary file containing the source of the program.
     * @return An {@link ISpoofaxAnalyzeUnit}.
     * @throws MetaborgException
     *             When parsing or analyzing fails.
     * @throws IOException
     *             When writing to the temporary file fails.
     */
    public ISpoofaxAnalyzeUnit analyze(String source, FileObject sourceFile)
            throws MetaborgException, IOException {
        ISpoofaxParseUnit parse = parseCommand.parse(source, sourceFile);
        return this.analyze(parse);
    }

    /**
     * Analyzes a program using the {@link ISpoofaxAnalysisService}.
     *
     * @param parseUnit
     *            A {@link ParseCommand} result.
     * @return An {@link ISpoofaxAnalyzeUnit}.
     * @throws MetaborgException
     *             When analyzing fails.
     */
    public ISpoofaxAnalyzeUnit analyze(ISpoofaxParseUnit parseUnit) throws MetaborgException {
        ISpoofaxAnalyzeResult analyzeResult = analysisService.analyze(parseUnit, this.context);
        ISpoofaxAnalyzeUnit analyzeUnit = analyzeResult.result();

        if (!analyzeUnit.valid()) {
            StringBuilder builder = new StringBuilder();
            analyzeUnit.messages().forEach(builder::append);
            throw new MetaborgException(builder.toString());
        }
        return analyzeUnit;
    }

    @Override
    public void execute(String... args) {
        try {
            FileObject tempFile = this.context.location().resolveFile("tmp.src");
            IStrategoTerm term = this.analyze(args[0], tempFile).ast();

            this.onSuccess.accept(new StyledText(common.toString(term)));
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
