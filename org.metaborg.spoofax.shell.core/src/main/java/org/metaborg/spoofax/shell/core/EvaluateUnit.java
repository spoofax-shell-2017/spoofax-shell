package org.metaborg.spoofax.shell.core;

import org.metaborg.core.context.IContext;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.unit.Unit;
import org.metaborg.spoofax.core.unit.UnitWrapper;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * The result of an evaluation with DynSem.
 *
 * @param <U> The type of the input unit.
 */
public class EvaluateUnit<U extends IUnit> extends UnitWrapper {
    private IStrategoTerm ast;
    private IContext context;
    private U inputUnit;

    /**
     * @param unit A unit.
     * @param input The input that lead to this {@link EvaluateUnit}.
     * @param ast The result AST.
     * @param context The context.
     */
    public EvaluateUnit(Unit unit, IStrategoTerm ast, IContext context, U input) {
        super(unit);
        this.ast = ast;
        this.context = context;
        this.inputUnit = input;
    }

    /**
     * @return The result AST.
     */
    public IStrategoTerm ast() {
        return ast;
    }

    /**
     * @return The context.
     */
    public IContext context() {
        return context;
    }

    /**
     * @return The input that lead to this {@link EvaluateUnit}.
     */
    public U input() {
        return inputUnit;
    }
}
