package org.metaborg.spoofax.shell.client.console.impl.hooks;

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.output.StyledText;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests {@link ConsoleMessageHook}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsoleMessageHookTest {
    @Mock
    private StyledText text;

    private ConsoleMessageHook hook;
    private IDisplay display;

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {
        this.display = mock(IDisplay.class, RETURNS_MOCKS);
        this.hook = new ConsoleMessageHook(display);
    }

    /**
     * Tests if {@link IDisplay#displayMessage(StyledText)} is called correctly.
     */
    @Test
    public void testAccept() {
        hook.accept(text);
        verify(display, times(1)).displayMessage(text);
    }
}
