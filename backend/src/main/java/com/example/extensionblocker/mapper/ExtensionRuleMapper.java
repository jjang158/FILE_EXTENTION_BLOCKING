package com.example.extensionblocker.mapper;

import com.example.extensionblocker.model.ExtensionRule;
import com.example.extensionblocker.type.ExtensionType;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

/**
 * 확장자 규칙 MyBatis Mapper 인터페이스
 * 고정/커스텀 확장자 규칙의 CRUD 작업을 담당
 */
@Mapper
public interface ExtensionRuleMapper {

    /**
     * 규칙 저장
     * 
     * @param rule 저장할 규칙 객체
     */
    void save(ExtensionRule rule);

    /**
     * 특정 정책의 모든 규칙 조회
     * 
     * @param policyId 정책 ID
     * @return 규칙 목록
     */
    List<ExtensionRule> findByPolicyId(@Param("policyId") Long policyId);

    /**
     * 정책 ID와 확장자명으로 규칙 조회
     * 
     * @param policyId  정책 ID
     * @param extension 확장자명
     * @return 규칙 객체 (Optional)
     */
    Optional<ExtensionRule> findByPolicyIdAndExtension(@Param("policyId") Long policyId,
            @Param("extension") String extension);

    /**
     * 특정 정책의 특정 타입 규칙 개수 조회
     * 
     * @param policyId 정책 ID
     * @param type     확장자 타입 (FIXED 또는 CUSTOM)
     * @return 규칙 개수
     */
    long countByPolicyIdAndType(@Param("policyId") Long policyId, @Param("type") ExtensionType type);

    /**
     * 규칙의 활성화 상태 업데이트
     * 
     * @param id       규칙 ID
     * @param isActive 활성화 여부
     */
    void updateActiveStatus(@Param("id") Long id, @Param("isActive") boolean isActive);

    /**
     * ID로 규칙 조회
     * 
     * @param id 규칙 ID
     * @return 규칙 객체 (Optional)
     */
    Optional<ExtensionRule> findById(@Param("id") Long id);

    /**
     * 규칙 삭제
     * 
     * @param id 삭제할 규칙 ID
     */
    void deleteById(@Param("id") Long id);
}
