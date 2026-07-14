plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sprayforecast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sprayforecast"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}

// Copy the project's index.html into assets at build time.
// The bundled asset is never hand-edited — edit the source HTML, then rebuild.
val sourceHtml = rootProject.projectDir.parentFile.resolve("index.html")
val generatedAssets = layout.buildDirectory.dir("generated/assets/main")

val copyLiveHtml by tasks.registering(Copy::class) {
    from(sourceHtml)
    into(generatedAssets)
    rename { "index.html" }
}

android.sourceSets.getByName("main") {
    assets.srcDir(generatedAssets)
}

afterEvaluate {
    tasks.named("mergeDebugAssets").configure { dependsOn(copyLiveHtml) }
    tasks.named("mergeReleaseAssets").configure { dependsOn(copyLiveHtml) }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.11.0")
}
