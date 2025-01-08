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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

class VideothekApplicationTests {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private VideothekService videothekService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void my_simple_unit_test() {
        System.out.println("This is a unit test!");
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
}