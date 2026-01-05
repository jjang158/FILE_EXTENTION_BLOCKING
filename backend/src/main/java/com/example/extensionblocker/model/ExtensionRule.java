package com.example.extensionblocker.model;

import com.example.extensionblocker.type.ExtensionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 확장자 차단 규칙 모델
 * 각 정책에 속하는 개별 확장자 규칙을 정의
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionRule {

    /**
     * 규칙 고유 ID
     */
    private Long id;

    /**
     * 소속 정책 ID
     */
    private Long policyId;

    /**
     * 확장자명 (소문자, 점 제외)
     */
    private String extension;

    /**
     * 확장자 타입 (FIXED: 고정, CUSTOM: 커스텀)
     */
    private ExtensionType type;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 새 규칙 생성 시 사용하는 생성자
     * 
     * @param policyId  소속 정책 ID
     * @param extension 확장자명
     * @param type      확장자 타입
     */
    public ExtensionRule(Long policyId, String extension, ExtensionType type) {
        this.policyId = policyId;
        this.extension = extension;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}
