plugins {
    kotlin("multiplatform") version "1.9.20"
}

group = "biz.thehacker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val targets = listOf(
        linuxX64()
    )

    targets.forEach { target ->
        target.apply {
            binaries {
                executable {
                    entryPoint = "main"
                }
            }
        }
    }
}
