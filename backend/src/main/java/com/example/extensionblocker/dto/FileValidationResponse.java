package com.example.extensionblocker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 파일 검증 응답 DTO
 * 파일 업로드 허용/차단 여부를 클라이언트에 반환
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileValidationResponse {

    /**
     * 업로드 허용 여부
     * true: 허용, false: 차단
     */
    private boolean allowed;

    /**
     * 파일의 확장자
     */
    private String extension;

    /**
     * 결과 사유 메시지
     */
    private String reason;
}
