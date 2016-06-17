package org.metaborg.spoofax.shell.output;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Tests all subclasses of {@link AbstractSpoofaxResult} using a {@link Parameterized
 * parameterized test}.
 */
@RunWith(Parameterized.class)
public class SpoofaxResultsTest {

    protected static final String ACTUAL_TO_STRING = "actual";
    protected static final String ACTUAL_SOURCE = "actual source";
    protected final AbstractSpoofaxResult<?> abstractResult;
    protected final List<IMessage> expectedMessages;
    protected final Optional<IContext> expectedContext;
    protected final FileObject expectedSource;
    protected final boolean expectedValid;
    protected final String expectedString;

    /**
     * Instantiate parameters.
     *
     * @param abstractResult
     *            The result class being tested.
     * @param messages
     *            Expected messages.
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
                              @Nullable IContext context, FileObject source, boolean isValid,
                              String expectedString) {
        this.abstractResult = abstractResult;
        this.expectedMessages = messages;
        this.expectedString = expectedString;
        this.expectedContext = Optional.ofNullable(context);
        this.expectedSource = source;
        this.expectedValid = isValid;
    }
    // CHECKSTYLE.ON: ParameterNumber

    /**
     * @return All of the parameters to run each test against.
     */
    @Parameters(name = "{index}: {0}")
    public static List<Object> resultParameters() {
        IMessage message1 = mock(IMessage.class), message2 = mock(IMessage.class);
        List<IMessage> messages = Arrays.asList(message1, message2);
        String messagesString = messages.toString();
        IStrategoTerm ast = mock(IStrategoTerm.class);
        IContext context = mock(IContext.class);
        FileObject source = mock(FileObject.class);

        return addParams(messages, messagesString, ast, context, source);
    }

    /**
     * Return list parameters.
     * @param messages messages
     * @param messagesString messagesString
     * @param ast ast
     * @param context context
     * @param source source
     * @return parameters
     */
    protected static List<Object> addParams(List<IMessage> messages,
                                            String messagesString, IStrategoTerm ast,
                                            IContext context, FileObject source) {
        List<Object> params = new ArrayList<>();

        ISpoofaxInputUnit mockInput = mockInput(source);
        InputResult inputResult = new InputResult(mockInput);
        params.add(new Object[] { inputResult, Collections.emptyList(), null, source, true,
                                  ACTUAL_SOURCE });
        return params;
    }

    /**
     * mock input unit.
     * @param source source.
     * @return the input unit.
     */
    protected static ISpoofaxInputUnit mockInput(FileObject source) {
        ISpoofaxInputUnit unit = (ISpoofaxInputUnit) mockUnit(ISpoofaxInputUnit.class, source);
        when(unit.text()).thenReturn(ACTUAL_SOURCE);
        return unit;
    }

    /**
     * Mock any unit.
     * @param clazz unit class
     * @param source source
     * @return mock unit.
     */
    protected static IUnit mockUnit(Class<? extends IUnit> clazz, FileObject source) {
        IUnit unit = mock(clazz);
        when(unit.source()).thenReturn(source);
        return unit;
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