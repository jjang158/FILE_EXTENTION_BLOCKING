package com.example.extensionblocker.mapper;

import com.example.extensionblocker.model.ExtensionPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 확장자 정책 MyBatis Mapper 인터페이스
 * 정책 CRUD 작업을 담당
 */
@Mapper
public interface ExtensionPolicyMapper {

    /**
     * 정책 저장
     * 
     * @param policy 저장할 정책 객체
     */
    void regExtensionPolicy(ExtensionPolicy policy);

    /**
     * 네임스페이스로 정책 조회
     * 
     * @param namespace 정책 네임스페이스 (예: "chat", "work")
     * @return 정책 객체 (Optional)
     */
    Optional<ExtensionPolicy> getPolicyByNamespace(@Param("namespace") String namespace);
}
