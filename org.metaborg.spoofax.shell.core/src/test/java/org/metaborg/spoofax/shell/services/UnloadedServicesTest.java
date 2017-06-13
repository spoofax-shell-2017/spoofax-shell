package org.metaborg.spoofax.shell.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.services.IEditorServicesStrategy;
import org.metaborg.spoofax.shell.services.UnloadedServices;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link UnloadedServices}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UnloadedServicesTest {

    private IEditorServicesStrategy strategy;

    /**
     * Set up instance of the class under test.
     */
    @Before
    public void setUp() {
        strategy = new UnloadedServices();
    }

    /**
     * Test correct instantiation.
     */
    @Test
    public void testUnloadedServices() {
        assertNotNull(strategy);
    }

    /**
     * Test correct flag for the loaded state.
     */
    @Test
    public void testIsLoaded() {
        assertFalse(strategy.isLoaded());
    }

    /**
     * Test that highlighting returns an exception.
     */
    @Test
    public void testHighlight() {
        System.out.println(strategy.highlight("someSource").getClass());
        assertEquals(FailOrSuccessResult.excepted(mock(ExceptionResult.class)).getClass(),
                strategy.highlight("someSource").getClass());
    }

}
