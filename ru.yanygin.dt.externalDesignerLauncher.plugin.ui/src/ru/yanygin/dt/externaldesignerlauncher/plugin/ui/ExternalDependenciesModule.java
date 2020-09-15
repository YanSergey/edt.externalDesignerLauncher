package ru.yanygin.dt.externaldesignerlauncher.plugin.ui;

import org.eclipse.core.runtime.Plugin;

import com._1c.g5.v8.dt.platform.services.core.infobases.IInfobaseAccessManager;
import com._1c.g5.v8.dt.platform.services.core.infobases.IInfobaseAssociationManager;
import com._1c.g5.v8.dt.platform.services.core.runtimes.environments.IResolvableRuntimeInstallationManager;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.IRuntimeComponentManager;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.wiring.AbstractServiceAwareModule;

public class ExternalDependenciesModule extends AbstractServiceAwareModule {
	
	public ExternalDependenciesModule(Plugin bundle) {
		super(bundle);
	}
	
	@Override
	protected void doConfigure() {
		bind(IRuntimeComponentManager.class).toService();
		bind(IResolvableRuntimeInstallationManager.class).toService();
		bind(IInfobaseAccessManager.class).toService();
		bind(IInfobaseAssociationManager.class).toService();
		bind(IRuntimeVersionSupport.class).toService();
	}
	
}
