import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.within"
    compileSdk = 34

    packaging{
        resources.excludes.add("META-INF/DEPENDENCIES")
    }

    defaultConfig {
        applicationId = "com.example.within"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.08.12.2012"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.biometric:biometric:1.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.stripe:stripe-android:20.36.1")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.1")
    implementation("com.twilio.sdk:twilio:9.14.1")
    implementation("com.twilio:audioswitch:1.1.8")
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("com.opencsv:opencsv:5.5")
    implementation("com.twilio:voice-android:6.4.0") // Use the correct version
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.code.gson:gson:2.10")
}
