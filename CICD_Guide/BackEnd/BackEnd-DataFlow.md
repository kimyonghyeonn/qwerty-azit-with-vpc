VPC 구축 이후 데이터 흐름 정리 (BackEnd)
FrontEnd-DataFlow 를 바탕으로 보자면 VPC 엔드포인트인 로드밸런서로 진입 => 웹서버에서 api 요청을 앱서버에서 받게 된다.
이떄 앱서버의 EC2 는 ALB 보안그룹을 공유하며 TCP 8080, 22 포트에 대해 인바운드를 허용한다.
아웃바운드는 All traffic / 0.0.0.0 으로 하여 어디든 나갈수 있게 하여 DB, 외부 API, OAuth 접속을 가능하기 위해 설정한다.

전체적인 흐름 개요는 다음과 같다.
User
↓
DNS (qwerty-azit.com)
↓
ALB / Web Server
↓
App Server (Spring Boot 8080)
↓
DB Server (MariaDB)
↓
App Server
↓
Web Server
↓
User
