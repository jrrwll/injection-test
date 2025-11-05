package org.dreamcat.injection.test.resolver;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.dreamcat.common.Pair;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.json.JavaPropsUtil;
import org.dreamcat.common.json.JsonUtil;
import org.dreamcat.common.json.TomlUtil;
import org.dreamcat.common.json.YamlUtil;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.dreamcat.common.util.FunctionUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ReflectUtil;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Jerry Will
 * @version 2025-09-05
 */
@Setter
@Accessors(chain = true)
public class ProfileExprValueResolver {

    private String baseName = "application";
    private String profileKey = "rita.profiles.active";
    private String profileDir; // null means classpath

    public static <A extends Annotation> void inject(
            Object bean, Class<A> annoClass, Function<A, String> valueGetter,
            Map<String, Object> exprVars) {
        Function<String, Object> exprValueProvider = exprVars::get;

        List<Field> fields = ReflectUtil.retrieveBeanFields(bean.getClass());
        for (Field field : fields) {
            A injectedValue = field.getDeclaredAnnotation(annoClass);
            if (injectedValue == null) continue;

            String expr = valueGetter.apply(injectedValue);
            String value = InterpolationUtil.formatEl(expr, exprValueProvider);
            // format nested variables
            if (value.contains("$")) {
                value = InterpolationUtil.formatEl(value, exprValueProvider);
            }
            Object fieldValue = ReflectUtil.parse(value, field.getType());
            ReflectUtil.setFieldValue(bean, field, fieldValue);
        }
    }

    /// parse expr vars
    private static final List<Pair<Function<String, Map<String, Object>>, List<String>>> extensions = Arrays.asList(
            Pair.of(TomlUtil::fromJsonObject, Collections.singletonList("toml")),
            Pair.of(YamlUtil::fromJsonObject, Arrays.asList("yml", "yaml")),
            Pair.of(JsonUtil::fromJsonObject, Collections.singletonList("json")),
            Pair.of(JavaPropsUtil::fromJsonObject, Collections.singletonList("properties"))
    );

    public Map<String, Object> parseExprVars() {
        return parseExprVars(null);
    }

    public Map<String, Object> parseExprVars(String preferProfile) {
        Map<String, Object> config = parseExprVars0(null);
        String profile;
        if (preferProfile == null) {
            profile = (String) config.get(profileKey);
            if (profile == null) return config;
        } else {
            profile = preferProfile;
        }

        Map<String, Object> profileConfig = parseExprVars0(profile);
        MapUtil.merge(config, profileConfig);
        return config;
    }

    private Map<String, Object> parseExprVars0(String profile) {
        for (Pair<Function<String, Map<String, Object>>, List<String>> pair : extensions) {
            Function<String, Map<String, Object>> mapper = pair.first();
            List<String> extensions = pair.second();
            for (String extension : extensions) {
                String path;
                if (profile != null) {
                    path = String.format("%s-%s.%s", baseName, profile, extension);
                } else {
                    path = baseName + "." + extension;
                }

                String configContent = readConfigContent("config/" + path);
                String content = readConfigContent(path);

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

    private String readConfigContent(String path) {
        if (profileDir == null) {
            return FunctionUtil.invokeOrNull(() ->
                    ClassLoaderUtil.getResourceAsString(path));
        } else {
            File profileFile = new File(profileDir, path);
            if (!profileFile.exists()) return null;
            return FunctionUtil.invokeOrNull(() -> FileUtil.readAsString(profileFile));
        }
    }
}
