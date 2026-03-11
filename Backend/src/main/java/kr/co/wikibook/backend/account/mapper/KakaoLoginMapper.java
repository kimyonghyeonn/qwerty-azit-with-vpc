package kr.co.wikibook.backend.account.mapper;

import kr.co.wikibook.backend.member.model.Members;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
@Mapper
public interface KakaoLoginMapper {
    String getKaKaoUserLoginId(String kakaoId) throws Exception;

    int joinWithKakao(Members member) throws Exception;
}
