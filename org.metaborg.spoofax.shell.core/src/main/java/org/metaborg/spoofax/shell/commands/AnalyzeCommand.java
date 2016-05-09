package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.StyledText;

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
     * @return an {@link ISpoofaxAnalyzeUnit}
     * @throws MetaborgException when parsing or analyzing fails
     * @throws IOException when creating a temp file fails
     */
    public ISpoofaxAnalyzeUnit analyze(String source) throws IOException, MetaborgException {
        return this.analyze(parseCommand.parse(source));
    }

    /**
     * Analyzes a program using the {@link ISpoofaxAnalysisService}.
     * @param parseUnit a {@link ParseCommand} result
     * @return an {@link ISpoofaxAnalyzeUnit}
     * @throws MetaborgException when analyzing fails
     */
    public ISpoofaxAnalyzeUnit analyze(ISpoofaxParseUnit parseUnit) throws MetaborgException {
        ISpoofaxAnalyzeUnit analyzeUnit = analysisService.analyze(parseUnit, this.context).result();
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
            this.onSuccess.accept(new StyledText(common.toString(this.analyze(args[0]).ast())));
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
