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
    println("*************************************************")
    println("*** ENSURE TO HAVE CACHED GITHUB CREDENTIALS  ***")
    println("*** e.g. by running 'git fetch' or 'git push' ***")
    println("*** waiting 5 seconds...                      ***")
    println("*************************************************")
    Thread.sleep(5000)
    addons.support.ForkSupport.runAndWait(
      command = Array("mvn", "-s", "maven-settings.xml", "release:prepare", "release:perform")
    )
  }

}
