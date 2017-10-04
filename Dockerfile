FROM openjdk:8-jdk-alpine
ADD reference-messaging/target/hspc-reference-messaging*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]