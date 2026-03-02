package com.gkjt.gkjt2026.service;

import com.gkjt.gkjt2026.model.SensorEvent;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
/**
 * 🧠 脚本执行引擎
 * 作用：读取 scripts 文件夹下的 Groovy 代码并运行
 */
@Service
public class GroovyScriptService {

    // 从配置文件读取路径 app.script.path=./scripts
    @Value("${app.script.path}")
    private String scriptPath;

    public void runSpaceAnalysis(String spaceId, List<SensorEvent> events) {
        // 1. 拼凑脚本文件路径 (例如 ./scripts/SPACE_WH_1F.groovy)
        File scriptFile = new File(scriptPath, spaceId + ".groovy");

        if (!scriptFile.exists()) {
            return; // 没脚本就不跑
        }

        try {
            System.out.println(">>> [脚本引擎] 正在加载: " + scriptFile.getName());

            // 2. 读取文件内容
            String scriptContent = Files.readString(scriptFile.toPath(), StandardCharsets.UTF_8);
// ======================== 新增：打印脚本内容 ========================
//            System.out.println("------ 📄 正在执行的热加载脚本内容如下 ------");
//            System.out.println(scriptContent);
//            System.out.println("-------------------------------------------");
// ===================================================================
            // 3. 准备运行环境 (把 Java 的 events 传给脚本)
            Binding binding = new Binding();
            binding.setVariable("events", events);
            GroovyShell shell = new GroovyShell(binding);

            // 4. 运行脚本
            Object result = shell.evaluate(scriptContent);

            // 5. 如果脚本返回了结果（比如报警信息），打印出来
            if (result != null) {
                System.out.println("🌟🌟🌟 [脚本判定结果]: \n" + result.toString());
            }

        } catch (Exception e) {
            System.err.println("❌ 脚本运行出错: " + scriptFile.getName());
            e.printStackTrace();
        }

    }
    /**
     * 🔥 新增：动态读取指定空间脚本中的 windowSeconds 配置
     * @param spaceId 空间ID
     * @return 脚本中配置的等待秒数（默认5秒）
     */
    public int getWindowSeconds(String spaceId) {
        File scriptFile = new File(scriptPath, spaceId + ".groovy");
        if (!scriptFile.exists()) {
            return 5; // 如果脚本不存在，给个保底的默认值
        }

        try {
            String scriptContent = Files.readString(scriptFile.toPath(), StandardCharsets.UTF_8);

            // 使用正则表达式匹配 "def windowSeconds = 10" 这样的语句
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("def\\s+windowSeconds\\s*=\\s*(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(scriptContent);

            if (matcher.find()) {
                // 成功提取到了数字！
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            System.err.println("❌ 读取脚本时间窗口配置失败: " + e.getMessage());
        }

        // 如果脚本里没写这句配置，保底返回 5 秒
        return 5;
    }
}