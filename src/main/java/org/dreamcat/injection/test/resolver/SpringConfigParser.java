package org.dreamcat.injection.test.resolver;


import lombok.SneakyThrows;
import org.dreamcat.common.Pair;
import org.dreamcat.common.json.JavaPropsUtil;
import org.dreamcat.common.json.YamlUtil;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.dreamcat.common.util.FunctionUtil;
import org.dreamcat.common.util.MapUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Jerry Will
 * @version 2025-09-03
 */
public class SpringConfigParser {

    public Map<String, Object> parseExprVars() {
        String active = System.getProperty("spring.profiles.active");

        // config files
        List<Pair<Function<String, Map<String, Object>>, List<String>>> list = Arrays.asList(
                Pair.of(YamlUtil::fromJsonObject, Arrays.asList("yml", "yaml")),
                Pair.of(JavaPropsUtil::fromJsonObject, Collections.singletonList("properties"))
        );

        Map<String, Object> baseConfig = loadConfigFile(list, null);

        if (active == null) {
            active = (String) baseConfig.get("spring.profiles.active");
        }
        if (active == null) return baseConfig;

        Map<String, Object> activeConfig = loadConfigFile(list, active);

        MapUtil.merge(activeConfig, baseConfig);
        return activeConfig;
    }

    private Map<String, Object> loadConfigFile(
            List<Pair<Function<String, Map<String, Object>>, List<String>>> list, String active) {
        for (Pair<Function<String, Map<String, Object>>, List<String>> pair : list) {
            Function<String, Map<String, Object>> mapper = pair.first();
            List<String> extensions = pair.second();
            for (String extension : extensions) {
                String path;
                if (active != null) {
                    path = String.format("application-%s.%s", active, extension);
                } else {
                    path = "application." + extension;
                }

                String content = FunctionUtil.invokeOrNull(() ->
                        ClassLoaderUtil.getResourceAsString(path));
                if (content == null) continue;
                Map<String, Object> config = mapper.apply(content);
                return MapUtil.flat(config);
            }
        }
        return new HashMap<>();
    }
}
