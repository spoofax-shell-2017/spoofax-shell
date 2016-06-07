package org.metaborg.spoofax.shell.functions;

import java.util.function.Function;

import org.metaborg.core.MetaborgException;

/**
 * A {@link Function} that throws a {@link MetaborgException}.
 * @param <A> the argument type
 * @param <R> the return type
 */
public interface FunctionThrows<A, R> {
    /**
     * A {@link Function} that throws a {@link MetaborgException}.
     * @param a  the argument of the function
     * @return   the return value of the function
     * @throws MetaborgException  when an exception occurs in Spoofax
     */
    R apply(A a) throws MetaborgException;
}
