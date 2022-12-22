package $packageName.service.impl;

import java.util.List;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dreamcat.common.Pair;
import $packageName.dao.${Entity}Dao;
import $packageName.entity.$Entity;
import $packageName.service.*;
import org.springframework.stereotype.Service;

@Getter
@RequiredArgsConstructor
@Service
public class ${Entity}ServiceImpl implements ${Entity}Service {

    private final ${Entity}Dao dao;
$fields
    public Long create${Entity}($Entity entity) {
        return dao.create${Entity}(entity);
    }

    public ${Entity} delete${Entity}(Long id) {
        return dao.delete${Entity}(id);
    }

    public Long update${Entity}($Entity entity) {
        return dao.update${Entity}(entity);
    }

    public ${Entity} get${Entity}(Long id) {
        return dao.get${Entity}(id);
    }

    public List<${Entity}> list${Entity}(String nameLike) {
        return dao.list${Entity}(nameLike);
    }

    public Pair<List<${Entity}>, Long> page${Entity}(String nameLike, Integer pageNum, Integer pageSize) {
        return dao.page${Entity}(nameLike, pageNum, pageSize);
    }
}