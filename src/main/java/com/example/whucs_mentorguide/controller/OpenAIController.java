package com.example.whucs_mentorguide.controller;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    private String API_KEY;

    // DeepSeek API 基础地址（从配置文件中注入）
    @Value("${ai.config.deepseek.baseUrl}")
    private String API_BASE_URL;

    // 完整的API URL
    private String getApiUrl() {
        return API_BASE_URL + "/chat/completions";
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
        // TODO: 生产环境应从认证头获取真实用户ID
        String userId = "123"; // 临时硬编码用户ID

        // 创建SSE发射器（-1表示永不超时）
        SseEmitter emitter = new SseEmitter(-1L);

        // 提交异步任务处理请求
        executorService.execute(() -> {
            try {
                log.info("开始处理流式请求，问题内容: {}", question);

                // --- 历史记录管理 ---
                // 获取当前用户的历史消息（不存在则创建空列表）
                List<Map<String, String>> messages = sessionHistory.getOrDefault(userId, new ArrayList<>());

                // 构造用户消息对象
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");    // 消息角色：用户
                userMessage.put("content", question); // 消息内容

                // 构造系统提示消息（注意：当前实现每次请求都会添加，建议优化）
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system"); // 消息角色：系统
                systemMessage.put("content", SYSTEM_PROMPT); // 系统提示词

                // 将新消息加入历史记录
                messages.add(userMessage);
                messages.add(systemMessage);

                // --- API请求构造 ---
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpPost request = new HttpPost(getApiUrl());

                    // 设置请求头
                    request.setHeader("Content-Type", "application/json");
                    request.setHeader("Authorization", "Bearer " + API_KEY);

                    // 构造请求体
                    Map<String, Object> requestMap = new HashMap<>();
                    requestMap.put("model", "deepseek-chat"); // 使用的模型
                    requestMap.put("messages", messages);     // 包含上下文的完整消息历史
                    requestMap.put("stream", true);           // 启用流式响应

                    // 序列化为JSON字符串
                    String requestBody = objectMapper.writeValueAsString(requestMap);
                    request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                    // --- 处理API响应 ---
                    try (CloseableHttpResponse response = client.execute(request);
                         BufferedReader reader = new BufferedReader(
                                 new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

                        StringBuilder aiResponse = new StringBuilder(); // 用于累积完整响应
                        String line;

                        // 逐行读取流式响应
                        while ((line = reader.readLine()) != null) {
                            // SSE协议数据行以"data: "开头
                            if (line.startsWith("data: ")) {
                                String jsonData = line.substring(6); // 去除"data: "前缀

                                // 流结束标记
                                if ("[DONE]".equals(jsonData)) {
                                    break;
                                }

                                // 解析JSON数据
                                JsonNode node = objectMapper.readTree(jsonData);

                                // 提取响应内容（路径：choices[0].delta.content）
                                String content = node.path("choices")
                                        .path(0)
                                        .path("delta")
                                        .path("content")
                                        .asText("");

                                // 发送有效内容到客户端
                                if (!content.isEmpty()) {
                                    emitter.send(content);       // 流式发送片段
                                    aiResponse.append(content); // 累积完整响应
                                }
                            }
                        }

                        // --- 历史记录更新 ---
                        // 构造AI回复消息
                        Map<String, String> aiMessage = new HashMap<>();
                        aiMessage.put("role", "assistant"); // 消息角色：AI助手
                        aiMessage.put("content", aiResponse.toString()); // 完整回复内容

                        messages.add(aiMessage); // 将AI回复加入历史
                        sessionHistory.put(userId, messages); // 更新会话状态

                        log.info("流式请求处理完成，问题: {}", question);
                        emitter.complete(); // 正常结束流
                    }
                } catch (Exception e) {
                    log.error("DeepSeek API请求处理异常", e);
                    try {
                        // 发送错误消息到前端
                        emitter.send("AI聊天服务暂时不可用，请稍后再试。错误信息: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(e);
                    }
                }
            } catch (Exception e) {
                log.error("请求处理流程异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping(value = "/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recommend(@RequestBody Map<String, Object> request) {
        String userId = "123";
        SseEmitter emitter = new SseEmitter(-1L);

        log.info("接收到推荐请求，请求内容: {}", request);
        log.info("API URL: {}", getApiUrl());
        log.info("API Key: {}", API_KEY.substring(0, 5) + "...");

        executorService.execute(() -> {
            try {
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
                    httpPost.setHeader("Authorization", "Bearer " + API_KEY);

                    Map<String, Object> requestMap = new HashMap<>();
                    requestMap.put("model", "deepseek-chat");
                    requestMap.put("messages", messages);
                    requestMap.put("stream", true);

                    String requestBody = objectMapper.writeValueAsString(requestMap);
                    httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                    log.info("发送到DeepSeek API的请求URL: {}", getApiUrl());
                    log.info("发送到DeepSeek API的请求头: {}", Arrays.toString(httpPost.getAllHeaders()));
                    log.info("发送到DeepSeek API的请求体: {}", requestBody);

                    try (CloseableHttpResponse response = client.execute(httpPost)) {
                        int statusCode = response.getStatusLine().getStatusCode();
                        log.info("DeepSeek API响应状态码: {}", statusCode);

                        if (statusCode != 200) {
                            String errorMessage = "DeepSeek API返回错误状态码: " + statusCode;
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
                        log.error("DeepSeek API请求处理异常", e);
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

    private String buildSystemPrompt(Map<String, Object> request) {
        return "你是一个专业的导师推荐系统。请根据用户的选择和偏好，推荐最适合的导师。\n" +
               "请按照以下格式返回推荐结果：\n\n" +
               "## 推荐导师列表\n\n" +
               "### 1. [导师姓名]\n" +
               "- 职称：[职称]\n" +
               "- 所属院系：[院系]\n" +
               "- 研究方向：[研究方向]\n" +
               "- 个人主页：[个人主页链接]\n" +
               "- 推荐理由：[详细推荐理由]\n\n" +
               "### 2. [导师姓名]\n" +
               "...\n\n" +
               "请确保：\n" +
               "1. 每个导师的信息用markdown格式清晰展示\n" +
               "2. 推荐理由要具体，说明与用户需求的匹配点\n" +
               "3. 个人主页链接要完整\n" +
               "4. 不要有多余的空格和换行";
    }

    private String buildUserPrompt(Map<String, Object> request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下信息推荐导师：\n\n");

        // 添加标签信息
        if (request.containsKey("tags") && request.get("tags") instanceof List) {
            List<String> tags = (List<String>) request.get("tags");
            if (!tags.isEmpty()) {
                prompt.append("研究方向：").append(String.join("、", tags)).append("\n");
            }
        }

        // 添加用户偏好
        if (request.containsKey("preferences") && request.get("preferences") instanceof String) {
            String preferences = (String) request.get("preferences");
            if (!preferences.isEmpty()) {
                prompt.append("用户偏好：").append(preferences).append("\n");
            }
        }

        // 添加教师信息
        if (request.containsKey("teachers") && request.get("teachers") instanceof List) {
            List<Map<String, Object>> teachers = (List<Map<String, Object>>) request.get("teachers");
            prompt.append("\n可选的教师列表：\n");
            for (Map<String, Object> teacher : teachers) {
                prompt.append("- ").append(teacher.get("name")).append("（")
                        .append(teacher.get("title")).append("，")
                        .append(teacher.get("department")).append("，")
                        .append("研究方向：").append(teacher.get("research_area")).append("，")
                        .append("个人主页：").append(teacher.get("profile_url")).append("）\n");
            }
        }

        prompt.append("\n请根据以上信息，推荐最适合的导师，并说明推荐理由。");
        return prompt.toString();
    }
}
