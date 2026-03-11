package kr.co.wikibook.backend.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.event.ExceptionEvent;
import kr.co.wikibook.backend.account.mapper.KakaoLoginMapper;
import kr.co.wikibook.backend.member.model.KakaoToken;
import kr.co.wikibook.backend.member.model.Members;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Service
public class KaKaoLoginService {
    @Autowired
    KakaoLoginMapper kakaoLoginMapper;

    @Autowired
    AccountService accountService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;


    public String requestToken(String code) throws MalformedURLException {
        String access_Token = "";
        String refresh_Token = "";

        String strUrl = "https://kauth.kakao.com/oauth/token"; // 토큰 요청 보낼 주소
        KakaoToken kakaoToken = new KakaoToken(); // 요청받을 객체

        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // url Http 연결 생성

            // POST 요청
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);// outputStreamm으로 post 데이터를 넘김

            // 파라미터 세팅
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();

            // 0번 파라미터 grant_type. -> authorization_code로 고정
            sb.append("grant_type=authorization_code");

            // 1번 파라미터 client_id
//             sb.append("&client_id=0665d3f6aefd487f0c3e9bba86b68e27");
//
//             // 2번 파라미터 redirect_uri
//             sb.append("&redirect_uri=http://localhost:5173/kakaoLogin");
//
//             // 3번 파라미터 code
//             sb.append("&code=" + code);
//
//             sb.append("&client_secret=mSLgD3pLaNemoIlo53QJhDRv0O9IjWNu");

            // ✅ (변경) client_id 하드코딩 제거
            sb.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));

            // ✅ (변경) redirect_uri 하드코딩 제거 + 인코딩
            sb.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));

            // ✅ (변경) code 인코딩 (안 하면 가끔 깨짐)
            sb.append("&code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));

            // ✅ (변경) client_secret 하드코딩 제거
            sb.append("&client_secret=").append(URLEncoder.encode(clientSecret, StandardCharsets.UTF_8));


            bw.write(sb.toString());
            bw.flush();// 실제 요청을 보내는 부분

            int responseCode = conn.getResponseCode();

            // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            // BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

              // ✅ (최소 변경 권장) 실패 시에도 메시지 읽게끔
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream()
                    ));

            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            ObjectMapper mapper = new ObjectMapper();

            kakaoToken = mapper.readValue(result, KakaoToken.class);
            access_Token = kakaoToken.getAccess_token();
            refresh_Token = kakaoToken.getRefresh_token();

            br.close();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return access_Token;
    }





        public String requestUser(String accessToken) throws Exception {
            String strUrl = "https://kapi.kakao.com/v2/user/me"; // request를 보낼 주소

            String memberId = "";

            try {
                URL url = new URL(strUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // url Http 연결 생성

                // POST 요청
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);// outputStreamm으로 post 데이터를 넘김

                // 전송할 header 작성, 인자로 받은 access_token전송
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                // 실제 요청을 보내는 부분, 결과 코드가 200이라면 성공
                int responseCode = conn.getResponseCode();

                // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                String result = "";

                while ((line = br.readLine()) != null) {
                    result += line;
                }
                br.close();

                ObjectMapper mapper = new ObjectMapper();

                HashMap<String, Object> resultMap = mapper.readValue(result, HashMap.class);
                HashMap<String, Object> properties = (HashMap<String, Object>) resultMap.get("properties");
                String id = String.valueOf((Long) resultMap.get("id"));

                String kakaoLoginId = kakaoLoginMapper.getKaKaoUserLoginId(id);
                if(kakaoLoginId!=null){
                    memberId = kakaoLoginId;
                }
                else {
                    Members member = new Members();
                    String randomId = UUID.randomUUID().toString();
                    member.setName( properties.get("nickname").toString());
                    member.setLoginId(randomId);
                    member.setLoginPw(UUID.randomUUID().toString());
                    member.setManager(false);
                    member.setOauthProvider("kakao");
                    member.setOauthId(id);

                    kakaoLoginMapper.joinWithKakao(member);

                    member = accountService.findMemberByLoginId(randomId);

                    memberId = member.getId().toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return memberId;
    }
}
