package com.example.extensionblocker.type;

/**
 * 확장자 타입 열거형
 * FIXED: 고정 확장자 (시스템 정의)
 * CUSTOM: 커스텀 확장자 (사용자 정의)
 */
public enum ExtensionType {
    /**
     * 고정 확장자 (관리자가 미리 정의한 7개)
     */
    FIXED,

    /**
     * 커스텀 확장자 (사용자가 직접 추가)
     */
    CUSTOM
}
