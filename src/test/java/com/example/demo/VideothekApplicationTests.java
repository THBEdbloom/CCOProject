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
    void testGetFilmById() {
        // Arrange
        Film film = new Film(1, 120, "TestFilm", "Description");
        when(filmRepository.findById(1L)).thenReturn(Optional.of(film));

        // Act
        Optional<Film> result = videothekService.getFilmById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("TestFilm");
        verify(filmRepository, times(1)).findById(1L);
    }

    @Test
    void testGetFilmByName() {
        // Arrange
        Film film = new Film(1, 120, "TestFilm", "Description");
        when(filmRepository.findByName("TestFilm")).thenReturn(Optional.of(film));

        // Act
        Optional<Film> result = videothekService.getFilmByName("TestFilm");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Description");
        verify(filmRepository, times(1)).findByName("TestFilm");
    }

    @Test
    void testGetAllFilms() {
        // Arrange
        Film film1 = new Film(1, 120, "Film1", "Desc1");
        Film film2 = new Film(2, 90, "Film2", "Desc2");
        when(filmRepository.findAll()).thenReturn(List.of(film1, film2));

        // Act
        var result = videothekService.getAllFilms();

        // Assert
        assertThat(result).hasSize(2);
        verify(filmRepository, times(1)).findAll();
    }

    @Test
    void testRedirectToStartPage() throws Exception {
        mockMvc.perform(get("/")) // Simuliert eine GET-Anfrage an die URL "/"
                .andExpect(status().is3xxRedirection()) // Überprüft, ob der Statuscode 3xx (Redirect) ist
                .andExpect(redirectedUrl("/videothek")); // Überprüft, ob die Weiterleitung auf "/videothek" erfolgt
    }
}
