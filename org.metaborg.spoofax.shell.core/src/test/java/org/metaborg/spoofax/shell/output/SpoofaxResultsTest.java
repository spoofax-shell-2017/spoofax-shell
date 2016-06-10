package org.metaborg.spoofax.shell.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private AbstractSpoofaxResult<?> abstractResult;
    private List<IMessage> expectedMessages;
    private Optional<IStrategoTerm> expectedAst;
    private Optional<IContext> expectedContext;
    private FileObject expectedSource;
    private boolean expectedValid;
    private String expectedInvalidString;
    private String expectedValidString;

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
     * @param validString
     *            The expected string when the result is valid.
     * @param invalidString
     *            The expected string when the result is invalid.
     */
    // CHECKSTYLE.OFF: ParameterNumber
    public SpoofaxResultsTest(AbstractSpoofaxResult<?> abstractResult, List<IMessage> messages,
                              @Nullable IStrategoTerm ast, @Nullable IContext context,
                              FileObject source, boolean isValid, String validString,
                              String invalidString) {
        this.abstractResult = abstractResult;
        this.expectedMessages = messages;
        this.expectedValidString = validString;
        this.expectedInvalidString = invalidString;
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

        ISpoofaxInputUnit mockInput = mockInput(source);
        ISpoofaxParseUnit mockParse = mockParse(source, ast, messages, mockInput);
        ISpoofaxAnalyzeUnit mockAnalyze = mockAnalyze(source, ast, context, messages, mockParse);
        ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> mockATransform =
            mockATransform(source, ast, context, messages, mockAnalyze);
        ISpoofaxTransformUnit<ISpoofaxParseUnit> mockPTransform =
            mockPTransform(source, ast, context, messages, mockParse);

        InputResult invalidInputResult = new InputResult(null, mockInput);
        ParseResult invalidParseResult = new ParseResult(common, mockParse);
        AnalyzeResult invalidAnalyzeResult = new AnalyzeResult(common, mockAnalyze);
        TransformResult.Parsed invalidParsedT = new TransformResult.Parsed(common, mockPTransform);
        TransformResult.Analyzed invalidAnalyzedT =
            new TransformResult.Analyzed(common, mockATransform);
        EvaluateResult.Analyzed invalidAnalyzedE =
            new EvaluateResult.Analyzed(common, invalidAnalyzeResult, ast);
        EvaluateResult.Parsed invalidParsedE =
            new EvaluateResult.Parsed(common, invalidParseResult, ast);

        return Arrays
            .asList((Object[]) new Object[][] { { invalidInputResult, Collections.emptyList(), null,
                                                  null, source, true, ACTUAL_SOURCE,
                                                  ACTUAL_SOURCE },
                                                { invalidParseResult, messages, ast, null, source,
                                                  false, ACTUAL_TO_STRING, messagesString },
                                                { invalidAnalyzeResult, messages, ast, context,
                                                  source, false, ACTUAL_TO_STRING, messagesString },
                                                { invalidParsedT, messages, ast, context, source,
                                                  false, ACTUAL_TO_STRING, messagesString },
                                                { invalidAnalyzedT, messages, ast, context, source,
                                                  false, ACTUAL_TO_STRING, messagesString },
                                                { invalidAnalyzedE, messages, ast, context, source,
                                                  false, ACTUAL_TO_STRING, ACTUAL_TO_STRING },
                                                { invalidParsedE, messages, ast, null, source,
                                                  false, ACTUAL_TO_STRING, ACTUAL_TO_STRING } });
    }
    // CHECKSTYLE.ON: MethodLength

    private static ISpoofaxInputUnit mockInput(FileObject source) {
        ISpoofaxInputUnit unit = (ISpoofaxInputUnit) mockUnit(ISpoofaxInputUnit.class, source);
        when(unit.text()).thenReturn(ACTUAL_SOURCE);
        return unit;
    }

    private static ISpoofaxParseUnit mockParse(FileObject source, IStrategoTerm ast,
                                               List<IMessage> messages,
                                               ISpoofaxInputUnit mockInput) {
        ISpoofaxParseUnit unit = (ISpoofaxParseUnit) mockUnit(ISpoofaxParseUnit.class, source);
        when(unit.ast()).thenReturn(ast);
        when(unit.messages()).thenReturn(messages);
        when(unit.input()).thenReturn(mockInput);
        when(unit.valid()).thenReturn(true, false, true, true, true, false, true, true, true, false,
                                      true, true, true, false, true, true, true, false, true, true,
                                      true, false, true, true);
        when(unit.success()).thenReturn(true, true, false, true, true, false, true, true, false,
                                        true, true, false, true, true, false, true, true, false);
        return unit;
    }

    private static ISpoofaxAnalyzeUnit mockAnalyze(FileObject source, IStrategoTerm ast,
                                                   IContext context, List<IMessage> messages,
                                                   ISpoofaxParseUnit mockParse) {
        ISpoofaxAnalyzeUnit unit =
            (ISpoofaxAnalyzeUnit) mockUnit(ISpoofaxAnalyzeUnit.class, source);
        when(unit.ast()).thenReturn(ast);
        when(unit.context()).thenReturn(context);
        when(unit.messages()).thenReturn(messages);
        when(unit.input()).thenReturn(mockParse);
        when(unit.valid()).thenReturn(true, false, true, false, true, false, true, false, true,
                                      false, true, false, true, false, true, false, true, false);
        return unit;
    }

    @SuppressWarnings("unchecked")
    private static ISpoofaxTransformUnit<ISpoofaxParseUnit>
            mockPTransform(FileObject source, IStrategoTerm ast, IContext context,
                           List<IMessage> messages, ISpoofaxParseUnit mockParse) {
        ISpoofaxTransformUnit<ISpoofaxParseUnit> unit =
            (ISpoofaxTransformUnit<ISpoofaxParseUnit>) mockTransform(source, ast, context,
                                                                     messages);
        when(unit.input()).thenReturn(mockParse);
        return unit;
    }

    @SuppressWarnings("unchecked")
    private static ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>
            mockATransform(FileObject source, IStrategoTerm ast, IContext context,
                           List<IMessage> messages, ISpoofaxAnalyzeUnit mockAnalyze) {
        ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> unit =
            (ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>) mockTransform(source, ast, context,
                                                                       messages);
        when(unit.input()).thenReturn(mockAnalyze);
        return unit;
    }

    private static ISpoofaxTransformUnit<?> mockTransform(FileObject source, IStrategoTerm ast,
                                                          IContext context,
                                                          List<IMessage> messages) {
        ISpoofaxTransformUnit<?> unit =
            (ISpoofaxTransformUnit<?>) mockUnit(ISpoofaxTransformUnit.class, source);
        when(unit.ast()).thenReturn(ast);
        when(unit.context()).thenReturn(context);
        when(unit.messages()).thenReturn(messages);
        when(unit.valid()).thenReturn(true, false, true, false, true, false, true, false, true,
                                      false);
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
        assertEquals(new StyledText(expectedValidString), abstractResult.styled());
        assertEquals(new StyledText(expectedInvalidString), abstractResult.styled());
        assertEquals(new StyledText(expectedValidString), abstractResult.styled());
        assertEquals(new StyledText(expectedInvalidString), abstractResult.styled());
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
        assertTrue(abstractResult.valid());
        assertEquals(expectedValid, abstractResult.valid());
        assertTrue(abstractResult.valid());
        assertEquals(expectedValid, abstractResult.valid());
    }
}
