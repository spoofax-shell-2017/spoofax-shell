package org.metaborg.spoofax.shell.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.spoofax.shell.functions.FailableFunction;
import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyleResult;
import org.metaborg.spoofax.shell.services.IEditorServicesStrategy;
import org.metaborg.spoofax.shell.services.LoadedServices;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoadedServicesTest {

    private IEditorServicesStrategy strategy;

    @Mock
    private FunctionComposer composer;
    @Mock
    private FailableFunction<String, StyleResult, IResult> highlightFunction;

    /**
     * Initialize all required function composition stubs and a fresh instance
     * of the class under test.
     */
    @Before
    public void setUp() throws Exception {
        this.strategy = new LoadedServices(composer);
        when(composer.pStyleFunction()).thenReturn(highlightFunction);
    }

    /**
     * Assert that the constructor works.
     */
    @Test
    public void testLoadedServices() {
        assertNotNull(strategy);
    }

    /**
     * Assert that the LoadedServices return <code>true</code>.
     */
    @Test
    public void testIsLoaded() {
        assertTrue(strategy.isLoaded());
    }

    /**
     * Test that the right stubbed function composition is called and that it is
     * applied with the right source.
     */
    @Test
    public void testHighlight() {
        final String source = "SomeSource";
        strategy.highlight(source);
        verify(highlightFunction).apply(source);
    }

}
