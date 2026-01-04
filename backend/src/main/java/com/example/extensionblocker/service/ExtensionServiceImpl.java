package com.example.extensionblocker.service;

import java.util.List;
import com.example.extensionblocker.constrant.ExtensionConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.extensionblocker.dto.PolicyResponse;
import com.example.extensionblocker.model.ExtensionPolicy;
import com.example.extensionblocker.model.ExtensionRule;
import com.example.extensionblocker.type.ExtensionType;
import com.example.extensionblocker.mapper.ExtensionPolicyMapper;
import com.example.extensionblocker.mapper.ExtensionRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;

/**
 * 확장자 차단 서비스 구현체
 * 정책 관리, 고정/커스텀 확장자 CRUD, 파일 검증 등의 기능 제공
 */
@Service
@RequiredArgsConstructor
public class ExtensionServiceImpl implements ExtensionService {

    private static final Logger log = LoggerFactory.getLogger(ExtensionServiceImpl.class);

    private final ExtensionPolicyMapper policyMapper;
    private final ExtensionRuleMapper ruleMapper;

    /**
     * 애플리케이션 시작 시 초기화
     * 여러 시나리오(chat, work)의 기본 정책과 고정 확장자를 설정
     */
    @PostConstruct
    @Transactional
    public void init() {
        // 여러 시나리오 초기화
        initPolicyIfNotExists("chat", "Chat Feature Policy");
        initPolicyIfNotExists("work", "Work Sharing Feature Policy");
    }

    /**
     * 특정 네임스페이스의 정책이 없으면 새로 생성하고 고정 확장자 초기화
     * 
     * @param namespace   정책 네임스페이스
     * @param description 정책 설명
     */
    private void initPolicyIfNotExists(String namespace, String description) {
        log.info("[init] Initializing policy for namespace={}", namespace);

        // 정책 초기화
        ExtensionPolicy policy = policyMapper.findByNamespace(namespace)
                .orElseGet(() -> {
                    ExtensionPolicy newPolicy = new ExtensionPolicy(namespace, description);
                    policyMapper.save(newPolicy);
                    log.info("[init] Created new policy: namespace={}, id={}", namespace, newPolicy.getId());
                    return newPolicy;
                });

        // 고정 확장자 7개 초기화 (기본값: 비활성화)
        for (String ext : ExtensionConst.DEFAULT_FIXED_EXTENSIONS) {
            if (ruleMapper.findByPolicyIdAndExtension(policy.getId(), ext).isEmpty()) {
                ExtensionRule rule = new ExtensionRule(policy.getId(), ext, ExtensionType.FIXED);
                rule.setActive(false); // 요구사항: 기본은 체크 해제 상태
                ruleMapper.save(rule);
            }
        }

        log.info("[init] Policy initialization complete for namespace={}", namespace);
    }

    /**
     * 정책 조회
     * 고정 확장자와 커스텀 확장자 목록 반환
     * 
     * @param namespace 정책 네임스페이스
     * @return 고정/커스텀 확장자 목록
     */
    @Transactional(readOnly = true)
    public PolicyResponse getPolicy(String namespace) {
        ExtensionPolicy policy = getPolicyOrThrow(namespace);
        List<ExtensionRule> rules = ruleMapper.findByPolicyId(policy.getId());

        // 고정 확장자 목록 생성
        List<PolicyResponse.FixedExtensionDto> fixed = rules.stream()
                .filter(r -> r.getType() == ExtensionType.FIXED)
                .map(r -> new PolicyResponse.FixedExtensionDto(r.getExtension(), r.isActive()))
                .collect(Collectors.toList());

        // 커스텀 확장자 목록 생성
        List<PolicyResponse.CustomExtensionDto> custom = rules.stream()
                .filter(r -> r.getType() == ExtensionType.CUSTOM)
                .map(r -> new PolicyResponse.CustomExtensionDto(r.getId(), r.getExtension(), r.isActive()))
                .collect(Collectors.toList());

        return new PolicyResponse(fixed, custom);
    }

    /**
     * 고정 확장자의 활성화 상태 토글 (차단/허용 전환)
     * 
     * @param namespace 정책 네임스페이스
     * @param extension 토글할 확장자명
     */
    @Transactional
    public void toggleFixed(String namespace, String extension) {
        log.info("[toggleFixed] START - namespace={}, extension={}", namespace, extension);

        ExtensionPolicy policy = getPolicyOrThrow(namespace);
        log.debug("[toggleFixed] Found policy id={}", policy.getId());

        ExtensionRule rule = ruleMapper.findByPolicyIdAndExtension(policy.getId(), extension)
                .orElseThrow(() -> new IllegalArgumentException("Fixed extension not found: " + extension));

        log.debug("[toggleFixed] Found rule id={}, current isActive={}", rule.getId(), rule.isActive());

        if (rule.getType() != ExtensionType.FIXED) {
            throw new IllegalArgumentException("Target is not a fixed extension");
        }

        // 상태 토글 및 업데이트
        boolean newStatus = !rule.isActive();
        log.info("[toggleFixed] Updating rule id={} from isActive={} to isActive={}",
                rule.getId(), rule.isActive(), newStatus);

        ruleMapper.updateActiveStatus(rule.getId(), newStatus);
        log.info("[toggleFixed] SUCCESS - Updated extension={} to isActive={}", extension, newStatus);
    }

    @Transactional
    public void addCustom(String namespace, String rawExtension) {
        log.info("[addCustom] START - namespace={}, rawExtension={}", namespace, rawExtension);

        ExtensionPolicy policy = getPolicyOrThrow(namespace);
        log.debug("[addCustom] Found policy id={}", policy.getId());

        // 1. Normalize
        String extension = normalize(rawExtension);

        // 2. Validate Format
        if (extension.length() > ExtensionConst.MAX_EXTENSION_LENGTH) {
            throw new IllegalArgumentException(
                    "Extension length cannot exceed " + ExtensionConst.MAX_EXTENSION_LENGTH + " characters");
        }
        if (!ExtensionConst.VALID_EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new IllegalArgumentException("Invalid extension format (only a-z, 0-9 allowed)");
        }

        // 3. Conflict Check (Fixed vs Custom)
        if (ExtensionConst.DEFAULT_FIXED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Extension is defined in fixed list. Please toggle it there.");
        }

        // Check DB for duplicates (Fixed or Custom)
        if (ruleMapper.findByPolicyIdAndExtension(policy.getId(), extension).isPresent()) {
            throw new IllegalArgumentException("Extension already exists");
        }

        // 4. Limit Check
        long customCount = ruleMapper.countByPolicyIdAndType(policy.getId(), ExtensionType.CUSTOM);
        if (customCount >= ExtensionConst.MAX_CUSTOM_EXTENSIONS) {
            throw new IllegalArgumentException(
                    "Max custom extensions limit (" + ExtensionConst.MAX_CUSTOM_EXTENSIONS + ") reached");
        }

        // 5. Save
        ExtensionRule rule = new ExtensionRule(policy.getId(), extension, ExtensionType.CUSTOM);
        rule.setActive(true); // Custom extensions are active by default

        log.info("[addCustom] Saving custom extension={}, policyId={}, isActive=true", extension, policy.getId());

        ruleMapper.save(rule);
        log.info("[addCustom] SUCCESS - Saved custom extension={} with id={}", extension, rule.getId());
    }

    @Transactional
    public void deleteCustom(Long id) {
        log.info("[deleteCustom] START - id={}", id);

        ExtensionRule rule = ruleMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found"));

        log.debug("[deleteCustom] Found rule id={}, extension={}, type={}",
                rule.getId(), rule.getExtension(), rule.getType());

        if (rule.getType() != ExtensionType.CUSTOM) {
            throw new IllegalArgumentException("Cannot delete fixed rule");
        }

        log.info("[deleteCustom] Deleting custom extension={}", rule.getExtension());
        ruleMapper.deleteById(id);
        log.info("[deleteCustom] SUCCESS - Deleted extension={}", rule.getExtension());
    }

    private ExtensionPolicy getPolicyOrThrow(String namespace) {
        return policyMapper.findByNamespace(namespace)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + namespace));
    }

    private String normalize(String input) {
        if (input == null)
            return "";
        String clean = input.trim().toLowerCase();
        if (clean.startsWith(".")) {
            clean = clean.substring(1);
        }
        return clean;
    }

    @Transactional(readOnly = true)
    public boolean isFileAllowed(String filename, String namespace) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        // Extract extension
        String extension = extractExtension(filename);
        if (extension.isEmpty()) {
            return true; // No extension = allowed
        }

        // Get policy
        ExtensionPolicy policy = policyMapper.findByNamespace(namespace).orElse(null);
        if (policy == null) {
            return true; // No policy = allowed
        }

        // Check if extension is actively blocked
        return ruleMapper.findByPolicyIdAndExtension(policy.getId(), extension)
                .map(rule -> !rule.isActive()) // If rule exists and is active, block (return false)
                .orElse(true); // If no rule exists, allow
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
