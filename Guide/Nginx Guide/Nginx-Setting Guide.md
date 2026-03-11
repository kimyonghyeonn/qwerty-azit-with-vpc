EC2-Setting Guide(FrontEnd) 진행 후 Nginx 세팅 해주시기 바랍니다.

Nginx 란?
웹서버, 클라이언트로부터 요청을 받아 내부 서버(WAS) 로 전달하기 위해 사용하며 이를 리버스 프록시라고 함
또한 로드 밸런싱으로 다수 서버에 트래픽을 분산시켜 부하를 절감하지만 공부를 위한 개인 플젝의 경우 트래픽 과부하가 없어서 로드 밸런싱은 안함

해당 가이드 문서는 EC2 FrontEnd 세팅이 끝난 뒤 이어서 세팅해주시기 바랍니다.

FrontEnd 가이드에서 다음과 같이 Nginx 세팅함
Nginx :
sudo yum -y install nginx
sudo systemctl enable nginx
sudo systemctl start nginx

Nginx 실행 파일
이에 따른 nginx 실행 파일은 다음 경로에 생성된다. => /usr/sbin/nginx (which nginx 명령어로 nginx 실행파일 위치 확인 가능)

Nginx 설정 파일
메인 설정
/etc/nginx/nginx.conf

Nginx 설정 전 점검사항

도메인 등록

1. 도메인 주소에 IP 등록 (본인은 NameCheap 에서 도메인 구매 후 도메인에 IP 적용)
2. Domain List > Advanced List 에 다음과 같이 세팅
   Type / Host / Value / TTL (Host 는 EC2 에 지정되어있는 고정IP)
   A record / @ / 12.345.67.890 / Automatic
   A record / api / 12.345.67.890 / Automatic
   A record / www / 12.345.67.890 / Automatic

EC2 보안그룹 포트 열기
HTTP(80), HTTPS(443), SSL(22) 포트 열려있는지 확인

Nginx 설정 과정

1. Cerbot(SSL 발급 도구) 설치 : sudo yum install certbot python3-certbot-nginx
2. SSL 인증서 발급 : sudo certbot --nginx
3. 인증서 자동 갱신 설정 (Let's Encrypt 인증서는 90일 유효하기 때문) : sudo certbot renew --dry-run (자동갱신 테스트)
   sudo systemctl list-timers | grep certbot
4. nginx.conf 파일 설정

Nginx 설계 요구사항

1. 도메인으로 서비스 (qwerty-azit.com / www.qwerty-azit.com)
2. 프론트 정적 배포
3. 백엔드는 Spring Boot, EC2 내부에서 8080포트로 구동
4. HTTPS 필수, HTTP => HTTPS 강제 이동
5. CORS 이슈 해결해야함
6. 파일 업로드 용량 20MB

트래픽 흐름
Client => Nginx(443/80) => 정적파일 or 프록시 => Spring(8080)
경로 분기
/api/\*\* = 백엔드 전달
이외 나머지 = 프론트 정적파일 제공
