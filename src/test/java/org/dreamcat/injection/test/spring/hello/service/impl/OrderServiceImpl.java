package org.dreamcat.injection.test.spring.hello.service.impl;

import lombok.RequiredArgsConstructor;
import org.dreamcat.injection.test.spring.hello.service.BookService;
import org.dreamcat.injection.test.spring.hello.service.OrderService;
import org.springframework.stereotype.Component;

/**
 * @author Jerry Will
 * @version 2022-10-13
 */
@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final BookService bookService;

    @Override
    public void borrow(String uid, String bookName) {
        System.out.println(uid + " borrowed " + bookService.getBook(bookName));
    }
}
