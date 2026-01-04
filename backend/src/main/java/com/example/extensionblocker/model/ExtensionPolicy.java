package com.example.extensionblocker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 확장자 차단 정책 모델
 * 각 시나리오(채팅, 업무 공유 등)별로 독립적인 정책을 관리
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionPolicy {

    /**
     * 정책 고유 ID
     */
    private Long id;

    /**
     * 정책 네임스페이스 (예: "chat", "work")
     * 각 기능별 정책을 구분하는 식별자
     */
    private String namespace;

    /**
     * 정책 설명
     */
    private String description;

    /**
     * ID를 제외한 생성자
     * 
     * @param namespace   정책 네임스페이스
     * @param description 정책 설명
     */
    public ExtensionPolicy(String namespace, String description) {
        this.namespace = namespace;
        this.description = description;
    }
}
