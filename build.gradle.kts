import com.google.protobuf.gradle.*

plugins {
	id("org.jetbrains.kotlin.jvm") version "1.4.32"
	id("org.jetbrains.kotlin.kapt") version "1.4.32"
	id("com.github.johnrengelman.shadow") version "7.0.0"
	id("io.micronaut.application") version "1.5.0"
	id("org.jetbrains.kotlin.plugin.allopen") version "1.4.32"
	id("com.google.protobuf") version "0.8.15"
}

version = "0.1"
group = "br.com.itau"

val kotlinVersion = project.properties.get("kotlinVersion")
repositories {
	mavenCentral()
}

micronaut {
	runtime("netty")
	testRuntime("junit5")
	processing {
		incremental(true)
		annotations("br.com.itau.*")
	}
}

dependencies {
	implementation("io.micronaut:micronaut-runtime")
	implementation("io.micronaut.grpc:micronaut-grpc-client-runtime")
	implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
	implementation("javax.annotation:javax.annotation-api")
	implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
	runtimeOnly("ch.qos.logback:logback-classic")
	implementation("io.micronaut:micronaut-validation")
	runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.hibernate.validator:hibernate-validator:6.1.2.Final")

	testImplementation("io.micronaut:micronaut-http-client")
	testImplementation("org.mockito:mockito-core:2.1.0")
	testImplementation("org.mockito:mockito-all:1.10.19")
	testImplementation("org.mockito:mockito-core:3.9.0")
	testImplementation("org.mockito:mockito-inline:3.9.0")
	testCompile("org.junit.jupiter:junit-jupiter-params:5.2.3")
}


application {
	mainClass.set("br.com.itau.ApplicationKt")
}
java {
	sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
	compileKotlin {
		kotlinOptions {
			jvmTarget = "11"
		}
	}
	compileTestKotlin {
		kotlinOptions {
			jvmTarget = "11"
		}
	}

}
sourceSets {
	main {
		java {
			srcDirs("build/generated/source/proto/main/grpc")
			srcDirs("build/generated/source/proto/main/java")
		}
	}
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:3.14.0"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.33.1"
		}
	}
	generateProtoTasks {
		ofSourceSet("main").forEach {
			it.plugins {
				// Apply the "grpc" plugin whose spec is defined above, without options.
				id("grpc")
			}
		}
	}
}
