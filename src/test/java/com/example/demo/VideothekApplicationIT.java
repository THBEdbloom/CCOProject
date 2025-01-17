// Integration Tests
package com.example.demo;

import com.example.demo.entity.Film;
import com.example.demo.repository.FilmRepository;
import com.example.demo.repository.PlaylistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VideothekApplicationIT {

	@LocalServerPort
    private int port;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void my_simple_integration_test() {
        System.out.println("This is an integration test!");
    }

    @Test
    void testGetAllFilmsIntegration() {
        // Arrange
        Film film = new Film(0, 120, "TestFilm", "Description", "random_video_key");
        filmRepository.save(film);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:" + port + "/videothek";

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("TestFilm");

        // Cleanup
        filmRepository.delete(film);
    }

    @Test
    void testGetFilmByIdIntegration() {
        // Arrange
        Film film = new Film(0, 90, "FilmById", "Description", "random_video_key");
        Film savedFilm = filmRepository.save(film);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:" + port + "/filmsId/" + savedFilm.getId();

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("FilmById");

        // Cleanup
        filmRepository.delete(savedFilm);
    }
}