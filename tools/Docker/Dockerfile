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

EXPOSE 8080

CMD java $ENV_JAVA_PROPS -jar app.jar $ENV_JAR_ARGS
