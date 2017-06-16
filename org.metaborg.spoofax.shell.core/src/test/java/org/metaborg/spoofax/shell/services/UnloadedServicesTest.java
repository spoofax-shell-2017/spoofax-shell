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

@RunWith(MockitoJUnitRunner.class)
public class UnloadedServicesTest {

    private IEditorServicesStrategy strategy;

    @Before
    public void setUp() throws Exception {
        strategy = new UnloadedServices();
    }

    @Test
    public void testUnloadedServices() {
        assertNotNull(strategy);
    }

    @Test
    public void testIsLoaded() {
        assertFalse(strategy.isLoaded());
    }

    @Test
    public void testHighlight() {
        System.out.println(strategy.highlight("someSource").getClass());
        assertEquals(FailOrSuccessResult.excepted(mock(ExceptionResult.class)).getClass(),
                strategy.highlight("someSource").getClass());
    }

}
