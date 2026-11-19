pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// Opt in with -PnextlibPath=../nextlib to test unpublished decoder changes.
providers.gradleProperty("nextlibPath").orNull?.let { nextlibPath ->
    includeBuild(nextlibPath) {
        dependencySubstitution {
            substitute(
                module("io.github.darkstar085:nextlib-extended-media3ext"),
            ).using(project(":media3ext"))
            substitute(
                module("io.github.darkstar085:nextlib-extended-mediainfo"),
            ).using(project(":mediainfo"))
        }
    }
}

rootProject.name = "Playora"
include(":app")
