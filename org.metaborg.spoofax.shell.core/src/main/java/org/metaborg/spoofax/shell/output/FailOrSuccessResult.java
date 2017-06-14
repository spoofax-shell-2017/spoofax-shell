package org.metaborg.spoofax.shell.output;

import java.util.Objects;

import org.metaborg.spoofax.shell.functions.FailableFunction;

/**
 * An {@link IResult} that may be a failure or a success, with in either case a wrapped result that
 * is respectively the representation of the failure or the successful result. Our equivalent of an
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
        private final S result;

        private Successful(S result) {
            this.result = result;
        }

        @Override
        public void accept(IResultVisitor visitor) {
            result.accept(visitor);
        }

        @Override
        public <NewS extends IResult> FailOrSuccessResult<NewS, F>
                flatMap(FailableFunction<? super S, NewS, F> failable) {
            Objects.requireNonNull(failable);
            return failable.apply(result);
        }

        @Override
        public void accept(FailOrSuccessVisitor<S, F> visitor) {
            visitor.visitSuccess(result);
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
        private final F result;

        private Failed(F result) {
            this.result = result;
        }

        @Override
        public void accept(IResultVisitor visitor) {
            result.accept(visitor);
        }

        @Override
        public <NewS extends IResult> FailOrSuccessResult<NewS, F>
                flatMap(FailableFunction<? super S, NewS, F> failable) {
            Objects.requireNonNull(failable);
            return failed(result);
        }

        @Override
        public void accept(FailOrSuccessVisitor<S, F> visitor) {
            visitor.visitFailure(result);
        }
    }

    /**
     * An excepted result.
     *
     * @param <S>
     * @param <F>
     */
    private static final class Excepted<S extends IResult, F extends IResult>
        extends FailOrSuccessResult<S, F> {
        private final ExceptionResult result;

        private Excepted(ExceptionResult result) {
            this.result = result;
        }

        @Override
        public void accept(IResultVisitor visitor) {
            result.accept(visitor);
        }

        @Override
        public <NewS extends IResult> FailOrSuccessResult<NewS, F>
                flatMap(FailableFunction<? super S, NewS, F> failable) {
            Objects.requireNonNull(failable);
            return excepted(result);
        }

        @Override
        public void accept(FailOrSuccessVisitor<S, F> visitor) {
            visitor.visitException(result);
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
     *            The type of the (non-existing) {@link IResult} that caused a failure.
     */
    public static <S extends IResult, F extends IResult> FailOrSuccessResult<S, F>
            successful(S successfulResult) {
        return new Successful<>(successfulResult);
    }

    /**
     * Create an {@link FailOrSuccessResult} that represents an unsuccessful result (failure).
     *
     * @param failedResult
     *            The cause of the failure.
     * @return The {@link FailOrSuccessResult} with an unsuccessful result.
     * @param <S>
     *            The type of the non-existing successful {@link ISpoofaxResult}.
     * @param <F>
     *            The type of the {@link IResult} that caused a failure.
     */
    public static <S extends IResult, F extends IResult> FailOrSuccessResult<S, F>
            failed(F failedResult) {
        return new Failed<>(failedResult);
    }

    /**
     * Create an {@link FailOrSuccessResult} that represents an unsuccessful
     * result (exception).
     *
     * @param exceptionResult
     *            The cause of the exception.
     * @return The {@link FailOrSuccessResult} with an unsuccessful result.
     * @param <S>
     *            The type of the non-existing successful
     *            {@link ISpoofaxResult}.
     * @param <F>
     *            The type of the non-existing failure {@linkplain IResult}.
     */
    public static <S extends IResult, F extends IResult> FailOrSuccessResult<S, F> excepted(
            ExceptionResult exceptedResult) {
        return new Excepted<>(exceptedResult);
    }

    /**
     * Create an {@link FailOrSuccessResult} that, depending on whether the given
     * {@link ISpoofaxResult} is {@link ISpoofaxResult#valid valid}, represents either a success or
     * a failure. In the latter case, it wraps a {@link FailResult} as the failure, which upon
     * accepting a {@link IResultVisitor visitor} calls its
     * {@link IResultVisitor#visitFailure(FailResult) visitFailure method}.
     *
     * @param result
     *            The result which may be valid or invalid.
     * @return The {@link FailOrSuccessResult} with either a successful or a {@link FailResult}.
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

    public abstract void accept(FailOrSuccessVisitor<Success, Fail> visitor);

    /**
     * Maps the given {@link FailableFunction} if this {@link FailOrSuccessResult} represents a
     * success, otherwise just returns the same cause of the failure. The result of the mapping can
     * in turn be a success or a failure.
     *
     * @param other
     *            The {@link FailableFunction} to apply when this was a success.
     * @return The result, either a success or a failure.
     * @param <S>
     *            the new type of a successful result.
     */
    public abstract <S extends IResult> FailOrSuccessResult<S, Fail>
            flatMap(FailableFunction<? super Success, S, Fail> other);
}
