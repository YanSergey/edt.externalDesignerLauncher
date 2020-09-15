package ru.yanygin.dt.externaldesignerlauncher.plugin.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "ru.yanygin.dt.externalDesignerLauncher.plugin.ui"; //$NON-NLS-1$
	
	// The shared instance
	private static Activator plugin;
	private Injector injector;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public synchronized Injector getInjector() {
		if (injector == null)
			injector = createInjector();
		
		return injector;
	}
	
	private Injector createInjector() {
		try {
			return Guice.createInjector(new ExternalDependenciesModule(this));
		} catch (Exception e) {
			log(createErrorStatus("Failed to create injector for " //$NON-NLS-1$
					+ getBundle().getSymbolicName(), e));
			throw new RuntimeException("Failed to create injector for " //$NON-NLS-1$
					+ getBundle().getSymbolicName(), e);
		}
	}
	
	public static IStatus createInfoStatus(String message) {
		return new Status(IStatus.INFO, PLUGIN_ID, 0, message, (Throwable) null);
	}
	
	public static IStatus createErrorStatus(String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, (Throwable) null);
	}
	
	public static IStatus createErrorStatus(Throwable throwable) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, throwable.getLocalizedMessage(), throwable);
	}
	
	public static IStatus createErrorStatus(String message, Throwable throwable) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
	}
	
	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}
	
}
