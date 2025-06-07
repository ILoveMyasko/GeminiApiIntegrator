FROM gradle:8.4-jdk21 AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle gradlew gradle/ ./

RUN gradle --no-daemon dependencies

COPY src src

RUN gradle --no-daemon clean jar -x test --stacktrace --info
FROM eclipse-temurin:21-jre-jammy


# Переменные окружения для версий и настроек
ENV CLOUDFLARED_VERSION=2025.5.0
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends wget ca-certificates procps && \
    rm -rf /var/lib/apt/lists/*

RUN wget -q https://github.com/cloudflare/cloudflared/releases/download/${CLOUDFLARED_VERSION}/cloudflared-linux-amd64.deb && \
    dpkg -i cloudflared-linux-amd64.deb && \
    rm cloudflared-linux-amd64.deb

ENV GOOGLE_API_KEY="AIzaSyAbZLMRdIkKRc0Sdd4KjXwrUmXE8Aa7M9g"

WORKDIR /app

COPY baseGeminiPrompt.txt baseGeminiPrompt.txt
COPY rawHtml.txt rawHtml.txt
COPY properties.cfg properties.cfg
COPY --from=builder  /workspace/build/libs/*.jar application.jar

COPY entrypoint.sh entrypoint.sh
RUN chmod +x ./entrypoint.sh


ENTRYPOINT ["./entrypoint.sh"]