package com.constructiq.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/27 12:24
 */

@RestController
@RequestMapping("/test")
public class Test {
    private final AtomicInteger counter = new AtomicInteger(0);
    @GetMapping("/helloworld")
    public Map<String, String> HelloWorld() {
        Map<String, String> map = Map.of(
                "content", "Hello World!",
                "count", String.valueOf(counter.incrementAndGet())
        );
        System.out.println(map);
        return map;
    }
}
