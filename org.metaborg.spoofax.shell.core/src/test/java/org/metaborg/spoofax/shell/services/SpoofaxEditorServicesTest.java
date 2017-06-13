package org.metaborg.spoofax.shell.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyleResult;
import org.metaborg.spoofax.shell.services.IEditorServices;
import org.metaborg.spoofax.shell.services.IEditorServicesStrategy;
import org.metaborg.spoofax.shell.services.LoadedServices;
import org.metaborg.spoofax.shell.services.SpoofaxEditorServices;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test the default implementation of {@link IEditorServices}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpoofaxEditorServicesTest {

    private SpoofaxEditorServices services;

    @Mock
    private IEditorServicesStrategy strategy;
    @Mock
    private FailOrSuccessResult<StyleResult, IResult> highlightResult;

    @Before
    public void setUp() {
        services = new SpoofaxEditorServices(strategy);
    }

    /**
     * Test that Services are constructed with 'unloaded' behaviour.
     */
    @Test
    public void testSpoofaxEditorServices() {
        assertEquals(strategy, services.strategy);
    }

    /**
     * Tests that the Services' strategy is correctly replaced when a language
     * is loaded.
     */
    @Test
    public void testLoad() {
        FunctionComposer composer = mock(FunctionComposer.class);
        services.load(composer);
        assertEquals(LoadedServices.class, services.strategy.getClass());
    }

    /**
     * Tests the strategy delegation of
     * {@link IEditorServices#highlight(String)}.
     */
    @Test
    public void testHighlight() {
        final String testSource = "test";
        services.highlight(testSource);
        verify(strategy).highlight(testSource);
    }

    /**
     * Tests the strategy delegation of
     * {@link IEditorServices#isLoaded()}.
     */
    @Test
    public void testIsLoaded() {
        services.isLoaded();
        verify(strategy).isLoaded();
    }

}
