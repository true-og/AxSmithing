/* This is free and unencumbered software released into the public domain */

import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.plugins.ide.eclipse.model.EclipseModel

val eclipseModel = extensions.getByType<EclipseModel>()

eclipseModel.project.name = "${rootProject.name}-Plugin"

/* Expose compileOnly dependencies to the Eclipse classpath so the IDE resolves them. */
fun Project.addResolvableEclipseConfigs() {
    val jarAttr = objects.named(LibraryElements::class, LibraryElements.JAR)
    val apiAttr = objects.named(Usage::class, Usage.JAVA_API)

    val compileOnlyRes =
        configurations.create("eclipseCompileOnly") {
            extendsFrom(configurations.getByName("compileOnly"))
            isCanBeResolved = true
            isCanBeConsumed = false
            attributes.attribute(Usage.USAGE_ATTRIBUTE, apiAttr)
            attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, jarAttr)
        }
    eclipseModel.classpath.plusConfigurations.add(compileOnlyRes)
}

addResolvableEclipseConfigs()
