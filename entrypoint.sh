#!/bin/bash
set -e # Выход при ошибке


# Параметры для cloudflared

CLOUDFLARED_PORT=${CLOUDFLARED_PORT:-53}
CLOUDFLARED_UPSTREAM1="https://xbox-dns.ru/dns-query"


echo "Starting cloudflared DNS proxy on port ${CLOUDFLARED_PORT}..."
echo "Upstream DoH server: ${CLOUDFLARED_UPSTREAM1}"

CLOUDFLARED_CMD="cloudflared proxy-dns --port ${CLOUDFLARED_PORT} --address 127.0.0.1 --upstream ${CLOUDFLARED_UPSTREAM1}"


eval "${CLOUDFLARED_CMD} &"

CLOUDFLARED_PID=$!


sleep 3

# Проверка, что cloudflared запустился
if ! ps -p $CLOUDFLARED_PID > /dev/null; then
  echo "ERROR: cloudflared failed to start!"
  exit 1
fi
echo "cloudflared started successfully (PID: $CLOUDFLARED_PID) with upstream ${CLOUDFLARED_UPSTREAM1}."

echo "nameserver 127.0.0.1" > /etc/resolv.conf

echo "Current /etc/resolv.conf:"
cat /etc/resolv.conf
cleanup() {
    echo "Stopping cloudflared (PID: $CLOUDFLARED_PID)..."
    kill $CLOUDFLARED_PID
    wait $CLOUDFLARED_PID # Ожидаем завершения процесса
    echo "cloudflared stopped."
    exit 0
}

trap cleanup SIGINT SIGTERM

echo "Starting Java application..."
java -jar application.jar "$@"

if ps -p $CLOUDFLARED_PID > /dev/null; then
    echo "Java application finished. Stopping cloudflared..."
    kill $CLOUDFLARED_PID
    wait $CLOUDFLARED_PID
fi

cleanup