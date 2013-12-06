import de.tototec.sbuild._

@version("0.6.0")
class SBuild(implicit _project: Project) {

  def mvn(args: String*) {
    addons.support.ForkSupport.runAndWait(
      command = Array("mvn", "-s", "maven-settings.xml") ++ args
    )
  }

  Target("phony:clean") exec { Path("target").deleteRecursive }
  Target("phony:all") exec { mvn("package") }
  Target("phony:test") exec { mvn("test") }

  Target("phony:release") exec {
    println("*************************************************")
    println("*** ENSURE TO HAVE CACHED GITHUB CREDENTIALS  ***")
    println("*** e.g. by running 'git fetch' or 'git push' ***")
    println("*** waiting 5 seconds...                      ***")
    println("*************************************************")
    Thread.sleep(5000)
    mvn("release:prepare", "release:perform")
  }

}
