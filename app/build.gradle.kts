plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.apollo)
}

val url = "https://swapi-graphql.netlify.app/.netlify/functions/index"

apollo {
    service("service") {
        packageName = "com.block57.homework.generate"

        srcDir("${project.projectDir}/src/main/graphql")
        // Warn if using a deprecated field
        warnOnDeprecatedUsages = true
        // Fail on warnings
        failOnWarnings = true

        val schemaPath = "${project.projectDir}/src/main/resources/star_wars.graphqls"
        schemaFiles.from(schemaPath)
        introspection {
            endpointUrl = url
            schemaFile = file(schemaPath)
        }

        // Whether to generate Kotlin or Java models
        generateKotlinModels = true
    }
//    generateSourcesDuringGradleSync.set(true)
}

android {
    namespace = "com.block57.homework"
    compileSdk = 34
    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "com.block57.homework"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "ENDPOINT_URL", "\"${url}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    afterEvaluate {
        tasks.forEach {
            println(it.toString())
        }
    }
}

//tasks.findByName("downloadServiceApolloSchemaFromIntrospection")?.apply dw@{
//    tasks.findByName("preBuild")?.apply {
//        dependsOn(this@dw)
//        println("register prebuild download graphql!")
//    }
//}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.apollo.runtime)
}