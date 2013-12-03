package org.sbuild.jenkins.plugin;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SBuildInstallation extends ToolInstallation implements
		EnvironmentSpecific<SBuildInstallation>,
		NodeSpecific<SBuildInstallation> {
	private static final long serialVersionUID = 1L;

	@DataBoundConstructor
	public SBuildInstallation(String name, String home,
			List<? extends ToolProperty<?>> properties) {
		super(name, home, properties);
	}

	@Override
	public void buildEnvVars(EnvVars env) {
		env.put("SBUILD_HOME", getHome());
	}

	public String getExecutable(Launcher launcher) throws IOException,
			InterruptedException {
		return launcher.getChannel().call(
				new Callable<String, IOException>() {
					private static final long serialVersionUID = 1L;

					public String call() throws IOException {
						File exe = getExeFile();
						if (exe.exists())
							return exe.getPath();
						return null;
					}
				});
	}

	private File getExeFile() {
		String execName = Functions.isWindows() ? "sbuild.bat" : "sbuild";
		String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
		return new File(home, "bin/" + execName);
	}

	public boolean getExists() throws IOException, InterruptedException {
		return getExecutable(new Launcher.LocalLauncher(TaskListener.NULL)) != null;
	}

	@Override
	public SBuildInstallation forEnvironment(EnvVars env) {
		return new SBuildInstallation(getName(),
				env.expand(getHome()), getProperties().toList());
	}

	@Override
	public SBuildInstallation forNode(Node node, TaskListener log)
			throws IOException, InterruptedException {
		return new SBuildInstallation(getName(), translateFor(node, log),
				getProperties().toList());
	}

	@Extension
	public static class DescriptorImpl extends
			ToolDescriptor<SBuildInstallation> {

		@Override
		public String getDisplayName() {
			return "SBuild";
		}

		@Override
		public SBuildInstallation[] getInstallations() {
			return Jenkins.getInstance().getDescriptorByType(SBuild.DescriptorImpl.class).getInstallations();
		}

		@Override
		public void setInstallations(SBuildInstallation... installations) {
			Jenkins.getInstance().getDescriptorByType(SBuild.DescriptorImpl.class)
					.setInstallations(installations);
		}

		@Override
		public List<? extends ToolInstaller> getDefaultInstallers() {
			return Collections.singletonList(new SBuildInstaller(null));
		}

		/**
		 * Checks if the SBUILD_HOME is valid.
		 */
		public FormValidation doCheckHome(@QueryParameter File value) {
			// this can be used to check the existence of a file on the
			// server, so needs to be protected
			if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER))
				return FormValidation.ok();

			if (value.getPath().equals(""))
				return FormValidation.ok();

			if (!value.isDirectory())
				return FormValidation.error("No a directory");

			File antJar = new File(value, "bin/sbuild");
			if (!antJar.exists())
				return FormValidation.error("Directory seems not to be an SBuild installation directory.");

			return FormValidation.ok();
		}

		public FormValidation doCheckName(@QueryParameter String value) {
			return FormValidation.validateRequired(value);
		}
	}

}