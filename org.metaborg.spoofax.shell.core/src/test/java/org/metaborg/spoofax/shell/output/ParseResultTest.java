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
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Test the {@link ParseResult} class that wraps an {@link ISpoofaxParseUnit}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParseResultTest {
    private static final String ACTUAL = "actual";

    // Constructor mocks
    @Mock private IStrategoCommon common;
    @Mock private ISpoofaxParseUnit unit;
    @Mock private IStrategoTerm ast;
    @Mock private IMessage message1, message2;

    private List<IMessage> messages;
    private ParseResult parseResult;

    /**
     * Set up mocks used in the test case.
     */
    @Before
    public void setup() {
        messages = Arrays.asList(message1, message2);
        when(common.toString(any())).thenReturn(ACTUAL);
        when(unit.ast()).thenReturn(ast);
        when(unit.messages()).thenReturn(messages);
        when(unit.valid()).thenReturn(true);

        parseResult = new ParseResult(common, unit);
    }

    /**
     * Test returning the ast of a {@link ParseResult}.
     */
    @Test
    public void testAst() {
        assertEquals(Optional.of(ast), parseResult.ast());
    }

    /**
     * Test returning the {@link IContext} of a {@link ParseResult}.
     */
    @Test
    public void testContext() {
        assertEquals(Optional.empty(), parseResult.context());
    }

    /**
     * Test returning the list of {@link IMessage} of a {@link ParseResult}.
     */
    @Test
    public void testMessages() {
        assertEquals(messages, parseResult.messages());
    }

    /**
     * Test returning the {@link StyledText} of a {@link ParseResult}.
     */
    @Test
    public void testStyled() {
        assertEquals(new StyledText(ACTUAL), parseResult.styled());
    }

    /**
     * Test validity of a {@link ParseResult} that wraps a valid {@link ISpoofaxParseUnit}.
     */
    @Test
    public void testValid() {
        when(unit.valid()).thenReturn(true);

        assertTrue(parseResult.valid());
    }

    /**
     * Test validity of a {@link ParseResult} that wraps an invalid {@link ISpoofaxParseUnit}.
     */
    @Test
    public void testInvalid() {
        when(unit.valid()).thenReturn(false);

        assertFalse(parseResult.valid());
    }
}
