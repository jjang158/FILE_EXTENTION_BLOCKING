package com.example.extensionblocker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 정책 조회 응답 DTO
 * 고정 확장자와 커스텀 확장자 목록을 클라이언트에 반환
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {

    /**
     * 고정 확장자 목록
     */
    private List<ExtensionDto> fixed;

    /**
     * 커스텀 확장자 목록
     */
    private List<ExtensionDto> custom;

}
