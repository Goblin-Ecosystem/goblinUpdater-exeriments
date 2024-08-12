FROM openjdk:17-jdk

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN microdnf update && microdnf install -y maven git

COPY . /home/app/

WORKDIR /home/app
ENTRYPOINT ["mvn","clean", "compile", "exec:java"]