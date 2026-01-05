package com.example.extensionblocker.service;

import com.example.extensionblocker.dto.PolicyResponse;
import com.example.extensionblocker.type.ExtensionType;

/**
 * 확장자 차단 서비스 인터페이스
 * 정책 관리 및 파일 검증 기능 정의
 */
public interface ExtensionService {

    /**
     * 특정 네임스페이스의 정책 조회
     * 
     * @param namespace 정책 네임스페이스 (예: "chat", "work")
     * @return 고정 확장자 및 커스텀 확장자 목록
     */
    PolicyResponse getPolicy(String namespace);

    void regExtensionRule(String namespace, ExtensionType type, String rawExtension);

    void delExtensionRule(Long id);

    /**
     * 파일 업로드 허용 여부 확인
     * 
     * @param filename  검증할 파일명
     * @param namespace 정책 네임스페이스
     * @return true: 허용, false: 차단
     */
    boolean isFileAllowed(String filename, String namespace);
}
