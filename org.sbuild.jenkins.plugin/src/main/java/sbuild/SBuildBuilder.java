package sbuild;

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
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;

public class SBuildBuilder extends Builder {

	private final String sbuildVersion;

	private final String targets;

	private final String buildFile;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public SBuildBuilder(String sbuildVersion, String targets, String buildFile) {
		this.sbuildVersion = sbuildVersion;
		this.targets = targets;
		this.buildFile = buildFile;
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

	public SBuildInstallation getSBuild() {
		for (SBuildInstallation sbuildInstallation : getDescriptor().getInstallations()) {
			if (sbuildVersion != null && sbuildVersion.equals(sbuildInstallation.getName()))
				return sbuildInstallation;
		}
		return null;
	}

	@Override
	public boolean perform(@SuppressWarnings("rawtypes") AbstractBuild build,
			Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		ArgumentListBuilder args = new ArgumentListBuilder();

		EnvVars env = build.getEnvironment(listener);
		@SuppressWarnings("unchecked")
		final Map<String, String> buildVariables = build.getBuildVariables();
		env.overrideAll(buildVariables);

		SBuildInstallation sbuildInstallation = getSBuild();
		if (sbuildInstallation != null) {
			sbuildInstallation = sbuildInstallation.forNode(Computer.currentComputer().getNode(), listener);
			sbuildInstallation = sbuildInstallation.forEnvironment(env);
			final String exe = sbuildInstallation.getExecutable(launcher);
			if (exe == null) {
				listener.fatalError("SBuild executable for configured version \"" + sbuildVersion + "\" not found.");
				return false;
			}
			args.add(exe);
		} else {
			args.add(launcher.isUnix() ? "sbuild" : "sbuild.bat");
		}

		args.add("--no-color");

		final String buildFileToUse;
		if (buildFile != null && buildFile.trim().length() > 0) {
			buildFileToUse = env.expand(buildFile);
			args.add("--buildfile", buildFileToUse);
		} else {
			buildFileToUse = "SBuild.scala";
		}
		FilePath buildFilePath = build.getModuleRoot().child(buildFileToUse);
		if (!buildFilePath.exists()) {
			listener.fatalError("Unabled to find buildfile at " + buildFilePath);
		}

		String targetsToUse = env.expand(this.targets);
		args.add(targetsToUse);

		if (!launcher.isUnix()) {
			args = args.toWindowsCommand();
		}

		// long startTime = System.currentTimeMillis();

		int res = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getModuleRoot()).join();

		// if(sbuildInstallation == null && (System.currentTimeMillis() -
		// startTime) < 1000) {
		//
		// }

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

		protected DescriptorImpl(Class<? extends SBuildBuilder> clazz) {
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
