package org.metaborg.spoofax.shell.output;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Tests all subclasses of {@link AbstractSpoofaxResult} using a {@link Parameterized parameterized
 * test}.
 */
@RunWith(Parameterized.class)
public class SpoofaxResultsTest {

    private static final String ACTUAL_TO_STRING = "actual";
    private static final String ACTUAL_SOURCE = "actual source";

    private final AbstractSpoofaxResult<?> abstractResult;
    private final List<IMessage> expectedMessages;
    private final Optional<IStrategoTerm> expectedAst;
    private final Optional<IContext> expectedContext;
    private final FileObject expectedSource;
    private final boolean expectedValid;
    private final String expectedString;

    /**
     * Instantiate parameters.
     *
     * @param abstractResult
     *            The result class being tested.
     * @param messages
     *            Expected messages.
     * @param ast
     *            Expected AST.
     * @param context
     *            Expected context.
     * @param source
     *            Expected source.
     * @param isValid
     *            Whether we should expect the result to be valid.
     * @param expectedString
     *            The expected string when calling the styled() method.
     */
    // CHECKSTYLE.OFF: ParameterNumber
    public SpoofaxResultsTest(AbstractSpoofaxResult<?> abstractResult, List<IMessage> messages,
                              @Nullable IStrategoTerm ast, @Nullable IContext context,
                              FileObject source, boolean isValid, String expectedString) {
        this.abstractResult = abstractResult;
        this.expectedMessages = messages;
        this.expectedString = expectedString;
        this.expectedAst = Optional.ofNullable(ast);
        this.expectedContext = Optional.ofNullable(context);
        this.expectedSource = source;
        this.expectedValid = isValid;
    }
    // CHECKSTYLE.ON: ParameterNumber

    /**
     * @return All of the parameters to run each test against.
     */
    // CHECKSTYLE.OFF: MethodLength
    //@formatter:off
    @Parameters(name = "{index}: {0}")
    public static List<Object> resultParameters() {
        IStrategoCommon common = mock(IStrategoCommon.class);
        IMessage message1 = mock(IMessage.class), message2 = mock(IMessage.class);
        List<IMessage> messages = Arrays.asList(message1, message2);
        String messagesString = messages.toString();
        IStrategoTerm ast = mock(IStrategoTerm.class);
        IContext context = mock(IContext.class);
        FileObject source = mock(FileObject.class);

        when(common.toString(any())).thenReturn(ACTUAL_TO_STRING);

        List<Object> params = new ArrayList<>();

        ISpoofaxInputUnit mockInput = mockInput(source);
        InputResult inputResult = new InputResult(null, mockInput);
        params.add(new Object[] { inputResult, Collections.emptyList(), null, null, source, true,
                                  ACTUAL_SOURCE });

        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeParseMock =
            (valid, success) -> new ParseResult(common, mockParse(source, ast, messages, mockInput,
                                                                  valid, success));
        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeNoASTParseMock =
            (valid, success) -> new ParseResult(common, mockParse(source, null, messages, mockInput,
                                                                  valid, success));
        ISpoofaxParseUnit mockParse = (ISpoofaxParseUnit) makeParseMock.apply(true, true).unit();
        params.addAll(makeParams(makeParseMock, makeNoASTParseMock, messages, ast, null, source,
                                 messagesString));

        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeAnalyzeMock =
            (valid, success) -> new AnalyzeResult(common,
                                                  mockAnalyze(source, ast, context, messages,
                                                              mockParse, valid, success));
        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeNoASTAnalyzeMock =
            (valid, success) -> new AnalyzeResult(common,
                                                  mockAnalyze(source, null, context, messages,
                                                              mockParse, valid, success));
        ISpoofaxAnalyzeUnit mockAnalyze = (ISpoofaxAnalyzeUnit) makeAnalyzeMock.apply(true, true)
                .unit();
        params.addAll(makeParams(makeAnalyzeMock, makeNoASTAnalyzeMock, messages, ast, context,
                                 source, messagesString));

        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeTransPMock =
            (valid, success) -> new TransformResult.Parsed(common,
                                                           mockPTransform(source, ast, context,
                                                                          messages, mockParse,
                                                                          valid, success));
        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeNoASTTransPMock =
            (valid, success) -> new TransformResult.Parsed(common,
                                                           mockPTransform(source, null, context,
                                                                          messages, mockParse,
                                                                          valid, success));
        params.addAll(makeParams(makeTransPMock, makeNoASTTransPMock, messages, ast, context,
                                 source, messagesString));

        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeTransAMock =
            (valid, success) -> new TransformResult.Analyzed(common,
                                                             mockATransform(source, ast, context,
                                                                            messages, mockAnalyze,
                                                                            valid, success));
        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeNoASTTransAMock =
            (valid, success) -> new TransformResult.Analyzed(common,
                                                             mockATransform(source, null, context,
                                                                            messages, mockAnalyze,
                                                                            valid, success));
        params.addAll(makeParams(makeTransAMock, makeNoASTTransAMock, messages, ast, context,
                                 source, messagesString));

        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeEvalAMock =
            (valid, success) -> new EvaluateResult.Analyzed(common, (AnalyzeResult) makeAnalyzeMock
                .apply(valid, success), ast);
        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeNoASTEvalAMock =
            (valid, success) -> new EvaluateResult.Analyzed(common,
                                                            (AnalyzeResult) makeNoASTAnalyzeMock
                .apply(valid, success), null);
        params.addAll(makeParams(makeEvalAMock, makeNoASTEvalAMock, messages, ast, context,
                                 source, messagesString));

        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeEvalPMock =
            (valid, success) -> new EvaluateResult.Parsed(common, (ParseResult) makeParseMock
                .apply(valid, success), ast);
        BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeNoASTEvalPMock =
            (valid, success) -> new EvaluateResult.Parsed(common, (ParseResult) makeNoASTParseMock
                .apply(valid, success), null);
        params.addAll(makeParams(makeEvalPMock, makeNoASTEvalPMock, messages, ast, null,
                                 source, messagesString));

        return params;
    }
    //@formatter:on

    private static Collection<? extends Object>
            makeParams(BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeMock,
                       BiFunction<Boolean, Boolean, ISpoofaxResult<?>> makeNoASTMock,
                       List<IMessage> messages, IStrategoTerm ast, IContext context,
                       FileObject source, String messagesString) {
        return Arrays
            .asList((Object[]) new Object[][] { { makeMock.apply(true, true), messages, ast,
                                                  context, source, true, ACTUAL_TO_STRING },
                                                { makeMock.apply(false, false), messages, ast,
                                                  context, source, false, messagesString },
                                                { makeMock.apply(false, true), messages, ast,
                                                  context, source, false, messagesString },
                                                { makeMock.apply(true, false), messages, ast,
                                                  context, source, false, messagesString },
                                                { makeNoASTMock.apply(true, true), messages, null,
                                                  context, source, true, messagesString },
                                                { makeNoASTMock.apply(false, false), messages, null,
                                                  context, source, false, messagesString },
                                                { makeNoASTMock.apply(false, true), messages, null,
                                                  context, source, false, messagesString },
                                                { makeNoASTMock.apply(true, false), messages, null,
                                                  context, source, false, messagesString } });
    }
    // CHECKSTYLE.ON: MethodLength

    private static ISpoofaxInputUnit mockInput(FileObject source) {
        ISpoofaxInputUnit unit = (ISpoofaxInputUnit) mockUnit(ISpoofaxInputUnit.class, source);
        when(unit.text()).thenReturn(ACTUAL_SOURCE);
        return unit;
    }

    private static ISpoofaxParseUnit mockParse(FileObject source, IStrategoTerm ast,
                                               List<IMessage> messages, ISpoofaxInputUnit mockInput,
                                               boolean valid, boolean success) {
        ISpoofaxParseUnit unit = (ISpoofaxParseUnit) mockUnit(ISpoofaxParseUnit.class, source);
        when(unit.ast()).thenReturn(ast);
        when(unit.messages()).thenReturn(messages);
        when(unit.input()).thenReturn(mockInput);
        when(unit.valid()).thenReturn(valid);
        when(unit.success()).thenReturn(success);
        return unit;
    }

    private static ISpoofaxAnalyzeUnit mockAnalyze(FileObject source, IStrategoTerm ast,
                                                   IContext context, List<IMessage> messages,
                                                   ISpoofaxParseUnit mockParse, boolean valid,
                                                   boolean success) {
        ISpoofaxAnalyzeUnit unit =
            (ISpoofaxAnalyzeUnit) mockUnit(ISpoofaxAnalyzeUnit.class, source);
        when(unit.ast()).thenReturn(ast);
        when(unit.context()).thenReturn(context);
        when(unit.messages()).thenReturn(messages);
        when(unit.input()).thenReturn(mockParse);
        when(unit.valid()).thenReturn(valid);
        when(unit.success()).thenReturn(success);
        return unit;
    }

    @SuppressWarnings("unchecked")
    private static ISpoofaxTransformUnit<ISpoofaxParseUnit>
            mockPTransform(FileObject source, IStrategoTerm ast, IContext context,
                           List<IMessage> messages, ISpoofaxParseUnit mockParse, boolean valid,
                           boolean success) {
        ISpoofaxTransformUnit<ISpoofaxParseUnit> unit =
            (ISpoofaxTransformUnit<ISpoofaxParseUnit>) mockTransform(source, ast, context, messages,
                                                                     valid, success);
        when(unit.input()).thenReturn(mockParse);
        return unit;
    }

    @SuppressWarnings("unchecked")
    private static ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>
            mockATransform(FileObject source, IStrategoTerm ast, IContext context,
                           List<IMessage> messages, ISpoofaxAnalyzeUnit mockAnalyze, boolean valid,
                           boolean success) {
        ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> unit =
            (ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>) mockTransform(source, ast, context,
                                                                       messages, valid, success);
        when(unit.input()).thenReturn(mockAnalyze);
        return unit;
    }

    private static ISpoofaxTransformUnit<?> mockTransform(FileObject source, IStrategoTerm ast,
                                                          IContext context, List<IMessage> messages,
                                                          boolean valid, boolean success) {
        ISpoofaxTransformUnit<?> unit =
            (ISpoofaxTransformUnit<?>) mockUnit(ISpoofaxTransformUnit.class, source);
        when(unit.ast()).thenReturn(ast);
        when(unit.context()).thenReturn(context);
        when(unit.messages()).thenReturn(messages);
        when(unit.valid()).thenReturn(valid);
        when(unit.success()).thenReturn(success);
        return unit;
    }

    private static IUnit mockUnit(Class<? extends IUnit> clazz, FileObject source) {
        IUnit unit = mock(clazz);
        when(unit.source()).thenReturn(source);
        return unit;
    }

    /**
     * Test returning the ast.
     */
    @Test
    public void testAst() {
        assertEquals(expectedAst, abstractResult.ast());
    }

    /**
     * Test returning the {@link IContext}.
     */
    @Test
    public void testContext() {
        assertEquals(expectedContext, abstractResult.context());
    }

    /**
     * Test returning the list of {@link IMessage}.
     */
    @Test
    public void testMessages() {
        assertEquals(expectedMessages, abstractResult.messages());
    }

    /**
     * Test returning the {@link StyledText}, both for a valid and invalid result.
     */
    @Test
    public void testValidOrInvalidStyled() {
        assertEquals(new StyledText(expectedString), abstractResult.styled());
    }

    /**
     * Test returning the source {@link FileObject}.
     */
    @Test
    public void testSource() {
        assertEquals(expectedSource, abstractResult.source());
    }

    /**
     * Test getting the original source text from any result.
     */
    @Test
    public void testSourceText() {
        assertEquals(ACTUAL_SOURCE, abstractResult.sourceText());
    }

    /**
     * Tests whether the result is valid or invalid as expected.
     */
    @Test
    public void testValidOrInvalid() {
        assertEquals(expectedValid, abstractResult.valid());
    }
}
