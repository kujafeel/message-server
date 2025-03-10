plugins {
    kotlin("jvm") version "1.9.10"
    application
}

application {
    mainClass.set("MainKt") // 메인 함수가 있는 파일 이름.kt 기준
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-websockets:2.3.4")
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("message-server")
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
    with(tasks.jar.get() as CopySpec)

    kotlin {
        jvmToolchain(17)
    }

}
