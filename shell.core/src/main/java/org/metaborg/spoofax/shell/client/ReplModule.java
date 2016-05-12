package org.metaborg.spoofax.shell.client;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.StyledText;
import org.metaborg.spoofax.shell.core.CoreModule;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * Client library bindings.
 */
public class ReplModule extends CoreModule {
    protected MapBinder<String, IReplCommand> commandBinder;

    /**
     * Binds the default commands.
     */
    protected void configureCommands() {
        commandBinder = MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        commandBinder.addBinding("exit").to(Repl.ExitCommand.class).in(Singleton.class);
    }

    @Override
    protected void configure() {
        super.configure();

        configureCommands();

        // @formatter:off
        bind(new TypeLiteral<Consumer<StyledText>>() { }).annotatedWith(Names.named("onSuccess"))
                .to(OnEvalSuccessHook.class).in(Singleton.class);
        bind(new TypeLiteral<Consumer<StyledText>>() { }).annotatedWith(Names.named("onError"))
                .to(OnEvalErrorHook.class).in(Singleton.class);
        // @formatter:on
    }

}
