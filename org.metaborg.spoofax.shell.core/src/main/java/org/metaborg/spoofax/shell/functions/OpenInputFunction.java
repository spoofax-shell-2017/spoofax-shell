package org.metaborg.spoofax.shell.functions;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 */
public class OpenInputFunction extends AbstractSpoofaxFunction<String, InputResult> {

    private IResourceService resourceService;

    /**
     * Instantiate a {@link InputFunction}.
     *
     * @param resultFactory
     *            The {@link IResultFactory}.
     * @param resourceService
     *            The {@link IResourceService}.
     * @param project
     *            The {@link IProject} in which this command should operate.
     * @param lang
     *            The {@link ILanguageImpl} to which this command applies.
     */
    @Inject
    public OpenInputFunction(IResultFactory resultFactory, IResourceService resourceService,
                             @Assisted IProject project, @Assisted ILanguageImpl lang) {
        super(resultFactory, project, lang);
        this.resourceService = resourceService;
    }

    @Override
    protected FailOrSuccessResult<InputResult, IResult> applyThrowing(String path)
        throws Exception {
        ShellFacet shellFacet = lang.facet(ShellFacet.class);
        if (shellFacet == null) {
            throw new MetaborgException("Cannot find the shell facet.");
        }

        FileObject file = resourceService.resolve(path);
        String source = IOUtils.toString(file.getContent().getInputStream());
        return FailOrSuccessResult.ofSpoofaxResult(resultFactory
            .createInputResult(lang, file, source,
                               new JSGLRParserConfiguration(shellFacet.getShellStartSymbol())));
    }

}
