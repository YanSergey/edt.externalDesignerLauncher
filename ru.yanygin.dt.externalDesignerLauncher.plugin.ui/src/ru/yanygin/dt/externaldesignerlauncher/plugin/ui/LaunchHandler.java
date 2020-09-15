package ru.yanygin.dt.externaldesignerlauncher.plugin.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com._1c.g5.v8.dt.common.Pair;
import com._1c.g5.v8.dt.platform.services.core.infobases.IInfobaseAccessManager;
import com._1c.g5.v8.dt.platform.services.core.infobases.IInfobaseAccessSettings;
import com._1c.g5.v8.dt.platform.services.core.infobases.IInfobaseAssociation;
import com._1c.g5.v8.dt.platform.services.core.infobases.IInfobaseAssociationManager;
import com._1c.g5.v8.dt.platform.services.core.runtimes.RuntimeInstallations;
import com._1c.g5.v8.dt.platform.services.core.runtimes.environments.IResolvableRuntimeInstallation;
import com._1c.g5.v8.dt.platform.services.core.runtimes.environments.IResolvableRuntimeInstallationManager;
import com._1c.g5.v8.dt.platform.services.core.runtimes.environments.MatchingRuntimeNotFound;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.IDesignerSessionThickClientLauncher;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.ILaunchableRuntimeComponent;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.IRuntimeComponentManager;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.IRuntimeComponentTypes;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.IThickClientLauncher;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.RuntimeExecutionArguments;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.RuntimeExecutionException;
import com._1c.g5.v8.dt.platform.services.model.InfobaseAccess;
import com._1c.g5.v8.dt.platform.services.model.InfobaseReference;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.inject.Inject;
import com.google.common.base.Strings;

public class LaunchHandler extends AbstractHandler {
	
	private IStatus launchStatus;
	
	@Inject
	IRuntimeComponentManager runtimeComponentManager;
	@Inject
	IRuntimeVersionSupport runtimeVersionSupport;
	@Inject
	IResolvableRuntimeInstallationManager resolvableRuntimeInstallationManager;
	@Inject
	IInfobaseAssociationManager infobaseAssociationManager;
	@Inject
	IInfobaseAccessManager infobaseAccessManager;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		
		if (currentSelection instanceof TreeSelection) {
			Object selectRef = ((TreeSelection) currentSelection).getFirstElement();
			
			if (selectRef instanceof InfobaseReference) {
				InfobaseReference infobaseRef = (InfobaseReference) selectRef;
				
				Optional<IInfobaseAssociation> infobaseAssociations = infobaseAssociationManager.getAssociation(infobaseRef);
				
				if (infobaseAssociations.isPresent()) {
					
					IProject project = infobaseAssociations.get().getProject();
					
					Pair<ILaunchableRuntimeComponent, IThickClientLauncher> v8Launcher;
					try {
						v8Launcher = createThickClientLauncher(project);
						closeDesignerSession(v8Launcher, buildArguments(infobaseRef), infobaseRef);
					} catch (MatchingRuntimeNotFound | RuntimeExecutionException e) {
						launchStatus = Activator.createErrorStatus(e);
						Activator.log(launchStatus);
					}
				}
				
				String exDesigner = System.getProperty("alternativeDesignerLauncher");
				if (Strings.isNullOrEmpty(exDesigner)) {
					launchStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "External designer not set");
					Activator.log(launchStatus);
					return null;
				}
				java.io.File file = new java.io.File(exDesigner);
				if (!file.exists()) {
					launchStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "External designer not found");
					Activator.log(launchStatus);
					return null;
				}
				
				String designerCommand = "DESIGNER";
				String baseNameCommand = "/IBName \"" + infobaseRef.getName() + "\"";
				String authCommand = addAuthentication(buildArguments(infobaseRef));
				
				String command = makeV8Command(exDesigner, designerCommand, baseNameCommand, authCommand);
				
				String processOutput = "";
				
				Process process;
				ProcessBuilder processBuilder = new ProcessBuilder();
				processBuilder.command("cmd.exe", "/c", command);

				try {
					process = processBuilder.start();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "windows-1251"));
					
					String line;
					while ((line = reader.readLine()) != null) {
						processOutput = processOutput.concat(System.lineSeparator()).concat(line);
					}
					
					int exitCode = process.waitFor();
					if (exitCode != 0) {
						launchStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "OperationAbort");
						Activator.log(Activator.createErrorStatus("OperationAbort"));
					}
					
				} catch (IOException | InterruptedException e) { // NOSONAR
					launchStatus = Activator.createErrorStatus(e);
					Activator.log(launchStatus);
				}
				
			}
		}
		
		return null;
	}
	
	protected Pair<ILaunchableRuntimeComponent, IThickClientLauncher> createThickClientLauncher(IProject project)
			throws MatchingRuntimeNotFound {
		
		Pair<ILaunchableRuntimeComponent, IThickClientLauncher> v8Launcher;
		
		IResolvableRuntimeInstallation resolvableRuntimeInstallation = resolvableRuntimeInstallationManager
				.getDefault(RuntimeInstallations.ENTERPRISE_PLATFORM, getV8VersionFromProject(project).toString());
		
		v8Launcher = runtimeComponentManager.getComponentAndExecutor(resolvableRuntimeInstallation.get(IRuntimeComponentTypes.THICK_CLIENT),
				IRuntimeComponentTypes.THICK_CLIENT);
		
		return v8Launcher;
		
	}
	
	private void closeDesignerSession(Pair<ILaunchableRuntimeComponent, IThickClientLauncher> v8Launcher,
			RuntimeExecutionArguments arguments, InfobaseReference infobase) throws RuntimeExecutionException {
		
		((IDesignerSessionThickClientLauncher) v8Launcher.second).closeDesignerSession(v8Launcher.first, infobase, arguments);
		
	}
	
	protected RuntimeExecutionArguments buildArguments(InfobaseReference infobase) {
		RuntimeExecutionArguments arguments = new RuntimeExecutionArguments();
		
		try {
			IInfobaseAccessSettings settings = infobaseAccessManager.getSettings(infobase);
			
			arguments.setAccess(settings.access());
			arguments.setUsername(settings.userName());
			arguments.setPassword(settings.password());
			
		} catch (CoreException e) {
			launchStatus = Activator.createErrorStatus(e);
			Activator.log(launchStatus);
		}
		
		return arguments;
	}
	
	private String addAuthentication(RuntimeExecutionArguments arguments) {
		String authString = "";
		
		if (arguments.getAccess() == InfobaseAccess.OS) {
			authString += "/WA +";
		} else if (arguments.getAccess() == InfobaseAccess.INFOBASE) {
			authString += "/WA -";
			if (!Strings.isNullOrEmpty(arguments.getUsername())) { 
				authString += " /N " + arguments.getUsername();
			}
			if (!Strings.isNullOrEmpty(arguments.getPassword())) { 
				authString += " /P " + arguments.getPassword();
			}
		}
		
		return authString;
	}

	public Version getV8VersionFromProject(IProject project) {
		return runtimeVersionSupport.getRuntimeVersion(project);
	}
	
	private String makeV8Command(String... commands) {
		return String.join(" ", commands);
	}
}
