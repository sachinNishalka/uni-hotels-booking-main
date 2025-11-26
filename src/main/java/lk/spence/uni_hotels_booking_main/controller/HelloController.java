package lk.spence.uni_hotels_booking_main.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class HelloController {

    @GetMapping("hello")
    public ResponseEntity<String> helloController() {
        return ResponseEntity.ok("Hello, World!");
    }

}
