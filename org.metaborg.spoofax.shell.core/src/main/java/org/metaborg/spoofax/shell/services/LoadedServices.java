package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.ISpoofaxTermResult;
import org.metaborg.spoofax.shell.output.PrintResult;
import org.metaborg.spoofax.shell.output.StyleResult;

/**
 * The strategy for {@link IEditorServices} when a language is loaded.
 */
public class LoadedServices implements IEditorServicesStrategy {

	private final FunctionComposer composer;

	/**
	 * Initializes the behaviour of a functional {@link IEditorServices}.
	 *
	 * <p>
	 * The behaviour is based on the provided <code>IFunctionComposer</code>.
	 * </p>
	 *
	 * @param composer
	 *            {@link FunctionComposer}
	 *            The language implementation that is used.
	 */
	protected LoadedServices(FunctionComposer composer) {
		this.composer = composer;
	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public FailOrSuccessResult<StyleResult, IResult> highlight(String source) {
		return composer.pStyleFunction().apply(source);
	}

    @Override
    public FailOrSuccessResult<PrintResult, IResult> foldAndPrint(ISpoofaxTermResult<?> input) {
        return composer.termPrettyPrintFunction().apply(input);
    }
}
