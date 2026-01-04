#!/bin/bash

# 도메인 및 이메일 설정
DOMAIN="exts-block.nyoung.cloud"
EMAIL="jjang1286@gmail.com"

echo "### Starting initial certificate request for $DOMAIN ..."

# 1. Nginx 실행 (이미 실행 중이어도 상관없음)
docker compose up -d web

# 2. 인증서 발급 요청
docker compose run --rm certbot certonly --webroot \
    --webroot-path /var/www/certbot \
    --email $EMAIL \
    --agree-tos \
    --no-eff-email \
    -d $DOMAIN

echo "### Certificate request complete!"
echo "If successful, uncomment the SSL block in nginx.conf and restart nginx."
