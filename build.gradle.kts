plugins {
    kotlin("jvm") version "1.9.10"
    application
}

application {
    mainClass.set("MainKt") // entry point
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
