package org.metaborg.spoofax.shell.commands;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

public class LanguageCommand implements IReplCommand {

    private ILanguageDiscoveryService langDiscoveryService;
    private IResourceService resourceService;
    private Consumer<StyledText> errorHook;
    private Consumer<StyledText> successHook;
    private Consumer<ICommandInvoker> invokerHook;
    private Injector injector;

    @Inject
    public LanguageCommand(Injector injector,
                           Map<String, IReplCommand> commands,
                           ILanguageDiscoveryService langDiscoveryService,
                           IResourceService resourceService,
                           @Named("onSuccess") Consumer<StyledText> successHook,
                           @Named("onError") Consumer<StyledText> errorHook,
                           Consumer<ICommandInvoker> invokerHook) {
        this.injector = injector;
        this.langDiscoveryService = langDiscoveryService;
        this.resourceService = resourceService;

        this.successHook = successHook;
        this.errorHook = errorHook;
        this.invokerHook = invokerHook;
    }

    @Override
    public String description() {
        return "Load a language from a path.";
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length == 0 || args.length > 1) {
                throw new MetaborgException("Syntax: :lang <path>");
            }

            FileObject resolve = resourceService.resolve("zip:" + args[0] + "!/");
            Iterable<ILanguageDiscoveryRequest> requests = langDiscoveryService.request(resolve);
            Iterable<ILanguageComponent> components = langDiscoveryService.discover(requests);

            Set<ILanguageImpl> implementations = LanguageUtils.toImpls(components);
            ILanguageImpl lang = LanguageUtils.active(implementations);

            if (lang == null) {
                throw new MetaborgException("Cannot find a language implementation");
            }

            injector.createChildInjector(e -> {
                MapBinder<String, IReplCommand> commandBinder =
                        MapBinder.newMapBinder(e, String.class, IReplCommand.class);
                commandBinder.addBinding("test").toInstance(new IReplCommand() {
                    @Override
                    public void execute(String... args) {
                        successHook.accept(new StyledText("Hello"));
                    }

                    @Override
                    public String description() {
                        return "test";
                    }
                });
            });

            invokerHook.accept(injector.getInstance(ICommandInvoker.class));
            successHook.accept(new StyledText("Loaded language " + lang));
        } catch (MetaborgException e) {
            errorHook.accept(new StyledText(e.getMessage()));
        }
    }

}
