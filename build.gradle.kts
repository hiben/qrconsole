plugins {
    id("java")
    id("application")
}

group = "de.zvxeb"
version = "1.0-SNAPSHOT"

application {
  mainClass = "de.zvxeb.QRConsole"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.nayuki:qrcodegen:1.8.0")
    implementation("org.jcommander:jcommander:2.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
  val fatJar = register<Jar>("fatJar") {
    dependsOn.addAll(listOf("compileJava", "processResources"))
    archiveFileName.set("QRConsole.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
      .map { if (it.isDirectory) it else zipTree(it) } +
      sourcesMain.output
    from(contents)
  }
  build {
    dependsOn(fatJar) // Trigger fat jar creation during build
  }
}