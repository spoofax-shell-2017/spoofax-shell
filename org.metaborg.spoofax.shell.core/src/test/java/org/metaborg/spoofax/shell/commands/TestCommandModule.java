package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Sets some overrides to be used with the JUnit tests.
 */
public class TestCommandModule extends AbstractModule {
    /**
     * Creates an {@link IProject} to be used for testing commands.
     *
     * @param service
     *            The {@link SimpleProjectService}.
     * @param resourceService
     *            The {@link IResourceService}.
     * @return An {@link IProject}.
     * @throws MetaborgException
     *             When the project could not be created.
     */
    @Provides
    public IProject project(SimpleProjectService service, IResourceService resourceService)
            throws MetaborgException {
        return service.create(resourceService.resolve("tmp:"));
    }

    /**
     * Creates an {@link IContext} to be used for testing commands.
     *
     * @param contService
     *            The {@link IContextService}.
     * @param project
     *            The {@link IProject}.
     * @param lang
     *            The {@link ILanguageImpl}.
     * @return An {@link IContext}.
     * @throws ContextException
     *             When the context could not be created.
     */
    @Provides
    public IContext context(IContextService contService, IProject project, ILanguageImpl lang)
            throws ContextException {
        return contService.getTemporary(project.location(), project, lang);
    }

    /**
     * Provides a language implementation.
     *
     * @param resService
     *            The {@link IResourceService} used to load the language.
     * @param langService
     *            The {@link ILanguageDiscoveryService} used to discover facets.
     * @return The {@link ILanguageImpl}.
     * @throws MetaborgException
     *             When the language fails to load.
     */
    @Provides
    public ILanguageImpl lang(IResourceService resService, ILanguageDiscoveryService langService)
            throws MetaborgException {
        FileObject cpresolve = resService.resolve("res:paplj.full");
        FileObject resolve = resService.resolve("zip:" + cpresolve + "!/");
        Iterable<ILanguageDiscoveryRequest> requests = langService.request(resolve);
        Iterable<ILanguageComponent> components = langService.discover(requests);
        return LanguageUtils.active(LanguageUtils.toImpls(components));
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<Consumer<StyledText>>() { })
            .annotatedWith(Names.named("onSuccess"))
            .toInstance((s) -> { });
        bind(new TypeLiteral<Consumer<StyledText>>() { })
            .annotatedWith(Names.named("onError"))
            .toInstance((s) -> { });
    }
}