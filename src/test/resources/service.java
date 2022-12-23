package $packageName.service;

import $packageName.entity.$Entity;
import java.util.List;
import org.dreamcat.common.Pair;

public interface $ {

    Entity
}

    Service {

        Long create$ {
            Entity
        } ($Entity entity);

        $ {
            Entity
        } delete$ {
            Entity
        } (Long id);

        Long update$ {
            Entity
        } ($Entity entity);

        $ {
            Entity
        } get$ {
            Entity
        } (Long id);

        List < $ {
            Entity
        }>list$ {
            Entity
        } (String nameLike);

        Pair < List < $ {
            Entity
        }>,Long > page$ {
            Entity
        } (String nameLike, Integer pageNum, Integer pageSize);
    }