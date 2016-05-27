package org.metaborg.spoofax.shell.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Test the {@link AnalyzeResult} class that wraps an {@link ISpoofaxAnalyzeUnit}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalyzeResultTest {
    private static final String ACTUAL = "actual";

    // Constructor mocks
    @Mock private IStrategoCommon common;
    @Mock private ISpoofaxAnalyzeUnit unit;
    @Mock private IStrategoTerm ast;
    @Mock private IContext context;
    @Mock private IMessage message1, message2;

    private List<IMessage> messages;
    private AnalyzeResult analyzeResult;

    /**
     * Set up mocks used in the test case.
     */
    @Before
    public void setup() {
        messages = Arrays.asList(message1, message2);
        when(common.toString(any())).thenReturn(ACTUAL);
        when(unit.ast()).thenReturn(ast);
        when(unit.context()).thenReturn(context);
        when(unit.messages()).thenReturn(messages);
        analyzeResult = new AnalyzeResult(common, unit);
    }

    /**
     * Test returning the ast of an {@link AnalyzeResult}.
     */
    @Test
    public void testAst() {
        assertEquals(Optional.of(ast), analyzeResult.ast());
    }

    /**
     * Test returning the {@link IContext} of an {@link AnalyzeResult}.
     */
    @Test
    public void testContext() {
        assertEquals(Optional.of(context), analyzeResult.context());
    }

    /**
     * Test returning the list of {@link IMessage} of an {@link AnalyzeResult}.
     */
    @Test
    public void testMessages() {
        assertEquals(messages, analyzeResult.messages());
    }

    /**
     * Test returning the {@link StyledText} of a valid {@link AnalyzeResult}.
     */
    @Test
    public void testValidStyled() {
        when(unit.valid()).thenReturn(true);

        assertEquals(new StyledText(ACTUAL), analyzeResult.styled());
    }

    /**
     * Test returning the {@link StyledText} of an invalid {@link AnalyzeResult}.
     */
    @Test
    public void testInvalidStyled() {
        when(unit.valid()).thenReturn(false);

        assertEquals(new StyledText(messages.toString()), analyzeResult.styled());
    }

    /**
     * Test validity of a {@link AnalyzeResult} that wraps a valid {@link ISpoofaxAnalyzeUnit}.
     */
    @Test
    public void testValid() {
        when(unit.valid()).thenReturn(true);

        assertTrue(analyzeResult.valid());
    }

    /**
     * Test validity of a {@link AnalyzeResult} that wraps an invalid {@link ISpoofaxAnalyzeUnit}.
     */
    @Test
    public void testInvalid() {
        when(unit.valid()).thenReturn(false);

        assertFalse(analyzeResult.valid());
    }
}

