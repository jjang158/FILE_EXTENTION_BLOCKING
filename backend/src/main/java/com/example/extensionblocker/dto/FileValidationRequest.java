package com.example.extensionblocker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 파일 검증 요청 DTO
 * 클라이언트에서 파일 업로드 가능 여부를 확인할 때 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileValidationRequest {

    /**
     * 검증할 파일명 (확장자 포함)
     */
    private String filename;

    /**
     * 정책 네임스페이스 (예: "chat", "work")
     */
    private String namespace;
}
