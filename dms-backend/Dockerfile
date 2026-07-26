FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# 首次构建需下载全部 Maven 依赖，去掉 -q 保持输出流动，避免部署端 SSH 因长时间静默断开
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -DskipTests package

FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN groupadd -r dms && useradd -r -g dms dms
USER dms
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Shanghai"
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
