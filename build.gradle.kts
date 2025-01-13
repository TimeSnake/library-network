plugins {
    id("java")
    id("java-base")
    id("java-library")
    id("maven-publish")
}


group = "de.timesnake"
version = "2.0.0"
var projectId = 37

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://git.timesnake.de/api/v4/groups/7/-/packages/maven")
        name = "timesnake"
        credentials(PasswordCredentials::class)
    }
}

dependencies {
    implementation("org.freemarker:freemarker:2.3.31")
    implementation("commons-io:commons-io:2.11.0")

    compileOnly("de.timesnake:database-api:4.+")
    compileOnly("de.timesnake:channel-api:5.+")
    compileOnly("de.timesnake:library-basic:2.+")

    compileOnly("com.moandjiezana.toml:toml4j:0.7.3-SNAPSHOT")
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        if (project.parent != null) {
            substitute(module("de.timesnake:database-api")).using(project(":database:database-api"))
            substitute(module("de.timesnake:channel-api")).using(project(":channel:channel-api"))
            substitute(module("de.timesnake:library-basic")).using(project(":libraries:library-basic"))
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://git.timesnake.de/api/v4/projects/$projectId/packages/maven")
            name = "timesnake"
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}