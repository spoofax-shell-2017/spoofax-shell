package org.metaborg.spoofax.shell.client;

import java.util.function.Consumer;

import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.shell.commands.AnalyzeCommand;
import org.metaborg.spoofax.shell.commands.EvaluateCommand;
import org.metaborg.spoofax.shell.commands.HelpCommand;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.ParseCommand;
import org.metaborg.spoofax.shell.core.CoreModule;
import org.metaborg.spoofax.shell.core.StyledText;
import org.metaborg.spoofax.shell.hooks.OnEvalErrorHook;
import org.metaborg.spoofax.shell.hooks.OnEvalSuccessHook;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * Client library bindings.
 */
public class ReplModule extends CoreModule {
    private IContext context;

    /**
     * Instantiates a new ReplModule.
     *
     * @param context
     *            The {@link IContext} in which the REPL operates.
     */
    public ReplModule(final IContext context) {
        this.context = context;
    }

    protected MapBinder<String, IReplCommand> commandBinder;

    /**
     * Binds the default commands.
     */
    protected void configureCommands() {
        commandBinder = MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        commandBinder.addBinding("exit").to(Repl.ExitCommand.class).in(Singleton.class);
        commandBinder.addBinding("help").to(HelpCommand.class).in(Singleton.class);
        commandBinder.addBinding("parse").to(ParseCommand.class).in(Singleton.class);
        commandBinder.addBinding("analyze").to(AnalyzeCommand.class).in(Singleton.class);
        // commandBinder.addBinding("eval").to(EvaluateCommand.class).in(Singleton.class);
        // FIXME: DIRTY HACK
        bind(IReplCommand.class).annotatedWith(Names.named("EvalCommand")).to(EvaluateCommand.class)
            .in(Singleton.class);
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

        bind(IContext.class).toInstance(this.context);
    }

}
