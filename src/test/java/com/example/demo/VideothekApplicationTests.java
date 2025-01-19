// Unit Tests
package com.example.demo;

import com.example.demo.entity.Film;
import com.example.demo.entity.Playlist;
import com.example.demo.repository.FilmRepository;
import com.example.demo.repository.PlaylistRepository;
import com.example.demo.service.VideothekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.example.demo.controller.VideothekController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

class VideothekApplicationTests {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private VideothekService videothekService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRedirectToStartPage() throws Exception {
        mockMvc.perform(get("/")) // Simuliert eine GET-Anfrage an die URL "/"
                .andExpect(status().is3xxRedirection()) // Überprüft, ob der Statuscode 3xx (Redirect) ist
                .andExpect(redirectedUrl("/videothek")); // Überprüft, ob die Weiterleitung auf "/videothek" erfolgt
    }
}
