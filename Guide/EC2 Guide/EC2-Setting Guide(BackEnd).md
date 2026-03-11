EC2-Setting Guide(Front) 세팅 후 해당 백엔드 세팅 가이드 문서대로 진행해주시기 바랍니다.

BackEnd CICD 의 절차

1. GitHub Actions가 SSH로 EC2 접속 => /srv/app/backend/build/libs 에 빌드된 jar를 복사
2. EC2 의 가장 최신 app.jar 를 덮어쓴 뒤 systemctl restart backend 로 재기동

BackEnd EC2 Setting (아래 순서대로 진행)

1. 원격 디렉토리 세팅 (/srv/app/backend/build/libs) : sudo mkdir -p /srv/app/backend/build/libs

2. 권한/소유자 설정 :
   sudo chown -R ec2-user:ec2-user /srv/app
   sudo chmod -R 755 /srv/app

3. 서비스 파일 생성 :

<!-- DB 관련 정보는 RDS 접속정보이며 해당 RDS의 연결 및 보안 > 엔드포인트 클릭하여 확인
DB_HOST : RDS 엔드포인트
DB_PORT : RDS 포트
DB_NAME : RDS 데이터베이스 이름
DB_USER : RDS 마스터 사용자 이름
DB_PASS : RDS Secret Manager
* 접속정보 및 민감정보는 본인이 찾기 쉬운 장소에 추가 별도 저장하여 관리
-->

<!-- 파일 시작 -->

sudo tee /etc/systemd/system/backend.service > /dev/null << 'EOF'

[Unit]
Description=Book Review Backend
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/srv/app/backend
Environment=DB_HOST=
Environment=DB_PORT=
Environment=DB_NAME=
Environment=DB_USER=
Environment=DB_PASS=
Environment=KAKAO_CLIENT_ID=
Environment=KAKAO_CLIENT_SECRET=
Environment=KAKAO_REDIRECT_URI=

ExecStart=/usr/bin/java -jar /srv/app/backend/build/libs/app.jar
Restart=always
RestartSec=3
Environment=SPRING_PROFILES_ACTIVE=prod

[Install]
WantedBy=multi-user.target

EOF

<!-- 파일 종료 -->

서비스 파일 생성과정에 대해 정리
데몬 : 사용자가 직접적으로 제어하지 않고 백그라운드에서 여러 작업을 하는 프로그램, 일반적으로 프로세스 이름 마지막에 d 가 붙음 (Ex. systemd, syslogd 등등)
Linux 는 OS 가 부팅하며 여러가지 데몬이 실행되며 부팅과 함께 실행되는 데몬은 백그라운드로 처리하는 데몬들임. 이런 데몬들은 Linux 에서 service 파일로 설정하여 실행되고 있고 systemd 라는 프로세스가 관리함

Service : 시스템 데몬 및 사용자 정의 데몬, 생성되면서 종료될 때까지 실행되는 프로세스 및 설정 파일을 의미, /etc/systemd/system 경로에 존재하고 systemd 에 의해 관리되고 있는 서비스들임
서비스 파일은 [Unit], [Service], [Install] 섹션으로 구성되어있음, 각 세부적인 속성들은 추가 구글링 필요

systemctl : service(데몬) 들을 관리하는 명령어

backend.service : 백엔드 구동시 필요한 서비스 파일, 서비스 파일은 /etc/systemd/system/ 디렉토리에 위치하고 .service 확장자를 가짐
systemd 는 리눅스에서 사용하는 시스템 및 서비스 관리자임

명령어 설명
Q. sudo tee /etc/systemd/system/backend.service > /dev/null <<'EOF'
A. vi, vim, nano 를 사용하지 않아야하는건 아니지만 CICD 운영 자동화 가이드에서는 tee를 사용하는것을 권고함
'> /dev/null' 는 화면에 불필요한 출력을 남기지 않기위해 작성
<<'EOF' 에서 EOF 는 End Of File 을 의미함, '' 내부에 작성한 이유는 변수 해석을 방지하기 위함

해당 파일 보안 취약점
DB 접속정보, KAKAO API 관련 정보 등 서비스 파일에 직접 작성하면 안됨 => 환경설정 관련 파일(EnvironmentFile) 로 분리해서 관리 필요

4. systemd 반영 및 자동시작 등록
   sudo systemctl daemon-reload
   sudo systemctl enable backend
   sudo systemctl start backend
   sudo systemctl status backend --no-pager | head -n 30

   명령어 설명
   sudo systemctl daemon-reload : .service 파일 수정되었을때 변경된 서비스 파일을 읽어 systemd 에 반영
   sudo systemctl enable backend : 서버 켜질때 backend.service 자동 실행
   sudo systemctl start backend : backend.service 를 읽어 ExecStart 명령 실행 => 프로세스 관리 시작
   sudo systemctl status backend --no-pager | head -n 30 : backend 상태 간략하게 확인
