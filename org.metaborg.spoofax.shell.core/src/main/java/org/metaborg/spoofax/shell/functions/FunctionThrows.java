package org.metaborg.spoofax.shell.functions;

import java.util.Objects;

/**
 * A {@link java.util.Function} that throws exceptions.
 * @param <A> the argument type
 * @param <B> the return type
 */
@FunctionalInterface
public interface FunctionThrows<A, B> {
    /**
     * A {@link java.util.Function} that throws exceptions.
     * @param a           the argument of the function
     * @return            the return value of the function
     * @throws Exception  should be subclassed in implementing types
     */
    B apply(A a) throws Exception;

    /**
     * Returns a composed {@link FunctionThrows} that executes this {@link FunctionThrows}
     * followed by the {@link FunctionThrows} passed as an argument..
     * @param after  the method that should be executed after this {@link FunctionThrows}
     * @param <C>    return type of the composed function
     * @return       the composed {@link FunctionThrows}
     */
    default <C> FunctionThrows<A, C>
    andThen(FunctionThrows<? super B, ? extends C> after) {
        Objects.requireNonNull(after);
        return (A a) -> after.apply(apply(a));
    }
}
