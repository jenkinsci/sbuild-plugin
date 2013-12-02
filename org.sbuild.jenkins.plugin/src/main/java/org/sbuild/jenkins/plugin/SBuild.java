package org.sbuild.jenkins.plugin;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;
import org.sbuild.jenkins.plugin.internal.Optional;

public class SBuild extends Builder {

	private final String sbuildVersion;

	private final String targets;

	private final String buildFiles;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public SBuild(String sbuildVersion, String targets, String buildFiles) {
		this.sbuildVersion = sbuildVersion;
		this.targets = targets;
		this.buildFiles = buildFiles;
	}

	public String getSbuildVersion() {
		return sbuildVersion;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getTargets() {
		return targets;
	}

	public Optional<SBuildInstallation> getSBuild() {
		if (sbuildVersion != null && sbuildVersion.trim().length() > 0) {
			for (SBuildInstallation sbuildInstallation : getDescriptor().getInstallations()) {
				if (sbuildVersion.equals(sbuildInstallation.getName()))
					return Optional.some(sbuildInstallation);
			}
			return Optional.some(null);
		} else {
			return Optional.none();
		}
	}

	@Override
	public boolean perform(@SuppressWarnings("rawtypes") AbstractBuild build,
			Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		ArgumentListBuilder args = new ArgumentListBuilder();

		EnvVars env = build.getEnvironment(listener);
		@SuppressWarnings("unchecked")
		final Map<String, String> buildVariables = build.getBuildVariables();
		env.overrideAll(buildVariables);

		Optional<SBuildInstallation> sbuildInstallation = getSBuild();
		if (sbuildInstallation.isDefined()) {
			SBuildInstallation installation = sbuildInstallation.get();
			if (installation == null) {
				listener.fatalError("Could not find the configured SBuild version \"" + sbuildVersion.trim() + "\".");
				return false;
			}
			installation = installation.forNode(Computer.currentComputer().getNode(), listener);
			installation = installation.forEnvironment(env);
			final String exe = installation.getExecutable(launcher);
			if (exe == null) {
				listener.fatalError("SBuild executable for configured version \"" + sbuildVersion + "\" not found.");
				return false;
			}
			args.add(exe);
		} else {
			listener.getLogger()
					.println(
							"WARNING: Using preinstalled SBuild installation from host system. This might give unexpected and unreproducable results.");
			args.add(launcher.isUnix() ? "sbuild" : "sbuild.bat");
		}

		args.add("--no-color");

		final List<String> buildFilesToUse = new LinkedList<String>();
		if (buildFiles != null) {
			String[] files = buildFiles.trim().split(" ");
			boolean firstFile = true;
			for (String file : files) {
				file = env.expand(file.trim());
				if (file.length() > 0) {
					buildFilesToUse.add(file);
					if (firstFile) {
						args.add("--buildfile", file);
						firstFile = false;
					} else {
						args.add("--additional-buildfile", file);
					}
				}
			}
		}
		if (buildFilesToUse.isEmpty()) {
			buildFilesToUse.add("SBuild.scala");
		}

		for (String file : buildFilesToUse) {
			FilePath buildFilePath = build.getModuleRoot().child(file);
			if (!buildFilePath.exists()) {
				listener.fatalError("Unabled to find buildfile at " + buildFilePath);
			}
		}

		if (targets != null) {
			String[] ts = targets.trim().split(" ");
			for (String target : ts) {
				target = env.expand(target.trim());
				if (target.length() > 0) {
					args.add(target);
				}
			}
		}

		if (!launcher.isUnix()) {
			args = args.toWindowsCommand();
		}

		int res = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getModuleRoot()).join();

		if (res != 0) {
			listener.fatalError("SBuild return with return code: " + res);
		}

		return res == 0;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {

		// private String sbuildVersion;

		@CopyOnWrite
		private volatile SBuildInstallation[] installations = new SBuildInstallation[0];

		public DescriptorImpl() {
			load();
		}

		protected DescriptorImpl(Class<? extends SBuild> clazz) {
			super(clazz);
		}

		public boolean isApplicable(
				@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "Build with SBuild";
		}

		// @Override
		// public boolean configure(StaplerRequest req, JSONObject formData)
		// throws FormException {
		// // To persist global configuration information,
		// // set that to properties and call save().
		// sbuildVersion = formData.getString("sbuildVersion");
		// // ^Can also use req.bindJSON(this, formData);
		// // (easier when there are many fields; need set* methods for this,
		// // like setUseFrench)
		// save();
		// return super.configure(req, formData);
		// }
		//
		// public String getSBuildVersion() {
		// return sbuildVersion;
		// }

		public SBuildInstallation[] getInstallations() {
			return installations;
		}

		public void setInstallations(SBuildInstallation... installations) {
			this.installations = installations;
			save();
		}

	}

}
