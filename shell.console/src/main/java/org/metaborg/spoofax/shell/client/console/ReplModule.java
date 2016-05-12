package org.metaborg.spoofax.shell.client.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandInvoker;
import org.metaborg.spoofax.shell.commands.StyledText;
import org.metaborg.spoofax.shell.core.CoreModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import jline.console.ConsoleReader;

/**
 * Bindings for Repl.
 */
public class ReplModule extends CoreModule {

    private void configureCommands() {
        MapBinder<String, IReplCommand> commandBinder =
                MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        commandBinder.addBinding("exit").to(Repl.ExitCommand.class).in(Singleton.class);

        bind(ICommandInvoker.class).to(SpoofaxCommandInvoker.class);
    }

    private void configureUserInterface() {
        bind(TerminalUserInterface.class).in(Singleton.class);
        bind(IEditor.class).to(TerminalUserInterface.class);
        bind(IDisplay.class).to(TerminalUserInterface.class);

        bind(InputStream.class).annotatedWith(Names.named("in")).toInstance(System.in);
        bind(OutputStream.class).annotatedWith(Names.named("out")).toInstance(System.out);
        bind(OutputStream.class).annotatedWith(Names.named("err")).toInstance(System.err);

        bind(new TypeLiteral<Consumer<StyledText>>() { }).annotatedWith(Names.named("onSuccess"))
                .to(OnEvalSuccessHook.class).in(Singleton.class);
        bind(new TypeLiteral<Consumer<StyledText>>() { }).annotatedWith(Names.named("onError"))
                .to(OnEvalErrorHook.class).in(Singleton.class);
    }

    @Override
    protected void configure() {
        super.configure();

        configureCommands();
        configureUserInterface();

        bind(Repl.class).in(Singleton.class);
    }

    /**
     * TODO: Replace with "CheckedProvides" because IO in Provider method is bad practice, see:
     * https://github.com/google/guice/wiki/ThrowingProviders.
     *
     * @param in
     *            The {@link InputStream}.
     * @param out
     *            The {@link OutputStream}.
     * @return a {@link ConsoleReader} with the given streams.
     * @throws IOException
     *             When an IO error occurs upon construction.
     */
    @Provides
    @Singleton
    protected ConsoleReader provideConsoleReader(@Named("in") InputStream in,
                                                 @Named("out") OutputStream out)
                                                     throws IOException {
        return new ConsoleReader(in, out);
    }
}
