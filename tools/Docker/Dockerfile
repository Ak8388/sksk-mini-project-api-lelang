FROM eclipse-temurin:21-jre-alpine

ARG JAR
ARG ARGS
ARG JAVA_PROPS

ENV ENV_JAR_ARGS=${ARGS}
ENV ENV_JAVA_PROPS=${JAVA_PROPS}

WORKDIR /app

RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Jakarta /etc/localtime \
    && echo "Asia/ Jakarta" > /etc/timezone

COPY ${JAR} app.jar

EXPOSE 8081

CMD java $ENV_JAVA_PROPS -jar app.jar $ENV_JAR_ARGS

# docker build -t sksk-lelang:v1 --build-arg JAR="target/*.jar" --build-arg ARGS="" --build-arg JAVA_PROPS="" -f ./tools/Docker/Dockerfile .