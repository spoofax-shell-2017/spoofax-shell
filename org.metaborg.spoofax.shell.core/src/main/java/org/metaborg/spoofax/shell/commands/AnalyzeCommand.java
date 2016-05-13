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
    public AnalyzeCommand(@Named("onSuccess") final Consumer<StyledText> onSuccess,
                          @Named("onError") final Consumer<StyledText> onError) {
        super(onSuccess, onError);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Analyzes a program using the {@link ISpoofaxAnalysisService}.
     * Delegates parsing to the {@link ParseCommand}.
     * @param source the source of the program
     * @param sourceFile the file containing the source of the program
     * @return an {@link ISpoofaxAnalyzeUnit}
     * @throws MetaborgException when parsing or analyzing fails
     * @throws IOException when creating a temp file fails
     */
    public ISpoofaxAnalyzeUnit analyze(String source, FileObject sourceFile)
            throws MetaborgException, IOException {
        ISpoofaxParseUnit parse = parseCommand.parse(source, sourceFile);
        ISpoofaxAnalyzeUnit analyze = this.analyze(parse);
        return analyze;
    }

    /**
     * Analyzes a program using the {@link ISpoofaxAnalysisService}.
     * @param parseUnit a {@link ParseCommand} result
     * @return an {@link ISpoofaxAnalyzeUnit}
     * @throws MetaborgException when analyzing fails
     */
    public ISpoofaxAnalyzeUnit analyze(ISpoofaxParseUnit parseUnit) throws MetaborgException {
        ISpoofaxAnalyzeResult analyzeResult = analysisService.analyze(parseUnit, this.context);
        ISpoofaxAnalyzeUnit analyzeUnit = analyzeResult.result();

        StringBuilder builder = new StringBuilder();
        analyzeUnit.messages().forEach(builder::append);

        if (!analyzeUnit.valid()) {
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
