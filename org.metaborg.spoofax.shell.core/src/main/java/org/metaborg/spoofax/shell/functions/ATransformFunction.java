package org.metaborg.spoofax.shell.functions;

import java.util.Collection;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ATransformFunction extends AbstractFunction<AnalyzeResult, TransformResult>{

    private ISpoofaxTransformService transformService;
    private ITransformAction action;

    /**
     * Instantiate a new {@link AnalyzedTransformCommand}.
     *
     * @param transformService
     *            The {@link ISpoofaxTransformService}.
     * @param resultFactory
     *            The {@link ResultFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     * @param action
     *            The {@link ITransformAction} that this command executes.
     */
    @Inject
    public ATransformFunction(ISpoofaxTransformService transformService,
                              IResultFactory resultFactory, @Assisted IProject project,
                              @Assisted ILanguageImpl lang,
                              @Assisted ITransformAction action) {
        super(resultFactory, project, lang);
        this.transformService = transformService;
        this.action = action;
    }

    @Override
    public TransformResult execute(AnalyzeResult arg) throws MetaborgException {
        ISpoofaxAnalyzeUnit unit = arg.unit();
        IContext context = unit.context();

        Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> transform =
            transformService.transform(unit, context, action.goal());
        return resultFactory.createTransformResult(transform.iterator().next());
    }
}
