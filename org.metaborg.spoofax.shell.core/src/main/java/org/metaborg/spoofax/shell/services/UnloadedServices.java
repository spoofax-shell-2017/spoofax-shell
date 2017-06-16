package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyleResult;

/**
 * The strategy for {@link IEditorServices} when no language is loaded.
 *
 * <p>
 * All method calls (that require a language definition) should return a negative
 * {@link FailOrSuccessResult} containing a {@link ServiceUnavailableException}.
 * </p>
 */
public class UnloadedServices implements IEditorServicesStrategy {

	/**
	 * Protected constructor hides the strategy from other packages.
	 */
	protected UnloadedServices() {
	}

	/**
	 * Creates an exception that can be used to return to the client as a failure.
	 *
	 * @return {@link ExceptionResult} - An exception about the language being unavailable.
	 */
	private ExceptionResult createException(String service) {
		return new ExceptionResult(new ServiceUnavailableException(service));
	}

	@Override
	public boolean isLoaded() {
		return false;
	}

	@Override
	public FailOrSuccessResult<StyleResult, IResult> highlight(String source) {
		return FailOrSuccessResult.excepted(createException("Syntax Highlighting"));
	}
}
