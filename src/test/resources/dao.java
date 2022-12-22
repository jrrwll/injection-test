package $packageName.dao;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dreamcat.common.Pair;
import $packageName.entity.$Entity;
import org.springframework.stereotype.Repository;

@Getter
@RequiredArgsConstructor
@Repository
public class ${Entity}Dao {
$fields
    public Long create${Entity}($Entity entity) {
        return 0L;
    }

    public ${Entity} delete${Entity}(Long id) {
        return new $Entity();
    }

    public Long update${Entity}($Entity entity) {
        return 0L;
    }

    public ${Entity} get${Entity}(Long id) {
        return new $Entity();
    }

    public List<${Entity}> list${Entity}(String nameLike) {
        return new ArrayList<>();
    }

    public Pair<List<${Entity}>, Long> page${Entity}(String nameLike, Integer pageNum, Integer pageSize) {
        return Pair.empty();
    }
}