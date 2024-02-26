package com.project.NlConverter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class springTest {
    @GetMapping("/hello")
    public static String spingTester(){
        return "hello world";
    }
}
