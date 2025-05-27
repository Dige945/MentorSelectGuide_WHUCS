package com.example.whucs_mentorguide.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

/**
 * DeepSeek AI 聊天控制器
 * 提供流式聊天接口和上下文管理功能
 */
@RestController
@RequestMapping("deepSeek")
public class OpenAIController {

    private static final Logger log = LoggerFactory.getLogger(OpenAIController.class);

    // DeepSeek API 密钥（从配置文件中注入）
    @Value("${ai.config.deepseek.apiKey}")
    private String DEEPSEEK_API_KEY;

    // DeepSeek API 基础地址（从配置文件中注入）
    @Value("${ai.config.deepseek.baseUrl}")
    private String DEEPSEEK_API_BASE_URL;
    
    // Kimi API 密钥（从配置文件中注入）
    @Value("${ai.config.kimi.apiKey}")
    private String KIMI_API_KEY;

    // Kimi API 基础地址（从配置文件中注入）
    @Value("${ai.config.kimi.baseUrl}")
    private String KIMI_API_BASE_URL;

    // 完整的API URL
    private String getApiUrl() {
        return KIMI_API_BASE_URL + "/chat/completions";
    }

    // 系统提示词（从配置文件中注入）
    @Value("${ai.systemPrompt}")
    private String SYSTEM_PROMPT;

    /**
     * 用户会话历史存储
     * Key: 用户ID
     * Value: 消息列表（按时间顺序存储）
     *
     * 注意：此实现为内存存储，重启服务会丢失历史记录
     * 生产环境应考虑持久化方案（如Redis）
     */
    private final Map<String, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();

    // AI推荐使用次数限制缓存
    // Key: IP-日期, Value: 已使用次数
    private static final Map<String, Integer> recommendLimitCache = new ConcurrentHashMap<>();
    
    // 研究方向介绍请求限制缓存
    // Key: IP-方向名称, Value: true(已请求过)
    private static final Map<String, Boolean> directionIntroCache = new ConcurrentHashMap<>();
    
    // 上次清理缓存的日期
    private static LocalDate lastCleanupDate = LocalDate.now();
    
    // 每天最大调用次数
    private static final int MAX_RECOMMEND_CALLS_PER_DAY = 2;

    // 异步任务线程池（缓存线程池，适合短生命周期的任务）
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // JSON处理器（线程安全）
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 聊天页面渲染
     * @param modelAndView Spring MVC模型视图对象
     * @return 返回chat视图
     */
    @GetMapping()
    public ModelAndView chat(ModelAndView modelAndView) {
        modelAndView.setViewName("chat"); // 对应resources/templates/chat.html
        return modelAndView;
    }

    // 获取客户端IP
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，第一个IP为真实IP
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
    
    // 清理过期缓存记录（不同日期的）
    private void cleanupExpiredRecords() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastCleanupDate)) {
            recommendLimitCache.clear();
            // 不清理directionIntroCache，因为我们希望每个方向只查询一次
            lastCleanupDate = today;
            log.info("已清理过期AI推荐限制记录");
        }
    }
    
    // 检查是否达到每日调用限制
    private boolean checkRecommendLimit(String clientIp) {
        cleanupExpiredRecords();
        
        String today = LocalDate.now().toString();
        String limitKey = clientIp + "-" + today;
        
        Integer count = recommendLimitCache.getOrDefault(limitKey, 0);
        boolean withinLimit = count < MAX_RECOMMEND_CALLS_PER_DAY;
        
        if (withinLimit) {
            // 更新计数
            recommendLimitCache.put(limitKey, count + 1);
            log.info("更新AI推荐使用次数：{} -> {}/{}", clientIp, count + 1, MAX_RECOMMEND_CALLS_PER_DAY);
        } else {
            log.info("用户已达到每日AI推荐使用上限：{} -> {}/{}", clientIp, count, MAX_RECOMMEND_CALLS_PER_DAY);
        }
        
        return withinLimit;
    }

    /**
     * 流式聊天接口
     * @param question 用户提问内容（JSON字符串格式）
     * @return SSE事件流发射器
     *
     * 协议说明：
     * 1. 使用Server-Sent Events(SSE)协议实现流式响应
     * 2. 响应Content-Type为text/event-stream
     * 3. 客户端需要按照SSE协议解析响应
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody String question) {
        SseEmitter emitter = new SseEmitter(-1L);
        
        executorService.execute(() -> {
            try {
                // 构建消息
                List<Map<String, String>> messages = new ArrayList<>();
                
                // 添加系统消息
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", SYSTEM_PROMPT);
                messages.add(systemMessage);
                
                // 添加用户消息
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", question);
                messages.add(userMessage);
                
                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "moonshot-v1-8k");
                requestBody.put("messages", messages);
                requestBody.put("stream", true);
                
                // 发送请求并处理响应
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpPost request = new HttpPost(getApiUrl());
                    request.setHeader("Content-Type", "application/json");
                    request.setHeader("Authorization", "Bearer " + KIMI_API_KEY);
                    
                    String jsonBody = objectMapper.writeValueAsString(requestBody);
                    request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
                    
                    try (CloseableHttpResponse response = client.execute(request)) {
                        if (response.getStatusLine().getStatusCode() != 200) {
                            throw new RuntimeException("API request failed with status: " + 
                                response.getStatusLine().getStatusCode());
                        }
                        
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent())
                        );
                        
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if ("[DONE]".equals(data)) {
                                    break;
                                }
                                
                                JsonNode node = objectMapper.readTree(data);
                                String content = node.path("choices")
                                                   .path(0)
                                                   .path("delta")
                                                   .path("content")
                                                   .asText("");
                                
                                if (!content.isEmpty()) {
                                    emitter.send(content);
                                }
                            }
                        }
                    }
                }
                
                emitter.complete();
                
            } catch (Exception e) {
                log.error("Chat processing error", e);
                try {
                    emitter.send("处理请求时发生错误，请稍后重试");
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
        
        return emitter;
    }

    @PostMapping(value = "/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recommend(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String userId = "123";
        SseEmitter emitter = new SseEmitter(-1L);

        log.info("接收到推荐请求，请求内容: {}", request);
        log.info("API URL: {}", getApiUrl());
        log.info("API Key: {}", KIMI_API_KEY.substring(0, 5) + "...");

        executorService.execute(() -> {
            try {
                // 获取客户端IP并检查使用限制
                String clientIp = getClientIp(httpRequest);
                log.info("客户端IP: {}", clientIp);
                
                if (!checkRecommendLimit(clientIp)) {
                    emitter.send("您今天的AI推荐次数已用完（每天限制" + MAX_RECOMMEND_CALLS_PER_DAY + "次），请明天再试。");
                    emitter.complete();
                    return;
                }
                
                log.info("开始处理推荐请求，请求内容: {}", request);

                List<Map<String, String>> messages = sessionHistory.getOrDefault(userId, new ArrayList<>());

                // 构造系统提示词
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", buildSystemPrompt(request));

                // 构造用户消息
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", buildUserPrompt(request));

                messages.add(systemMessage);
                messages.add(userMessage);

                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpPost httpPost = new HttpPost(getApiUrl());
                    httpPost.setHeader("Content-Type", "application/json");
                    httpPost.setHeader("Authorization", "Bearer " + KIMI_API_KEY);

                    Map<String, Object> requestMap = new HashMap<>();
                    requestMap.put("model", "moonshot-v1-32k");
                    requestMap.put("messages", messages);
                    requestMap.put("stream", true);

                    String requestBody = objectMapper.writeValueAsString(requestMap);
                    httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                    log.info("发送到Kimi API的请求URL: {}", getApiUrl());
                    log.info("发送到Kimi API的请求头: {}", Arrays.toString(httpPost.getAllHeaders()));
                    log.info("发送到Kimi API的请求体: {}", requestBody);

                    try (CloseableHttpResponse response = client.execute(httpPost)) {
                        int statusCode = response.getStatusLine().getStatusCode();
                        log.info("Kimi API响应状态码: {}", statusCode);

                        if (statusCode != 200) {
                            String errorMessage = "Kimi API返回错误状态码: " + statusCode;
                            log.error(errorMessage);
                            emitter.send(errorMessage);
                            emitter.complete();
                            return;
                        }

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

                            StringBuilder aiResponse = new StringBuilder();
                            String line;

                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("data: ")) {
                                    String jsonData = line.substring(6);

                                    if ("[DONE]".equals(jsonData)) {
                                        break;
                                    }

                                    JsonNode node = objectMapper.readTree(jsonData);
                                    String content = node.path("choices")
                                            .path(0)
                                            .path("delta")
                                            .path("content")
                                            .asText("");

                                    if (!content.isEmpty()) {
                                        emitter.send(content);
                                        aiResponse.append(content);
                                    }
                                }
                            }

                            // 更新历史记录
                            Map<String, String> aiMessage = new HashMap<>();
                            aiMessage.put("role", "assistant");
                            aiMessage.put("content", aiResponse.toString());

                            messages.add(aiMessage);
                            sessionHistory.put(userId, messages);

                            log.info("推荐请求处理完成");
                            emitter.complete();
                        }
                    } catch (Exception e) {
                        log.error("Kimi API请求处理异常", e);
                        try {
                            // 发送错误消息到前端
                            emitter.send("AI推荐服务暂时不可用，请稍后再试。错误信息: " + e.getMessage());
                            emitter.complete();
                        } catch (Exception ex) {
                            emitter.completeWithError(e);
                        }
                    }
                } catch (Exception e) {
                    log.error("推荐请求处理流程异常", e);
                    emitter.completeWithError(e);
                }
            } catch (Exception e) {
                log.error("推荐请求处理流程异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
    
    /**
     * 检查用户今天是否还可以使用AI推荐功能
     * @param request HTTP请求，用于获取客户端IP
     * @return 包含是否可用和剩余次数的结果
     */
    @GetMapping("/recommend/can-use")
    public Map<String, Object> canUseRecommend(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 清理过期记录
            cleanupExpiredRecords();
            
            // 获取客户端IP
            String clientIp = getClientIp(request);
            String today = LocalDate.now().toString();
            String limitKey = clientIp + "-" + today;
            
            // 获取已使用次数
            Integer usedCount = recommendLimitCache.getOrDefault(limitKey, 0);
            boolean canUse = usedCount < MAX_RECOMMEND_CALLS_PER_DAY;
            int remainingCount = MAX_RECOMMEND_CALLS_PER_DAY - usedCount;
            
            result.put("success", true);
            result.put("canUse", canUse);
            result.put("remainingCount", remainingCount);
            result.put("maxCount", MAX_RECOMMEND_CALLS_PER_DAY);
            
            log.info("用户AI推荐使用情况检查: IP={}, 已用={}, 剩余={}", clientIp, usedCount, remainingCount);
            
        } catch (Exception e) {
            log.error("检查AI推荐使用限制时发生错误", e);
            result.put("success", false);
            result.put("canUse", true); // 出错时默认允许使用，后端会再次验证
            result.put("message", "检查失败: " + e.getMessage());
        }
        
        return result;
    }    private String buildSystemPrompt(Map<String, Object> request) {
        return "你是一个专业的导师推荐系统。请根据用户的自我描述、选择的标签和偏好，推荐最适合的导师。\n\n" +
               "分析用户的自我描述，结合用户的标签选择和偏好，提供更加个性化的推荐。\n\n" +
               "请按照以下格式返回推荐结果：\n\n" +
               "## 推荐导师列表\n\n" +
               "### 1. [导师姓名]\n\n" +
               "- 职称：[职称]\n\n" +
               "- 所属院系：[院系]\n\n" +
               "- 研究方向：[研究方向]\n\n" +
               "- 推荐理由：[详细推荐理由，包括与用户自我描述的契合点]\n\n" +
               "请确保推荐理由具体说明与用户需求和用户自我描述的匹配点。每个导师的信息之间请用两个换行符分隔。";
    }

    private String buildUserPrompt(Map<String, Object> request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下信息推荐导师：\n\n");

        // 添加标签信息
        if (request.containsKey("tags") && request.get("tags") instanceof List) {
            List<String> tags = (List<String>) request.get("tags");
            if (!tags.isEmpty()) {
                prompt.append("研究方向：").append(String.join("、", tags)).append("\n\n");
            }
        }

        // 添加用户偏好
        if (request.containsKey("preferences") && request.get("preferences") instanceof String) {
            String preferences = (String) request.get("preferences");
            if (!preferences.isEmpty()) {
                prompt.append("用户偏好：").append(preferences).append("\n\n");
            }
        }
          // 添加用户画像信息（仅考虑个人描述）
        if (request.containsKey("userProfile") && request.get("userProfile") instanceof Map) {
            Map<String, Object> userProfile = (Map<String, Object>) request.get("userProfile");
            if (!userProfile.isEmpty() && userProfile.containsKey("personImage") && userProfile.get("personImage") != null) {
                String personImage = userProfile.get("personImage").toString();
                if (!personImage.isEmpty()) {
                    prompt.append("用户自我描述：").append(personImage).append("\n\n");
                }
            }
        }

        // 添加教师信息
        if (request.containsKey("teachers") && request.get("teachers") instanceof List) {
            List<Map<String, Object>> teachers = (List<Map<String, Object>>) request.get("teachers");
            prompt.append("可选的教师列表：\n\n");
            for (Map<String, Object> teacher : teachers) {
                prompt.append("- ").append(teacher.get("name")).append("（")
                        .append(teacher.get("title")).append("，")
                        .append(teacher.get("department")).append("，")
                        .append(teacher.get("research_area")).append("）\n\n");
            }
        }

        prompt.append("请根据以上信息，特别是用户画像和用户偏好，推荐最适合的导师，并说明推荐理由。");
        return prompt.toString();
    }

    @PostMapping("/direction/intro")
    public SseEmitter getDirectionIntro(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        SseEmitter emitter = new SseEmitter();
        
        executorService.execute(() -> {
            try {
                // 获取当前方向名称
                String directionName = request.get("direction");
                if (directionName == null || directionName.trim().isEmpty()) {
                    emitter.send("方向名称不能为空");
                    emitter.complete();
                    return;
                }
                
                // 获取客户端IP
                String clientIp = getClientIp(httpRequest);
                String cacheKey = clientIp + "-" + directionName;
                
                // 检查是否已经请求过这个方向
                if (directionIntroCache.containsKey(cacheKey)) {
                    log.info("用户已请求过该研究方向介绍: IP={}, 方向={}", clientIp, directionName);
                    emitter.send("您已经查询过这个研究方向的介绍，请尝试其他方向。");
                    emitter.complete();
                    return;
                }
                
                // 记录该IP已请求过这个方向
                directionIntroCache.put(cacheKey, true);
                log.info("记录新的研究方向介绍请求: IP={}, 方向={}", clientIp, directionName);
                
                // 构建消息
                List<Map<String, String>> messages = new ArrayList<>();
                
                // 添加系统消息
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", 
                    "你是一个专业的研究方向介绍助手。请用专业且易懂的方式介绍研究方向，要求：\n" +
                    "- 使用markdown格式\n" +
                    "- 按照基本概念、主要研究内容、应用场景、最新研究热点等方面展开\n" +
                    "- 不要使用数字编号，改用描述性的小标题\n" +
                    "- 语言要专业且平实，避免过于学术化的表达\n" +
                    "- 确保内容的逻辑性和连贯性\n" +
                    "- 适当使用换行来提高可读性\n" +
                    "- 内容大约150字。"
                );
                messages.add(systemMessage);
                
                // 添加用户消息
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", 
                    "请介绍" + directionName + "这个研究方向。\n" +
                    "要求：\n" +
                    "- 从基础概念开始，循序渐进地展开介绍\n" +
                    "- 重点突出该方向的研究价值和应用前景\n" +
                    "- 提到一些最新的研究进展和未来趋势"
                );
                messages.add(userMessage);
                
                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "moonshot-v1-8k");
                requestBody.put("messages", messages);
                requestBody.put("stream", true);
                
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpPost httpPost = new HttpPost(getApiUrl());
                    httpPost.setHeader("Content-Type", "application/json");
                    httpPost.setHeader("Authorization", "Bearer " + KIMI_API_KEY);
                    
                    String requestBodyJson = objectMapper.writeValueAsString(requestBody);
                    httpPost.setEntity(new StringEntity(requestBodyJson, StandardCharsets.UTF_8));
                    
                    try (CloseableHttpResponse response = client.execute(httpPost)) {
                        if (response.getStatusLine().getStatusCode() != 200) {
                            throw new RuntimeException("Kimi API request failed with status: " + 
                                response.getStatusLine().getStatusCode());
                        }
                        
                        StringBuilder messageBuilder = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                            
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("data: ")) {
                                    String content = line.substring(6).trim();
                                    if ("[DONE]".equals(content)) {
                                        break;
                                    }
                                    
                                    try {
                                        JsonNode node = objectMapper.readTree(content);
                                        String text = node.path("choices")
                                                       .path(0)
                                                       .path("delta")
                                                       .path("content")
                                                       .asText("");
                                        
                                        if (!text.isEmpty()) {
                                            // Remove "data:" prefix and handle line breaks
                                            text = text.replace("data:", "").trim();
                                            messageBuilder.append(text);
                                            
                                            // Send complete sentences or paragraphs
                                            if (text.endsWith(".") || text.endsWith("。") || 
                                                text.endsWith("\n") || text.endsWith("：")) {
                                                emitter.send(messageBuilder.toString());
                                                messageBuilder.setLength(0);
                                            }
                                        }
                                    } catch (JsonProcessingException e) {
                                        log.error("Error processing JSON response", e);
                                    }
                                }
                            }
                            
                            // Send any remaining content
                            if (messageBuilder.length() > 0) {
                                emitter.send(messageBuilder.toString());
                            }
                        }
                    }
                }
                
                emitter.complete();
                
            } catch (Exception e) {
                log.error("处理研究方向介绍请求失败", e);
                try {
                    emitter.send("获取研究方向介绍失败，请稍后重试");
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
        
        return emitter;
    }

    private void handleStreamResponse(Map<String, Object> requestBody, SseEmitter emitter) {
        StringBuilder messageBuilder = new StringBuilder();
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(getApiUrl());
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + KIMI_API_KEY);

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(requestBodyJson, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    String errorMessage = "Kimi API返回错误状态码: " + statusCode;
                    log.error(errorMessage);
                    emitter.send(errorMessage);
                    emitter.complete();
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String jsonData = line.substring(6);
                            
                            if ("[DONE]".equals(jsonData)) {
                                break;
                            }

                            JsonNode node = objectMapper.readTree(jsonData);
                            String content = node.path("choices")
                                    .path(0)
                                    .path("delta")
                                    .path("content")
                                    .asText("");

                            if (!content.isEmpty()) {
                                messageBuilder.append(content);
                                
                                // 当收集到一个完整的段落或句子时发送
                                if (content.contains(".") || content.contains("。") || 
                                    content.contains("\n") || content.contains("；")) {
                                    String message = messageBuilder.toString();
                                    emitter.send(message);
                                    messageBuilder.setLength(0);
                                }
                            }
                        }
                    }

                    // 发送剩余的内容
                    if (messageBuilder.length() > 0) {
                        emitter.send(messageBuilder.toString());
                    }

                    emitter.complete();
                }
            }
        } catch (Exception e) {
            log.error("处理流式响应失败", e);
            try {
                emitter.send("获取研究方向介绍失败，请稍后重试");
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    /**
     * 检测推荐理由中是否包含对导师的负面信息
     * @param request 包含推荐理由的请求对象
     * @return 检测结果，包含是否包含负面信息及具体内容
     */
    @PostMapping("/check-negative-content")
    public Map<String, Object> checkNegativeContent(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String recommendationText = request.get("recommendationText");
            if (recommendationText == null || recommendationText.isEmpty()) {
                result.put("hasNegativeContent", false);
                result.put("message", "推荐理由不能为空");
                return result;
            }
            
            log.info("开始检测推荐理由中的负面内容: {}", recommendationText);
            
            // 构建消息
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
                "你是一个内容审核助手，专门检测文本中是否包含对导师的负面信息。\n" +
                "请分析提供的推荐理由文本，检查是否包含以下类型的负面内容：\n" +
                "1. 对导师的辱骂、人身攻击或贬低\n" +
                "2. 对导师学术能力或教学水平的负面评价\n" +
                "3. 对导师个人品德的质疑或负面描述\n" +
                "4. 对导师声誉可能造成损害的言论\n" +
                "5. 其他可能对导师形象产生负面影响的内容\n\n" +
                "请以JSON格式返回结果，包含以下字段：\n" +
                "- hasNegativeContent: 布尔值，表示是否包含负面内容\n" +
                "- message: 字符串，如果包含负面内容，则用一句话总结问题；如果不包含负面内容，则返回空字符串\n" +
                "- negativeContent: 字符串数组，列出检测到的具体负面内容\n" +
                "- suggestion: 字符串，提供修改建议\n" +
                "- confidence: 数字，表示检测的置信度（0-1之间）\n\n" +
                "注意：请严格判断，任何可能对导师形象产生负面影响的内容都应标记为负面内容。"
            );
            messages.add(systemMessage);
            
            // 添加用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "请检测以下推荐理由中是否包含对导师的负面信息：\n\n" + recommendationText);
            messages.add(userMessage);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "moonshot-v1-8k");
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            
            // 发送请求并处理响应
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(getApiUrl());
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + KIMI_API_KEY);
                
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);
                httpPost.setEntity(new StringEntity(requestBodyJson, StandardCharsets.UTF_8));
                
                try (CloseableHttpResponse response = client.execute(httpPost)) {
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new RuntimeException("Kimi API request failed with status: " + 
                            response.getStatusLine().getStatusCode());
                    }
                    
                    // 读取响应内容
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8)
                    );
                    
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    
                    // 解析响应
                    JsonNode responseNode = objectMapper.readTree(responseBuilder.toString());
                    String content = responseNode.path("choices")
                                           .path(0)
                                           .path("message")
                                           .path("content")
                                           .asText("");
                    
                    log.info("AI返回的原始内容: {}", content);
                    
                    // 解析AI返回的JSON结果
                    JsonNode resultNode;
                    try {
                        resultNode = objectMapper.readTree(content);
                    } catch (Exception e) {
                        log.error("解析AI返回的JSON失败", e);
                        // 尝试从文本中提取信息
                        boolean containsNegative = content.toLowerCase().contains("负面") || 
                                                 content.toLowerCase().contains("辱骂") ||
                                                 content.toLowerCase().contains("攻击") ||
                                                 content.toLowerCase().contains("贬低");
                        
                        result.put("hasNegativeContent", containsNegative);
                        result.put("message", containsNegative ? "检测到负面内容" : "");
                        result.put("rawResponse", content);
                        return result;
                    }
                    
                    // 构建返回结果
                    boolean hasNegativeContent = resultNode.path("hasNegativeContent").asBoolean(false);
                    result.put("hasNegativeContent", hasNegativeContent);
                    
                    if (hasNegativeContent) {
                        result.put("message", resultNode.path("message").asText("包含负面信息"));
                        result.put("negativeContent", resultNode.path("negativeContent"));
                        result.put("suggestion", resultNode.path("suggestion").asText(""));
                        result.put("confidence", resultNode.path("confidence").asDouble(0.8));
                    } else {
                        result.put("message", "");
                        result.put("negativeContent", new ArrayList<>());
                        result.put("suggestion", "");
                        result.put("confidence", resultNode.path("confidence").asDouble(0.8));
                    }
                    
                    log.info("负面内容检测完成，结果: {}", result);
                }
            }
        } catch (Exception e) {
            log.error("检测负面内容时发生错误", e);
            result.put("hasNegativeContent", false);
            result.put("message", "检测负面内容时发生错误: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 检查用户是否已经查询过某个研究方向
     * @param request HTTP请求，用于获取客户端IP
     * @param directionName 研究方向名称
     * @return 包含是否可查询的结果
     */
    @GetMapping("/direction/can-query")
    public Map<String, Object> canQueryDirection(HttpServletRequest request, @RequestParam String directionName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取客户端IP
            String clientIp = getClientIp(request);
            String cacheKey = clientIp + "-" + directionName;
            
            // 检查是否已查询过
            boolean canQuery = !directionIntroCache.containsKey(cacheKey);
            
            result.put("success", true);
            result.put("canQuery", canQuery);
            result.put("directionName", directionName);
            
            log.info("检查研究方向查询权限: IP={}, 方向={}, 可查询={}", clientIp, directionName, canQuery);
            
        } catch (Exception e) {
            log.error("检查研究方向查询权限时发生错误", e);
            result.put("success", false);
            result.put("canQuery", true); // 出错时默认允许查询，后端会再次验证
            result.put("message", "检查失败: " + e.getMessage());
        }
        
        return result;
    }
}




