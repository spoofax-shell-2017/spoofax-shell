package org.metaborg.spoofax.shell.client.eclipse;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The Activator class controls the plugin's life cycle.
 */
public class Activator extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.metaborg.spoofax.shell.eclipse";
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
     * Returns an {@link ImageDescriptor} for the image file at the given plugin-relative path.
     *
     * @param path
     *            The plugin-relative path to the image.
     * @return The {@link ImageDescriptor} for the image at the given path.
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
