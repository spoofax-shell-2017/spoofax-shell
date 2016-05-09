package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.StyledText;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Represents an evaluate command sent to Spoofax.
 */
public class EvaluateCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Evaluate an expression";

    @Inject
    private IStrategoRuntimeService runtimeService;
    @Inject
    private AnalyzeCommand analyzeCommand;

    /**
     * Instantiate an {@link EvaluateCommand}.
     * @param onSuccess Called upon success by the created {@link SpoofaxCommand}.
     * @param onError   Called upon an error by the created {@link SpoofaxCommand}.
     */
    @Inject
    public EvaluateCommand(@Named("onSuccess") final Consumer<StyledText> onSuccess,
                           @Named("onError") final Consumer<StyledText> onError) {
        super(onSuccess, onError);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Interprets a program using a {@link HybridInterpreter}.
     * Delegates parsing to the {@link ParseCommand} and analyzing to the {@link AnalyzeCommand}.
     * @param source the source of the program
     * @return an {@link IStrategoTerm}
     * @throws MetaborgException when parsing, analyzing or interpreting fails
     * @throws IOException when creating a temp file fails
     */
    public IStrategoTerm interp(String source) throws IOException, MetaborgException {
        return this.interp(analyzeCommand.analyze(source));
    }

    /**
     * Interprets a program using a {@link HybridInterpreter}.
     * Delegates analyzing to the {@link AnalyzeCommand}.
     * @param parseUnit a {@link ParseCommand} result
     * @return an {@link IStrategoTerm}
     * @throws MetaborgException when parsing, analyzing or interpreting fails
     * @throws IOException when creating a temp file fails
     */
    public IStrategoTerm interp(ISpoofaxParseUnit parseUnit) throws IOException, MetaborgException {
        return this.interp(analyzeCommand.analyze(parseUnit));
    }

    /**
     * Interprets a program using a {@link HybridInterpreter}.
     * @param analyzeUnit a {@link AnalyzeCommand} result
     * @return an {@link IStrategoTerm}
     * @throws MetaborgException when parsing, analyzing or interpreting fails
     */
    public IStrategoTerm interp(ISpoofaxAnalyzeUnit analyzeUnit) throws MetaborgException {
        final FacetContribution<StrategoRuntimeFacet> runContrib = this.context.language()
                .facetContribution(StrategoRuntimeFacet.class);
        if (runContrib == null) {
            throw new MetaborgException("Cannot retrieve the runtime facet");
        }
        final HybridInterpreter interpreter = runtimeService.runtime(
                runContrib.contributor, this.context);
        return common.invoke(interpreter, analyzeUnit.ast(), "runstrat");
    }

    /**
     * Evaluates {@code args}.
     * @param args The expression(s) to evaluate.
     */
    @Override
    public void execute(String... args) {
        try {
            this.onSuccess.accept(new StyledText(common.toString(this.interp(args[0]))));
        } catch (IOException | MetaborgException e) {
            e.printStackTrace();
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
