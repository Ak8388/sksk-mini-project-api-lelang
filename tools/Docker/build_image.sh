#! /bash/sh

sudo docker build -t sksk-lelang:v1 \
     --build-arg JAR="target/*.jar" \
     --build-arg ARGS="" \
     --build-arg JAVA_PROPS="" \
     -f ./tools/Docker/Dockerfile .


sudo docker images

#sudo docker-compose up

sudo docker run -it -d -p 8080:8080 sksk-lelang