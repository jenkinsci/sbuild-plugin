import de.tototec.sbuild._

@version("0.6.0.9004")
class SBuild(implicit _project: Project) {

  Target("phony:clean") exec {
    Path("target").deleteRecursive
  }

  Target("phony:all") exec {
    addons.support.ForkSupport.runAndWait(
      command = Array("mvn", "-s", "maven-settings.xml", "package")
    )
  }

  Target("phony:release") exec {
    addons.support.ForkSupport.runAndWait(
      command = Array("mvn", "-s", "maven-settings.xml", "release:prepare", "release:perform")
    )
  }

}
