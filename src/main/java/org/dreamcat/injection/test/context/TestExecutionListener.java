package org.dreamcat.injection.test.context;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
public interface TestExecutionListener {

    default void beforeTestClass(TestContext testContext) throws Exception {
    }

    default void prepareTestInstance(TestContext testContext) throws Exception {
    }

    default void beforeTestMethod(TestContext testContext) throws Exception {
    }

    default void beforeTestExecution(TestContext testContext) throws Exception {
    }

    default void afterTestExecution(TestContext testContext) throws Exception {
    }

    default void afterTestMethod(TestContext testContext) throws Exception {
    }

    default void afterTestClass(TestContext testContext) throws Exception {
    }
}
