package org.sbuild.jenkins.plugin;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import hudson.Extension;
import hudson.FilePath;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.ToolInstallation;

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

		@Override
		public List<? extends Installable> getInstallables() throws IOException {
			final String[][] matrix = new String[][] {
					{ "0.7.1", "http://sbuild.tototec.de/sbuild/attachments/download/87/sbuild-0.7.1-dist.zip" },
					{ "0.7.0", "http://sbuild.tototec.de/sbuild/attachments/download/82/sbuild-0.7.0-dist.zip" },
					{ "0.6.0", "http://sbuild.tototec.de/sbuild/attachments/download/75/sbuild-0.6.0-dist.zip" },
					{ "0.5.0", "http://sbuild.tototec.de/sbuild/attachments/download/71/sbuild-0.5.0-dist.zip" },
					{ "0.4.0", "http://sbuild.tototec.de/sbuild/attachments/download/57/sbuild-0.4.0-dist.zip" },
					{ "0.3.2", "http://sbuild.tototec.de/sbuild/attachments/download/53/sbuild-0.3.2-dist.zip" },
					{ "0.3.1", "http://sbuild.tototec.de/sbuild/attachments/download/49/sbuild-0.3.1-dist.zip" },
					{ "0.3.0", "http://sbuild.tototec.de/sbuild/attachments/download/44/sbuild-0.3.0-dist.zip" },
					{ "0.2.0", "http://sbuild.tototec.de/sbuild/attachments/download/39/sbuild-0.2.0-dist.zip" },
					{ "0.1.5", "http://sbuild.tototec.de/sbuild/attachments/download/28/sbuild-0.1.5-dist.zip" },
					{ "0.1.4", "http://sbuild.tototec.de/sbuild/attachments/download/19/sbuild-0.1.4-dist.zip" },
					{ "0.1.3", "http://sbuild.tototec.de/sbuild/attachments/download/14/sbuild-0.1.3-dist.zip" },
					{ "0.1.2", "http://sbuild.tototec.de/sbuild/attachments/download/9/sbuild-0.1.2-dist.zip" },
					{ "0.1.1", "http://sbuild.tototec.de/sbuild/attachments/download/4/sbuild-0.1.1-dist.zip" },
					{ "0.1.0", "http://sbuild.tototec.de/sbuild/attachments/download/1/sbuild-0.1.0-dist.zip" }
			};

			LinkedList<Installable> installables = new LinkedList<Installable>();
			for (String[] info : matrix) {
				Installable i = new Installable();
				i.id = info[0];
				i.name = "SBuild " + info[0];
				i.url = info[1];
				installables.add(i);
			}

			return installables;
		}
	}

}
