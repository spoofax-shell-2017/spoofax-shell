package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Sets some overrides to be used with the JUnit tests.
 */
public class TestCommandModule extends AbstractModule {

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