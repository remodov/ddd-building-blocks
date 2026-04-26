plugins {
    id("java-library")
    id("maven-publish")
}

val libVersion = libs.versions.ddd.building.blocks.get()

group = "ru.vikulinva"
version = libVersion

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "ru.vikulinva"
            artifactId = "ddd-building-blocks"
            version = libVersion
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/remodov/ddd-building-blocks")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
