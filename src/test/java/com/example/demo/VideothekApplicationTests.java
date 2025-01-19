package com.example.demo;

import com.example.demo.controller.VideothekController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VideothekApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Hier ist keine spezielle Initialisierung nötig, MockMvc wird durch AutoConfigureMockMvc bereitgestellt
    }

    @Test
    void testRedirectToStartPage() throws Exception {
        mockMvc.perform(get("/")) // Simuliert eine GET-Anfrage an die URL "/"
                .andExpect(status().is3xxRedirection()) // Überprüft, ob der Statuscode 3xx (Redirect) ist
                .andExpect(redirectedUrl("/videothek")); // Überprüft, ob die Weiterleitung auf "/videothek" erfolgt
    }
}
