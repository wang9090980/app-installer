plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(property("COMPILE_SDK_VERSION").toString().toInt())
    defaultConfig {
        applicationId = "me.panpf.app.install.sample"
        minSdkVersion(property("MIN_SDK_VERSION").toString().toInt())
        targetSdkVersion(property("TARGET_SDK_VERSION").toString().toInt())
        versionCode = property("VERSION_CODE").toString().toInt()
        versionName = property("VERSION_NAME").toString()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    testImplementation("junit:junit:${property("JUNIT_VERSION")}")
    androidTestImplementation("androidx.test:runner:${property("ANDROIDX_TEST_RUNNER")}")
    androidTestImplementation("androidx.test:rules:${property("ANDROIDX_TEST_RULES")}")
    androidTestImplementation("androidx.test.ext:junit:${property("ANDROIDX_TEST_JUNIT")}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${property("ANDROIDX_TEST_ESPRESSO")}")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${property("KOTLIN_VERSION")}")
    implementation("androidx.appcompat:appcompat:${property("ANDROIDX_APPCOMPAT")}")
    implementation("androidx.core:core-ktx:${property("ANDROIDX_CORE_KTX")}")
    implementation("com.google.android.material:material:${property("ANDROIDX_MATERIAL")}")
    implementation("androidx.lifecycle:lifecycle-extensions:${property("ANDROIDX_LIFECYCLE")}")
    implementation("androidx.constraintlayout:constraintlayout:${property("ANDROIDX_CONSTRAINTLAYOUT")}")
    implementation("androidx.navigation:navigation-fragment:${property("ANDROIDX_NAVIGATION")}")
    implementation("androidx.navigation:navigation-ui:${property("ANDROIDX_NAVIGATION")}")
    implementation("androidx.navigation:navigation-fragment-ktx:${property("ANDROIDX_NAVIGATION")}")
    implementation("androidx.navigation:navigation-ui-ktx:${property("ANDROIDX_NAVIGATION")}")
    implementation(project(":app-installer"))
//    implementation(project(":app-installer-auto"))
//    implementation(project(":app-installer-root"))
//    implementation(project(":app-installer-xpk"))
    implementation("me.panpf:sketch:${property("SKETCH")}")
    implementation("me.panpf:assembly-adapter:${property("ASSEMBLY_ADAPTER")}")
    implementation("me.panpf:assembly-adapter-ktx:${property("ASSEMBLY_ADAPTER")}")
    implementation("me.panpf:androidx-kt:${property("PANPF_ANDROIDX")}")
    implementation("me.panpf:javax-kt:${property("PANPF_JAVAX")}")
}
