package org.dreamcat.injection.test.resolver;

import org.dreamcat.common.Pair;
import org.dreamcat.common.json.JavaPropsUtil;
import org.dreamcat.common.json.JsonUtil;
import org.dreamcat.common.json.TomlUtil;
import org.dreamcat.common.json.YamlUtil;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.dreamcat.common.util.FunctionUtil;
import org.dreamcat.common.util.MapUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Jerry Will
 * @version 2025-09-05
 */
public class RitaTestExecutionListener extends InjectionTestExecutionListener {

    public static boolean isEnable() {
        try {
            Class.forName("org.dreamcat.rita.boot.RitaBootApplication");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public RitaTestExecutionListener(Class<?> testClass, Map<String, String> properties) {
        super(testClass, properties);
    }

    @Override
    protected Set<String> resolveBasePackageFromApplicationClass(Class<?> applicationClass) {
        throw new RuntimeException("no implement yet");
    }

    @Override
    protected Function<String, Object> resolveExprValueProviderForConfig() {
        Map<String, Object> exprVars = parseExprVars();
        return exprVars::get;
    }

    /// parse expr vars
    static final List<Pair<Function<String, Map<String, Object>>, List<String>>> extensions = Arrays.asList(
            Pair.of(TomlUtil::fromJsonObject, Collections.singletonList("toml")),
            Pair.of(YamlUtil::fromJsonObject, Arrays.asList("yml", "yaml")),
            Pair.of(JsonUtil::fromJsonObject, Collections.singletonList("json")),
            Pair.of(JavaPropsUtil::fromJsonObject, Collections.singletonList("properties"))
    );

    public static Map<String, Object> parseExprVars() {
        Map<String, Object> config = parseExprVars(null);
        String profile = (String) config.get("rita.profiles.active");
        if (profile == null) return config;

        Map<String, Object> profileConfig = parseExprVars(profile);
        MapUtil.merge(config, profileConfig);
        return config;
    }

    private static Map<String, Object> parseExprVars(String profile) {
        for (Pair<Function<String, Map<String, Object>>, List<String>> pair : extensions) {
            Function<String, Map<String, Object>> mapper = pair.first();
            List<String> extensions = pair.second();
            for (String extension : extensions) {
                String path;
                if (profile != null) {
                    path = String.format("application-%s.%s", profile, extension);
                } else {
                    path = "application." + extension;
                }

                String configContent = FunctionUtil.invokeOrNull(() ->
                        ClassLoaderUtil.getResourceAsString("config/" + path));
                String content = FunctionUtil.invokeOrNull(() ->
                        ClassLoaderUtil.getResourceAsString(path));

                Map<String, Object> configContentMap = new HashMap<>();
                if (configContent != null) {
                    configContentMap = MapUtil.flat(mapper.apply(configContent));
                }

                Map<String, Object> contentMap = new HashMap<>();
                if (content != null) {
                    contentMap = MapUtil.flat(mapper.apply(content));
                }
                if (contentMap.isEmpty() && configContentMap.isEmpty()) {
                    continue;
                }
                MapUtil.merge(contentMap, configContentMap);
                return contentMap;
            }
        }
        return new HashMap<>();
    }
}
