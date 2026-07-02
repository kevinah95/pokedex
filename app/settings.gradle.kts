rootProject.name = "Pokedex"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    maven("https://packages.jetbrains.team/maven/p/kt/dev")
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version "4.5.0"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

develocity {
  buildScan {
    publishing.onlyIf { !System.getenv("CI").isNullOrEmpty() }
    termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
    termsOfUseAgree.set("yes")
  }
}

dependencyResolutionManagement {
  repositories {
    maven("https://packages.jetbrains.team/maven/p/kt/dev")
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

include(":androidApp")
include(":shared")
include(":webApp")