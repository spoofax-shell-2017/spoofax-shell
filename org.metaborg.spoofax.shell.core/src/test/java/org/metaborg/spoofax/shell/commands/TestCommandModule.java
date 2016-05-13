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
     * Creates a project to be used for testing commands.
     * @param service the {@link SimpleProjectService}
     * @param resourceService the {@link IResourceService}
     * @return an {@link IProject}
     * @throws MetaborgException when the project could not be created
     */
    @Provides
    IProject project(SimpleProjectService service, IResourceService resourceService)
            throws MetaborgException {
        return service.create(resourceService.resolve("tmp:"));
    }

    /**
     * Creates a context to be used for testing commands.
     * @param contService the {@link IContextService}
     * @param project the {@link IProject}
     * @param lang the {@link ILanguageImpl}
     * @return an {@link IContext}
     * @throws ContextException when the context could not be created
     */
    @Provides
    IContext context(IContextService contService, IProject project, ILanguageImpl lang)
            throws ContextException {
        return contService.getTemporary(project.location(), project, lang);
    }

    /**
     * Provides a language implementation.
     * @param resService the {@link IResourceService} used to load the language
     * @param langService the {@link ILanguageDiscoveryService} used to discover facets
     * @return the {@link ILanguageImpl}
     * @throws MetaborgException when the language fails to load
     */
    @Provides
    public ILanguageImpl lang(IResourceService resService, ILanguageDiscoveryService langService)
            throws MetaborgException {
        FileObject cpresolve = resService.resolve("res:paplj.full");
        FileObject resolve = resService.resolve("zip:" + cpresolve + "!/");
        final Iterable<ILanguageDiscoveryRequest> requests = langService.request(resolve);
        final Iterable<ILanguageComponent> components = langService.discover(requests);
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