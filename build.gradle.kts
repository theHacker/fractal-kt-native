plugins {
    kotlin("multiplatform") version "1.9.20"
}

group = "biz.thehacker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

kotlin {
    linuxX64 {
        binaries {
            executable {
                entryPoint = "main"
            }

            compilations.getByName("main") {
                cinterops {
                    val libpng by creating
                }
            }
        }
    }
}
