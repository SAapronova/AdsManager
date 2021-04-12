FROM registry.do.x5.ru/shared/base-containers/openjdk/11:latest

ENV JAVA_OPTS ""

# The build/libs/*.jar files appear in the build stage. The files are uploaded to the runner
COPY build/libs/*.jar /app/

WORKDIR /app

ENTRYPOINT java $JAVA_OPTS -jar *.jar