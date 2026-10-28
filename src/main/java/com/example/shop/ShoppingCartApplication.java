package com.example.shop;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShoppingCartApplication {

    public static void main(String[] args) {
      SpringApplication.run(ShoppingCartApplication.class, args);
     
       }

    /** Injected wherever "now" is needed, so tests can use a fixed clock. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
