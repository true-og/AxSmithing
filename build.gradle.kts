/* ------------------------------ Plugins ------------------------------ */
plugins {
    id("java") // Import Java plugin.
    id("java-library") // Import Java Library plugin.
    id("com.diffplug.spotless") version "8.1.0" // Import Spotless plugin.
    id("com.gradleup.shadow") version "8.3.9" // Import Shadow plugin.
    id("checkstyle") // Import Checkstyle plugin.
    eclipse // Import Eclipse plugin.
}

/* --------------------------------- JDK -------------------------------- */
java {
    sourceCompatibility = JavaVersion.VERSION_17 // Compile with JDK 17 compatibility.
    toolchain { // Select Java toolchain.
        languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17.
        vendor.set(JvmVendorSpec.GRAAL_VM) // Use GraalVM CE.
    }
}

/* ----------------------------- Metadata ------------------------------ */
group = "com.artillexstudios.axsmithing" // Declare bundle identifier.

version = "1.9" // Declare plugin version (will be in .jar).

/* ----------------------------- Resources ----------------------------- */
tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version)
    inputs.properties(props) // Indicates to rerun if version changes.
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") } // Bundle license into jarfile.
}

/* ---------------------------- Repos ---------------------------------- */
repositories {
    mavenCentral() // Import the Maven Central Maven Repository.
    maven { url = uri("https://repo.alessiodp.com/releases/") } // Import the AlessioDP Maven Repository.
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    } // Import the SpigotMC Maven Repository.
    maven { url = uri("https://repo.viaversion.com") } // Import the ViaVersion Maven Repository.
    maven { url = uri("https://repo.artillex-studios.com/releases") } // Import the Artillex Studios Maven Repository.
}

/* ---------------------- Java project deps ---------------------------- */
dependencies {
    implementation("net.byteflux:libby-bukkit:1.2.0") // Import the Libby runtime dependency loader.
    implementation("com.artillexstudios.axapi:axapi:1.4.190:all") // Import the AxAPI library.
    compileOnly("com.google.code.gson:gson:2.10.1") // Import Gson (provided by the server at runtime).
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT") // Import the Spigot API.
    compileOnly(
        "com.viaversion:viaversion-api:5.9.1"
    ) // Import the ViaVersion API (closest official to true-og/ViaVersion 5.9.2-SNAPSHOT).
}

apply(from = "eclipse.gradle.kts") // Import eclipse classpath support script.

/* ---------------------- Reproducible jars ---------------------------- */
tasks.withType<AbstractArchiveTask>().configureEach { // Ensure reproducible .jars
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

/* ----------------------------- Shadow -------------------------------- */
tasks.shadowJar {
    relocate("net.byteflux.libby", "com.artillexstudios.axsmithing.libs.libby") // Relocate Libby.
    relocate("com.artillexstudios.axapi", "com.artillexstudios.axsmithing.libs.axapi") // Relocate AxAPI.
    archiveClassifier.set("") // Use empty string instead of "all".
}

tasks.jar { archiveClassifier.set("part") } // Applies to root jarfile only.

tasks.build { dependsOn(tasks.spotlessApply, tasks.shadowJar) } // Build depends on spotless and shadow.

/* --------------------------- Javac opts ------------------------------- */
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters") // Enable reflection for java code.
    options.isFork = true // Run javac in its own process.
    options.compilerArgs.add("-Xlint:deprecation") // Trigger deprecation warning messages.
    options.encoding = "UTF-8" // Use UTF-8 file encoding.
}

/* ----------------------------- Auto Formatting ------------------------ */
spotless {
    java {
        eclipse().configFile("config/formatter/eclipse-java-formatter.xml") // Eclipse java formatting.
        leadingTabsToSpaces() // Convert leftover leading tabs to spaces.
        removeUnusedImports() // Remove imports that aren't being called.
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) } // JetBrains Kotlin formatting.
        target("build.gradle.kts", "settings.gradle.kts", "eclipse.gradle.kts") // Gradle files to format.
    }
}

checkstyle {
    toolVersion = "10.18.1" // Declare checkstyle version to use.
    configFile = file("config/checkstyle/checkstyle.xml") // Point checkstyle to config file.
    isIgnoreFailures = true // Don't fail the build if checkstyle does not pass.
    isShowViolations = true // Show the violations in any IDE with the checkstyle plugin.
}

tasks.named("compileJava") {
    dependsOn("spotlessApply") // Run spotless before compiling with the JDK.
}

tasks.named("spotlessCheck") {
    dependsOn("spotlessApply") // Run spotless before checking if spotless ran.
}
