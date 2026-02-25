package com.gkjt.gkjt2026.model.dto;

import lombok.Data;
import java.util.Map;

/**
 * 专门用来接网关 HTTP JSON 的对象
 */
@Data
public class GatewayMessage {
    private String msgId;
    private String type;     // 对应 JSON 里的 type
    private String sn;       // 对应 JSON 里的 sn
    private String ip;
    private Long ts;
    private Map<String, Object> payload; // 对应 JSON 里的 payload
}
