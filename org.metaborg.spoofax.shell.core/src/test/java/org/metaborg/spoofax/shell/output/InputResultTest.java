package org.metaborg.spoofax.shell.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

/**
 * Test the {@link InputResult} class that wraps an {@link ISpoofaxInputUnit}.
 */
@RunWith(MockitoJUnitRunner.class)
public class InputResultTest {
    private static final String ACTUAL = "actual";

    // Constructor mocks
    @Mock private IStrategoCommon common;
    @Mock private ISpoofaxInputUnit unit;
    @Mock private FileObject source;

    private InputResult inputResult;

    /**
     * Set up mocks used in the test case.
     */
    @Before
    public void setup() {
        when(common.toString(any())).thenReturn(ACTUAL);
        when(unit.text()).thenReturn(ACTUAL);
        when(unit.source()).thenReturn(source);

        inputResult = new InputResult(common, unit);
    }

    /**
     * Test returning the ast of an {@link InputResult}.
     */
    @Test
    public void testAst() {
        assertEquals(Optional.empty(), inputResult.ast());
    }

    /**
     * Test returning the {@link IContext} of an {@link InputResult}.
     */
    @Test
    public void testContext() {
        assertEquals(Optional.empty(), inputResult.context());
    }

    /**
     * Test returning the list of {@link IMessage} of an {@link InputResult}.
     */
    @Test
    public void testMessages() {
        assertEquals(Lists.newArrayList(), inputResult.messages());
    }

    /**
     * Test returning the source {@link FileObject} of an {@link InputResult}.
     */
    @Test
    public void testSource() {
        assertEquals(source, inputResult.source());
    }

    /**
     * Test returning the {@link StyledText} of an {@link InputResult}.
     */
    @Test
    public void testStyled() {
        assertEquals(new StyledText(ACTUAL), inputResult.styled());
    }

    /**
     * Test validity of the {@link InputResult}.
     */
    @Test
    public void testValid() {
        assertTrue(inputResult.valid());
    }
}
