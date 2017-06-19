package org.metaborg.spoofax.shell.output;

/**
 * Visitor pattern to dispatch a {@link FailOrSuccessResult} with the specified parametric types.
 *
 * The dispatch happens based on the dynamic type of the <code>FailorSuccessResult</code>, not on
 * its wrapped {@link IResult}.
 *
 * @param <Success>
 *            The type of the wrapped result if it is successful.
 * @param <Fail>
 *            The type of the wrapped result if it has failed.
 */
public interface FailOrSuccessVisitor<Success extends IResult, Fail extends IResult> {

	/**
	 * The dispatch for a successful result.
	 *
	 * @param result
	 *            <code>Success</code> - The successful result of the expected
	 *            type.
	 */
	void visitSuccess(Success result);

	/**
	 * The dispatch for a failed result.
	 *
	 * @param result
	 *            <code>Fail</code> - The failed result of the expected type.
	 */
	void visitFailure(Fail result);

	/**
	 * The dispatch for an exception (that is returned as a result).
	 *
	 * @param result
	 *            <{@link ExceptionResult} - The result containing the
	 *            exception.
	 */
	void visitException(ExceptionResult result);

}
