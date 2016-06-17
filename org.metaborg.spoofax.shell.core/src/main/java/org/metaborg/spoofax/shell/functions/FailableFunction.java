package org.metaborg.spoofax.shell.functions;

import java.util.Objects;
import java.util.function.Function;

import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;

/**
 * A {@link Function} that may result in a failure. This is our domain-specific equivalent
 * of a Kleisli arrow in Haskell.
 *
 * @param <In>
 *            the argument type
 * @param <Success>
 *            the return type of a success
 * @param <Fail>
 *            the return type of an error.
 */
@FunctionalInterface
public interface FailableFunction<In, Success extends IResult, Fail extends IResult>
    extends Function<In, FailOrSuccessResult<Success, Fail>> {

    /**
     * Apply this function, returning either a succes or an error.
     *
     * @param input
     *            The input of the function.
     * @return {@link FailOrSuccessResult An error or a success result}, depending on whether the
     *         application was successful or not.
     */
    @Override
    FailOrSuccessResult<Success, Fail> apply(In input);

    /**
     * Kleisli composition of this {@link FailableFunction}. Same as normal composition, except that
     * it works with our {@link FailOrSuccessResult}.
     *
     * @param other
     *            The {@link FailableFunction} to compose this {@link FailableFunction} with.
     * @return A composed {@link FailableFunction}.
     * @param <NewSuccess>
     *            The return type of a successful result.
     */
    default <NewSuccess extends IResult> FailableFunction<In, NewSuccess, Fail>
        kleisliCompose(FailableFunction<Success, NewSuccess, Fail> other) {
        Objects.requireNonNull(other);
        return a -> this.apply(a).flatMap(other);
    }
}
