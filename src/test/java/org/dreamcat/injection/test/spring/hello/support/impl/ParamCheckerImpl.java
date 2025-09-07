package org.dreamcat.injection.test.spring.hello.support.impl;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.util.AssertUtil;
import org.dreamcat.common.util.NumberUtil;
import org.dreamcat.injection.test.spring.hello.dao.HelloDao;
import org.dreamcat.injection.test.spring.hello.support.ParamChecker;
import org.springframework.stereotype.Component;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Component
@RequiredArgsConstructor
public class ParamCheckerImpl implements ParamChecker {

    final HelloDao helloDao;

    @Override
    public void checkName(String name) {
        int flag = helloDao.findRule();
        if ((flag & 1) == 1) {
            AssertUtil.requireNotBlank(name, "name");
        } else {
            NumberUtil.parseNumber(name, false);
        }
    }
}
