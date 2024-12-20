FROM rust:1.83 AS build-native
WORKDIR /app
COPY spring-osu-extended/native/Cargo.toml\
     spring-osu-extended/native/Cargo.lock\
     spring-osu-extended/native/jni_macro\
     spring-osu-extended/native/src\
     ./
RUN cargo build --release

FROM gradle:8.10.0-jdk21-graal AS build-app

WORKDIR /app

COPY *.gradle.kts ./
COPY buildSrc ./buildSrc
COPY spring-application/build.gradle.kts ./spring-application/build.gradle.kts
COPY spring-core/build.gradle.kts ./spring-core/build.gradle.kts
COPY spring-image/build.gradle.kts ./spring-image/build.gradle.kts
COPY spring-osu-api/build.gradle.kts ./spring-osu-api/build.gradle.kts
COPY spring-osu-beatmap-mirror/build.gradle.kts ./spring-osu-beatmap-mirror/build.gradle.kts
COPY spring-osu-extended/build.gradle.kts ./spring-osu-extended/build.gradle.kts
COPY spring-osu-persistence/build.gradle.kts ./spring-osu-persistence/build.gradle.kts
COPY spring-web/build.gradle.kts ./spring-web/build.gradle.kts
COPY gradle.properties ./
COPY gradle ./gradle
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
RUN gradle --no-daemon dependencies

WORKDIR /app
COPY . .
COPY --from=build-native /**/libspring_jni.so ./spring-osu-extended/src/main/resources/lib/libspring_jni.so
RUN gradle buildApplication

FROM eclipse-temurin:21 AS jre-build
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base \
         --add-modules java.compiler \
         --add-modules java.desktop \
         --add-modules java.instrument \
         --add-modules java.logging \
         --add-modules java.management \
         --add-modules java.naming \
         --add-modules java.security.jgss \
         --add-modules java.sql \
         --add-modules java.transaction.xa \
         --add-modules java.xml \
         --add-modules jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --output /javaruntime

FROM debian:buster-slim AS runtime
LABEL authors="osu-framework"

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

WORKDIR /app
COPY --from=build-app /app/build/libs/app.jar /app.jar

ARG PORT=8080
ARG DATABASE_URL
ARG DATABASE_USERNAME
ARG DATABASE_PASSWORD

ENV server.port=${PORT}
ENV database.url=${DATABASE_URL}
ENV database.username=${DATABASE_USERNAME}
ENV database.password=${DATABASE_PASSWORD}

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "/app.jar"]

