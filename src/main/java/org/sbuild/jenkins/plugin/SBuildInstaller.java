package org.sbuild.jenkins.plugin;

import hudson.Extension;
import hudson.FilePath;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.ToolInstallation;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

public class SBuildInstaller extends DownloadFromUrlInstaller {

	@DataBoundConstructor
	public SBuildInstaller(String id) {
		super(id);

	}

	@Override
	protected FilePath findPullUpDirectory(FilePath root) throws IOException, InterruptedException {
		if (root.child("bin").exists()) {
			return root;
		} else {
			FilePath child = root.child("sbuild-" + id);
			if (child.exists()) {
				return root.child("sbuild-" + id);
			} else {
				// TODO: handle error
				return child;
			}
		}
	}

	@Extension
	public static final class DescriptiorImpl extends
			DownloadFromUrlInstaller.DescriptorImpl<SBuildInstaller> {
		@Override
		public String getDisplayName() {
			return "Install from sbuild.tototec.de";
		}

		@Override
		public boolean isApplicable(
				Class<? extends ToolInstallation> toolType) {
			return toolType == SBuildInstallation.class;
		}
	}

}
