package $packageName.service;

import java.util.List;
import org.dreamcat.common.Pair;
import $packageName.entity.$Entity;

public interface ${Entity}Service {

    Long create${Entity}($Entity entity);

    ${Entity} delete${Entity}(Long id);

    Long update${Entity}($Entity entity);

    ${Entity} get${Entity}(Long id);

    List<${Entity}> list${Entity}(String nameLike);

    Pair<List<${Entity}>, Long> page${Entity}(String nameLike, Integer pageNum, Integer pageSize);
}