package $packageName.controller;

import $packageName.entity.$Entity;
import $packageName.service.$;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.dreamcat.common.Pair;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/${entity}")
public class $ {

    Entity
}

    Controller {

    private final $ {
            Entity
        } Service service;

        @RequestMapping(path = "", method = RequestMethod.GET)
        public Long create$ {
            Entity
        } (@RequestBody $Entity entity){
            return service.create$ {
                Entity
            } (entity);
        }

    @RequestMapping(path = "", method = RequestMethod.DELETE)
    public $ {
            Entity
        } delete$ {
            Entity
        } (@RequestParam("id") Long id){
            return service.delete$ {
                Entity
            } (id);
        }

        @RequestMapping(path = "", method = RequestMethod.PUT)
        public Long update$ {
            Entity
        } (@RequestBody $Entity entity){
            return service.update$ {
                Entity
            } (entity);
        }

    @RequestMapping(path = "", method = RequestMethod.GET)
    public $ {
            Entity
        } get$ {
            Entity
        } (@RequestParam("id") Long id){
            return service.get$ {
                Entity
            } (id);
        }

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public List < $ {
            Entity
        }>list$ {
            Entity
        } (@RequestParam("nameLike") String nameLike){
            return service.list$ {
                Entity
            } (nameLike);
        }

    @RequestMapping(path = "/page", method = RequestMethod.GET)
    public Pair < List < $ {
            Entity
        }>,Long > page$ {
            Entity
        } (
                @RequestParam("nameLike") String nameLike,
                @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize){
            return service.page$ {
                Entity
            } (nameLike, pageNum, pageSize);
        }
    }