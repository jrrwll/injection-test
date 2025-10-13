package org.dreamcat.injection.test.spring.generated;

import static org.dreamcat.common.util.RandomUtil.choose26;
import static org.dreamcat.common.util.RandomUtil.chooseOne;
import static org.dreamcat.common.util.RandomUtil.randi;

import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2022-12-22
 */
public class SpringProjectGen {

    private static final int entity_count = 20;

    public static void main(String[] args) throws Exception {
        String entity_temp = ClassLoaderUtil.getResourceAsString("entity.java");
        String ctrl_temp = ClassLoaderUtil.getResourceAsString("controller.java");
        String svc_temp = ClassLoaderUtil.getResourceAsString("service.java");
        String impl_temp = ClassLoaderUtil.getResourceAsString("impl.java");
        String dao_temp = ClassLoaderUtil.getResourceAsString("dao.java");

        String testSrcDir = new File("build/generated/java").getCanonicalPath();
        String packageName = SpringProjectGen.class.getPackage().getName();
        String packageDir = testSrcDir + "/" + packageName.replace(".", "/");

        Set<String> entityNames = genNames(entity_count);
        for (String entityName : entityNames) {
            String entity = StringUtil.toCapitalLowerCase(entityName);
            Map<String, Object> m = MapUtil.of(
                    "packageName", packageName,
                    "Entity", entityName,
                    "entity", entity);

            String fields = genNames(randi(1, 10)).stream()
                    .map(it -> "    private " + genFieldType() + " " + it + ";")
                    .collect(Collectors.joining("\n"));
            FileUtil.write(new File(getBaseDir(packageDir, "entity"),
                            entityName + ".java"),
                    InterpolationUtil.format(InterpolationUtil.format(
                            entity_temp, Collections.singletonMap("fields", fields)), m));

            FileUtil.write(new File(getBaseDir(packageDir, "controller"),
                            entityName + "Controller.java"),
                    InterpolationUtil.format(ctrl_temp, m));

            FileUtil.write(new File(getBaseDir(packageDir, "service"),
                            entityName + "Service.java"),
                    InterpolationUtil.format(svc_temp, m));

            fields = chooseEntities(entityNames, entityName).stream()
                    .map(it -> field(it, "Service"))
                    .collect(Collectors.joining("\n"));
            if (!fields.isEmpty()) fields = fields + "\n";
            FileUtil.write(new File(getBaseDir(packageDir, "service/impl"),
                            entityName + "ServiceImpl.java"),
                    InterpolationUtil.format(InterpolationUtil.format(
                            impl_temp, Collections.singletonMap("fields", fields)), m));

            fields = chooseEntities(entityNames, entityName).stream()
                    .map(it -> field(it, "Dao"))
                    .collect(Collectors.joining("\n"));
            if (!fields.isEmpty()) fields = fields + "\n";
            FileUtil.write(new File(getBaseDir(packageDir, "dao"),
                            entityName + "Dao.java"),
                    InterpolationUtil.format(InterpolationUtil.format(
                            dao_temp, Collections.singletonMap("fields", fields)), m));
        }
    }

    private static String field(String entityName, String type) {
        return "    @Resource\n    private " +
                entityName + type + " " +
                StringUtil.toCapitalLowerCase(entityName) + type + ";";
    }

    private static List<String> chooseEntities(Set<String> entityNames, String entityName) {
        if (entity_count < 2) return Collections.emptyList();
        int c = entity_count / 5 + 1;
        if (entity_count < 4) c = entity_count - 1;  // 2, 3
        else if (entity_count < 10) c = entity_count - 2; // 4, 5, ..., 9

        List<String> result = new ArrayList<>();

        List<String> list = new ArrayList<>(entityNames);
        list.remove(entityName);
        for (int i = 0; i < c; i++) {
            result.add(list.remove(randi(0, list.size())));
        }
        return result;
    }

    private static File getBaseDir(String packageDir, String name) {
        File baseDir = new File(packageDir, name);
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new RuntimeException("not found " + baseDir);
        }
        return baseDir;
    }

    private static Set<String> genNames(int count) {
        Set<String> names = new HashSet<>();
        for (int i = 0; i < count; i++) {
            for (; ; ) {
                String name = genName();
                if (names.add(name)) break;
            }
        }
        return names;
    }

    private static String genName() {
        StringBuilder s = new StringBuilder();
        int c = randi(1, 5); // 1-4
        for (int i = 0; i < c; i++) {
            int w = randi(2, 4); // 2-3
            s.append(StringUtil.toCapitalCase(choose26(w)));
        }
        return s.toString();
    }

    private static String genFieldType() {
        return chooseOne(new String[]{"String", "Integer", "Long"});
    }
}
