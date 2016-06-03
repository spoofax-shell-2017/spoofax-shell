package org.metaborg.spoofax.shell.client.console.impl.hooks;

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests {@link ConsoleResultHook}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsoleResultHookTest {
    @Mock
    private ISpoofaxResult<?> result;

    private ConsoleResultHook hook;
    private IDisplay display;

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {
        this.display = mock(IDisplay.class, RETURNS_MOCKS);
        this.hook = new ConsoleResultHook(display);
    }

    /**
     * Tests if {@link IDisplay#displayResult(ISpoofaxResult)} is called correctly.
     */
    @Test
    public void testAccept() {
        hook.accept(result);
        verify(display, times(1)).displayResult(result);
    }
}