로컬환경 실행 가이드 문서

FrontEnd 실행 방법

node 버전은 22.9.0, nvm 사용시 22.9.0 버전 받고 nvm use 22.9.0 실행
npm run dev 로 로컬서버 실행, 포트 : 5173

BackEnd 실행 방법

툴은 인텔리제이 사용 (사유 : JDK 수동 설정 안해줘도 됨)
인텔리제이 실행 > 좌측 상단 File > Settings > Gradle 검색 후 Gradle JVM 에서 Download JDK 선택
version : 21 선택 > Oracle OpenJDK (25.0.1) 선택 후 DOWNLOAD 버튼 클릭 (JDK21만 맞으면 됨, Vendor 는 아무거나 상관없음)
Gradle JVM 에 다운로드한 JVM 선택 후 APPLY => OK 클릭
우측 코끼리 새로고참 로고 클릭 또는 Gradle Clean => Build 실행

DB 세팅 방법 (일단 개인 로컬 PC 에서 실행, DB 서버는 추후 구축 예정)

마리아DB 다운 및 설치 (참고 링크 : https://timeboxstory.tistory.com/144)
DB포트(3306)에 대해서 로컬 PC 에 인바운드 규칙 추가 (참고 링크 : https://timeboxstory.tistory.com/11#google_vignette)
application.properties > spring.profiles.active=dev 세팅
applicaion-dev.properties > spring.datasource.url=jdbc:mariadb://localhost:3306/ensembleRoom (로컬호스트 안되면 개인 IP 주소로 변경) spring.datasource.username=root spring.datasource.password=root
DBeaver 설치 후 로컬 MariaDB 연결
ensembleRoom DB 생성
ERD 폴더 내 DDL 쿼리문 하나씩 실행하여 테이블 생성
