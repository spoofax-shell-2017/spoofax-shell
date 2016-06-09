package org.metaborg.spoofax.shell.output;

import java.util.Objects;

import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.functions.FailableFunction;

/**
 * An {@link IResult} that may be an error or a success, with in either case a wrapped result that
 * is respectively the representation of the error or the successful result. Our equivalent of an
 * "Either" type seen in some functional languages.
 *
 * @param <Success>
 *            The type of the successful {@link IResult}.
 * @param <Fail>
 *            The type of the failed {@link IResult}.
 */
public abstract class FailOrSuccessResult<Success extends IResult, Fail extends IResult>
    implements IResult {

    /**
     * A successful result.
     *
     * @param <S>
     * @param <F>
     */
    private static final class Successful<S extends IResult, F extends IResult>
        extends FailOrSuccessResult<S, F> {
        private S result;

        private Successful(S result) {
            this.result = result;
        }

        @Override
        public void accept(IResultVisitor visitor) {
            result.accept(visitor);
        }

        @Override
        public <NewS extends ISpoofaxResult<?>> FailOrSuccessResult<NewS, F>
            flatMap(FailableFunction<S, NewS, F> errorFunc) {
            Objects.requireNonNull(errorFunc);
            return errorFunc.apply(result);
        }
    }

    /**
     * A failed result.
     *
     * @param <S>
     * @param <F>
     */
    private static final class Failed<S extends IResult, F extends IResult>
        extends FailOrSuccessResult<S, F> {
        private F result;

        private Failed(F result) {
            this.result = result;
        }

        @Override
        public void accept(IResultVisitor visitor) {
            result.accept(visitor);
        }

        @Override
        public <NewS extends ISpoofaxResult<?>> FailOrSuccessResult<NewS, F>
            flatMap(FailableFunction<S, NewS, F> errorFunc) {
            Objects.requireNonNull(errorFunc);
            return failed(result);
        }
    }

    /**
     * Create an {@link FailOrSuccessResult} that represents a successful result.
     *
     * @param successfulResult
     *            The successful result.
     * @return The {@link FailOrSuccessResult} with a wrapped result.
     * @param <S>
     *            The type of the wrapped {@link IResult}.
     * @param <F>
     *            The type of the non-existing {@link IResult} that caused an error.
     */
    public static <S extends IResult, F extends IResult> FailOrSuccessResult<S, F>
        successful(S successfulResult) {
        return new Successful<S, F>(successfulResult);
    }

    /**
     * Create an {@link FailOrSuccessResult} that represents an unsuccessful result (error).
     *
     * @param failedResult
     *            The cause of the error.
     * @return The {@link FailOrSuccessResult} with an unsuccessful result.
     * @param <S>
     *            The type of the non-existing successful {@link ISpoofaxResult}.
     * @param <F>
     *            The type of the {@link IResult} that caused an error.
     */
    public static <S extends IResult, F extends IResult> FailOrSuccessResult<S, F>
        failed(F failedResult) {
        return new Failed<S, F>(failedResult);
    }

    /**
     * Create an {@link FailOrSuccessResult} that, depending on whether the given
     * {@link ISpoofaxResult} is {@link ISpoofaxResult#valid valid}, represents either a success or
     * an error.
     *
     * @param result
     *            The cause of the error.
     * @return The {@link FailOrSuccessResult} with either a successful or an error result.
     * @param <S>
     *            The type of the result {@link ISpoofaxResult}.
     */
    public static <S extends ISpoofaxResult<?>> FailOrSuccessResult<S, IResult>
        ofSpoofaxResult(S result) {
        if (result.valid()) {
            return successful(result);
        } else {
            return failed(new FailResult(result));
        }
    }

    @Override
    public abstract void accept(IResultVisitor visitor);

    /**
     * Maps the given {@link FailableFunction} if this {@link FailOrSuccessResult} represents a
     * success, otherwise just returns the same cause of the error. The result of the mapping can in
     * turn be a success or an error.
     *
     * @param errorFunc
     *            The {@link FailableFunction} to apply when this was a success.
     * @return The result, either a success or an error.
     * @param <S>
     *            the new type of a successful result.
     */
    public abstract <S extends ISpoofaxResult<?>> FailOrSuccessResult<S, Fail>
        flatMap(FailableFunction<Success, S, Fail> errorFunc);
}
