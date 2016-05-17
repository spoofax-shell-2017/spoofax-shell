package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
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
    public EvaluateCommand(@Named("onSuccess") Consumer<StyledText> onSuccess,
                           @Named("onError") Consumer<StyledText> onError) {
        super(onSuccess, onError);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Interprets a program using a {@link HybridInterpreter}. Delegates parsing to the
     * {@link ParseCommand} and analyzing to the {@link AnalyzeCommand}.
     *
     * @param source
     *            The source of the program.
     * @param sourceFile
     *            The temporary file containing the source of the program.
     * @return An {@link IStrategoTerm}.
     * @throws MetaborgException
     *             When parsing, analyzing or interpreting fails.
     * @throws IOException
     *             When writing to the temporary file fails.
     */
    public IStrategoTerm interp(String source, FileObject sourceFile)
            throws IOException, MetaborgException {
        return this.interp(analyzeCommand.analyze(source, sourceFile));
    }

    /**
     * Interprets a program using a {@link HybridInterpreter}. Delegates analyzing to the
     * {@link AnalyzeCommand}.
     *
     * @param parseUnit
     *            A {@link ParseCommand} result.
     * @return An {@link IStrategoTerm}.
     * @throws MetaborgException
     *             When analyzing or interpreting fails
     */
    public IStrategoTerm interp(ISpoofaxParseUnit parseUnit) throws MetaborgException {
        return this.interp(analyzeCommand.analyze(parseUnit));
    }

    /**
     * Interprets a program using a {@link HybridInterpreter}.
     *
     * @param analyzeUnit
     *            An {@link AnalyzeCommand} result.
     * @return An {@link IStrategoTerm}.
     * @throws MetaborgException
     *             When interpreting fails.
     */
    public IStrategoTerm interp(ISpoofaxAnalyzeUnit analyzeUnit) throws MetaborgException {
        FacetContribution<StrategoRuntimeFacet> runContrib = this.context.language()
                .facetContribution(StrategoRuntimeFacet.class);
        if (runContrib == null) {
            throw new MetaborgException("Cannot retrieve the runtime facet");
        }
        HybridInterpreter interpreter =
            runtimeService.runtime(runContrib.contributor, this.context);
        return common.invoke(interpreter, analyzeUnit.ast(), "runstrat");
    }

    @Override
    public void execute(String... args) {
        try {
            FileObject tempFile = this.context.location().resolveFile("tmp.src");
            IStrategoTerm term = this.interp(args[0], tempFile);

            this.onSuccess.accept(new StyledText(common.toString(term)));
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
