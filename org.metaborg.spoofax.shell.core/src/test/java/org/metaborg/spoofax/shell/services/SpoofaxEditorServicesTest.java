package org.metaborg.spoofax.shell.services;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyleResult;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test the default implementation of {@link IEditorServices}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpoofaxEditorServicesTest {

    private SpoofaxEditorServices services;

    @Mock
    private FunctionComposer composer;
    @Mock
    private IServicesStrategyFactory factory;
    @Mock
    private UnloadedServices unloaded;
    @Mock
    private LoadedServices loaded;
    @Mock
    private FailOrSuccessResult<StyleResult, IResult> highlightResult;

    /**
     * Set up instance of the class under test.
     */
    @Before
    public void setUp() {
        when(factory.createUnloadedStrategy()).thenReturn(unloaded);
        when(factory.createLoadedStrategy(any())).thenReturn(loaded);
        services = new SpoofaxEditorServices(factory);
    }

    /**
     * Test that Services are constructed with 'unloaded' behaviour.
     */
    @Test
    public void testSpoofaxEditorServices() {
        verify(factory).createUnloadedStrategy();
    }

    /**
     * Tests that the Services' strategy is correctly replaced when a language
     * is loaded.
     */
    @Test
    public void testLoad() {
        services.load(composer);
        verify(factory).createLoadedStrategy(composer);
        services.isLoaded();
        verify(loaded).isLoaded();
    }

    /**
     * Tests the strategy delegation of
     * {@link IEditorServices#highlight(String)} to the unloaded strategy.
     */
    @Test
    public void testHighlightUnloaded() {
        final String testSource = "test";
        services.highlight(testSource);
        verifyZeroInteractions(loaded);
        verify(unloaded).highlight(testSource);
    }

    /**
     * Tests the strategy delegation of
     * {@link IEditorServices#highlight(String)} to the loaded strategy.
     */
    @Test
    public void testHighlightLoaded() {
        final String testSource = "test";
        services.load(composer);
        services.highlight(testSource);
        verifyZeroInteractions(unloaded);
        verify(loaded).highlight(testSource);
    }

    /**
     * Tests the strategy delegation of {@link IEditorServices#isLoaded()} as well as the unloaded
     * return value (false).
     */
    @Test
    public void testIsLoadedFalse() {
        services.isLoaded();
        verify(unloaded).isLoaded();
    }

    /**
     * Tests the strategy delegation of {@link IEditorServices#isLoaded()} as well as the loaded
     * return value (true).
     */
    @Test
    public void testIsLoadedTrue() {
        FunctionComposer composer = mock(FunctionComposer.class);
        services.load(composer);
        services.isLoaded();
        verifyZeroInteractions(unloaded);
        verify(loaded).isLoaded();
    }

}
