package org.dreamcat.injection.test.spring.hello.service;

import java.util.Map;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
public interface BookService {

    void addBook(String name);

    Map<String, Object> addAndGetBook(String name);

    Map<String, Object> getBook(String name);
}
