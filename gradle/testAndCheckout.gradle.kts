// Apply with:
//   ./gradlew -I gradle/testAndCheckout.gradle.kts testAndCheckout -PtargetBranch=<branch>

tasks.register("testAndCheckout") {
    group = "verification"
    description = "Run tests; on success checkout branch given by -PtargetBranch"
    doLast {
        val target = project.findProperty("targetBranch") as String?
            ?: throw GradleException("Provide -PtargetBranch=<branch>")
        println("Running tests before switching to: $target")
        exec { commandLine("./gradlew", "test") }
        exec { commandLine("git", "checkout", target) }
        println("Switched to branch: $target")
    }
}
