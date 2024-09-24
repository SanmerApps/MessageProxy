plugins {
    alias(libs.plugins.self.library)
}

android {
    namespace = "dev.sanmer.lettre"

    defaultConfig {
        ndk.abiFilters += listOf("arm64-v8a", "x86_64")

        consumerProguardFile("proguard-rules.pro")
    }

    sourceSets.all {
        jniLibs.srcDir("src/main/libs")
    }
}