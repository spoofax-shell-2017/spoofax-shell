package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.shell.client.ConsoleReplModule;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.console.impl.ConsoleRepl;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Injector;

/**
 * This class launches a {@link ConsoleRepl}, a console based REPL.
 */
// CHECKSTYLE.OFF: HideUtilityClassConstructor - No need for private constructor
public final class Main {
	// CHECKSTYLE.ON
	private static final String ERROR = "Invalid commandline parameters: %s%nThe only argument "
			+ "accepted is the path to a language implementation "
			+ "location, using any filesystem supported by Apache VFS";

	private static StyledText error(String[] args) {
		StringBuilder invalidArgs = new StringBuilder();
		for (String arg : args) {
			invalidArgs.append(arg).append(", ");
		}
		// Remove the appended ", " from the string.
		invalidArgs.delete(invalidArgs.length() - 2, invalidArgs.length());
		return new StyledText(Color.RED, String.format(ERROR, invalidArgs.toString()));
	}

	/**
	 * Instantiates and runs a new {@link ConsoleRepl}.
	 *
	 * @param args
	 *            The path to a language implementation location, using any filesystem supported by
	 *            Apache VFS.
	 * @throws MetaborgException
	 *             When Spoofax initialization fails.
	 */
	public static void main(String[] args) throws MetaborgException {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");

		try (final Spoofax spoofax = new Spoofax(new ConsoleReplModule())) {
			final Injector injector = spoofax.injector;
			final IDisplay display = injector.getInstance(IDisplay.class);
			final ConsoleRepl repl = injector.getInstance(ConsoleRepl.class);

			if (args.length == 1) {
				final String arg = args[0];
				if (arg.equals("--exit")) {
					return;
				}
				repl.runOnce(":load " + args[0]);
			} else if (args.length > 1) {
				display.displayStyledText(error(args));
			}

			repl.run();
		}
	}
}
