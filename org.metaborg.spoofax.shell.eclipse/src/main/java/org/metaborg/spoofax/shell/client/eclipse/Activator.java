package org.metaborg.spoofax.shell.client.eclipse;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The Activator class controls the plugin's life cycle. It is instantiated by Eclipse
 * automatically. See {@link org.eclipse.core.runtime.Plugin#Plugin()} and
 * {@link AbstractUIPlugin#AbstractUIPlugin()} for more information.
 *
 * Currently its use is to keep track of certain objects that need to be available for the plugin.
 * In the future, it can be used as entry points to the REPL for commands and actions.
 */
public class Activator extends AbstractUIPlugin {
    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Activator.plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        Activator.plugin = null;
        super.stop(context);
    }

    /**
     * Return the shared Activator instance.
     *
     * @return The shared Activator instance.
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Return the plugin's ID.
     *
     * @return The plugin's ID.
     */
    public static String getPluginID() {
        return getDefault().getBundle().getSymbolicName();
    }

    /**
     * Return an {@link ImageDescriptor} for the image file at the given plugin-relative path.
     *
     * @param path
     *            The plugin-relative path to the image.
     * @return The {@link ImageDescriptor} for the image at the given path.
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(getPluginID(), path);
    }
}
