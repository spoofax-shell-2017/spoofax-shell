package org.metaborg.spoofax.shell.client;

import java.io.InputStream;
import java.io.OutputStream;

import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandInvoker;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class ReplModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(TerminalUserInterface.class).in(Singleton.class);
		bind(IEditor.class).to(TerminalUserInterface.class);
		bind(IDisplay.class).to(TerminalUserInterface.class);
		bind(ICommandInvoker.class).to(SpoofaxCommandInvoker.class);
		
		bind(InputStream.class).annotatedWith(Names.named("in")).toInstance(System.in);
		bind(OutputStream.class).annotatedWith(Names.named("out")).toInstance(System.out);
		bind(OutputStream.class).annotatedWith(Names.named("err")).toInstance(System.err);
	}
}
