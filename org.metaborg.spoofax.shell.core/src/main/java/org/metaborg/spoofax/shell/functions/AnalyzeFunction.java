package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.util.concurrent.IClosableLock;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents a parse command sent to Spoofax.
 */
public class AnalyzeFunction extends AbstractSpoofaxFunction<ParseResult, AnalyzeResult> {
    private final IContextService contextService;
    private final ISpoofaxAnalysisService analysisService;

    /**
     * Instantiate an {@link AnalyzeCommand}.
     *
     * @param contextService
     *            The {@link IContextService}.
     * @param analysisService
     *            The {@link IAnalysisService}
     * @param resultFactory
     *            The {@link ResultFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    public AnalyzeFunction(IContextService contextService, ISpoofaxAnalysisService analysisService,
                           IResultFactory resultFactory, @Assisted IProject project,
                           @Assisted ILanguageImpl lang) {
        super(resultFactory, project, lang);
        this.contextService = contextService;
        this.analysisService = analysisService;
    }

    @Override
    protected FailOrSuccessResult<AnalyzeResult, IResult> applyThrowing(ParseResult a)
        throws Exception {
        IContext context = a.context().orElse(contextService.get(a.source(), project, lang));
        ISpoofaxAnalyzeUnit analyze;
        try (IClosableLock lock = context.write()) {
            analyze = analysisService.analyze(a.unit(), context).result();
        }
        return FailOrSuccessResult.ofSpoofaxResult(resultFactory.createAnalyzeResult(analyze));
    }
}
