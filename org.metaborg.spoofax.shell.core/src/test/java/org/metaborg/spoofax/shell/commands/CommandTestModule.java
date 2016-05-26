package org.metaborg.spoofax.shell.commands;

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
    }

}
