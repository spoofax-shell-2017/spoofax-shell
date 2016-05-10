package org.metaborg.spoofax.shell.middle;

import org.metaborg.spoofax.shell.commands.IReplCommand;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * Module for test bindings.
 */
public class CommandTestModule extends AbstractModule {

    @Override
    protected void configure() {
        MapBinder<String, IReplCommand> commandBinder =
            MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        commandBinder.addBinding("existing-command").toInstance(new IReplCommand() {

            @Override
            public void execute(String... args) {
            }

            @Override
            public String description() {
                return "dummy";
            }
        });
        bind(IReplCommand.class).annotatedWith(Names.named("EvalCommand"))
            .toInstance(new IReplCommand() {

                @Override
                public void execute(String... args) {
                }

                @Override
                public String description() {
                    return "dummy";
                }
            });
    }

}
