package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.StyledText;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

/**
 * Represents an evaluate command sent to Spoofax.
 */
public class EvaluateCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Evaluate an expression";

    private IContextService contextService;
    private IStrategoRuntimeService runtimeService;

    @Inject
    private AnalyzeCommand analyzeCommand;

    /**
     * Instantiate an {@link EvaluateCommand}.
     * @param common    The {@link IStrategoCommon} service.
     * @param contextService The {@link IContextService}.
     * @param runtimeService The {@link IStrategoRuntimeService}.
     * @param onSuccess Called upon success by the created {@link SpoofaxCommand}.
     * @param onError   Called upon an error by the created {@link SpoofaxCommand}.
     * @param project   The project in which this command should operate.
     * @param lang      The language to which this command applies.
     */
    @Inject
    public EvaluateCommand(IStrategoCommon common,
                           IContextService contextService,
                           IStrategoRuntimeService runtimeService,
                           @Named("onSuccess") Consumer<StyledText> onSuccess,
                           @Named("onError") Consumer<StyledText> onError,
                           @Assisted IProject project,
                           @Assisted ILanguageImpl lang) {
        super(common, onSuccess, onError, project, lang);
        this.contextService = contextService;
        this.runtimeService = runtimeService;
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
        IContext context = contextService.get(analyzeUnit.source(), project, lang);
        FacetContribution<StrategoRuntimeFacet> runContrib =
                lang.facetContribution(StrategoRuntimeFacet.class);
        if (runContrib == null) {
            throw new MetaborgException("Cannot retrieve the runtime facet");
        }
        HybridInterpreter interpreter =
            runtimeService.runtime(runContrib.contributor, context);
        return common.invoke(interpreter, analyzeUnit.ast(), "runstrat");
    }

    @Override
    public void execute(String... args) {
        try {
            IStrategoTerm term = this.interp(args[0], write(args[0]));

            this.onSuccess.accept(new StyledText(common.toString(term)));
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
