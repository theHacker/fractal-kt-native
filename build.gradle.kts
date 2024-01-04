plugins {
    kotlin("multiplatform") version "1.9.20"
}

group = "biz.thehacker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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
