package org.metaborg.spoofax.shell.output;

public interface FailOrSuccessVisitor<Success extends IResult, Fail extends IResult> {

	/**
	 * The dispatch for a successful result.
	 *
	 * @param result
	 *            <code>Success<code> - The successful result of the expected
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
