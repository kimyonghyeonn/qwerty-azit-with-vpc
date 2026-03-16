<!-- Nginx app.conf 설정파일 -->

server {
server_name qwerty-azit.com www.qwerty-azit.com;

    # 프론트 정적파일 경로 (프론트 배포 후 사용)
    root /var/www/frontend;
    index index.html;

    # 첨부파일 용량 설정
    client_max_body_size 20m;

    # 백엔드 API 프록시
    location /api/ {


              # ✅ preflight(OPTIONS) 요청은 Nginx에서 즉시 처리
                 if ($request_method = OPTIONS) {
        add_header 'Access-Control-Allow-Origin' "$http_origin" always;
        add_header 'Access-Control-Allow-Credentials' 'true' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, PATCH, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type, X-Requested-With' always;
        add_header 'Access-Control-Max-Age' 86400 always;
        return 204;
                }

                 # ✅ 실제 API 응답에도 CORS 헤더 붙이기
        add_header 'Access-Control-Allow-Origin' "$http_origin" always;
        add_header 'Access-Control-Allow-Credentials' 'true' always;

        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 프론트 SPA 라우팅 지원 (배포 후 사용)
    location / {
        root /var/www/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/qwerty-azit.com/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/qwerty-azit.com/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

}
server {
if ($host = www.qwerty-azit.com) {
        return 301 https://$host$request_uri;
} # managed by Certbot

    if ($host = qwerty-azit.com) {
        return 301 https://$host$request_uri;
    } # managed by Certbot


    listen 80;
    server_name qwerty-azit.com www.qwerty-azit.com;
    return 404; # managed by Certbot

}
