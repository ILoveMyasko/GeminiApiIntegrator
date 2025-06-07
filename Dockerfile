FROM gradle:8.4-jdk21 AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle gradlew gradle/ ./

RUN gradle --no-daemon dependencies

COPY src src

RUN gradle --no-daemon clean jar -x test --stacktrace --info
# 1. Базовый образ с Java (выберите подходящий для вашего приложения)
FROM eclipse-temurin:21-jre-jammy
# Метки для информации об образе (опционально)
LABEL maintainer="your-email@example.com"
LABEL description="Java application with cloudflared for DNS over HTTPS"

# Переменные окружения для версий и настроек
ENV CLOUDFLARED_VERSION=2025.5.0
# Проверьте последнюю версию на GitHub Cloudflare releases
ENV DEBIAN_FRONTEND=noninteractive

# 2. Установка зависимостей (wget для скачивания, ca-certificates)
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget ca-certificates procps && \
    rm -rf /var/lib/apt/lists/*

# 3. Скачивание и установка cloudflared
RUN wget -q https://github.com/cloudflare/cloudflared/releases/download/${CLOUDFLARED_VERSION}/cloudflared-linux-amd64.deb && \
    dpkg -i cloudflared-linux-amd64.deb && \
    rm cloudflared-linux-amd64.deb

ENV GOOGLE_API_KEY="AIzaSyAbZLMRdIkKRc0Sdd4KjXwrUmXE8Aa7M9g"

WORKDIR /app
# 4. Копирование вашего Java-приложения в контейнер
# Замените 'your-app.jar' на имя вашего jar-файла
COPY --from=builder  /workspace/build/libs/*.jar application.jar

# 5. Копирование entrypoint-скрипта
COPY entrypoint.sh entrypoint.sh
RUN chmod +x ./entrypoint.sh



# Порт, на котором слушает ваше Java-приложение (если это веб-сервис)
# EXPOSE 8080

# 6. Запуск entrypoint-скрипта
ENTRYPOINT ["./entrypoint.sh"]