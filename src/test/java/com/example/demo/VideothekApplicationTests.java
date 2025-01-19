package com.example.demo;

import com.example.demo.entity.Film;
import com.example.demo.entity.Playlist;
import com.example.demo.repository.FilmRepository;
import com.example.demo.repository.PlaylistRepository;
import com.example.demo.service.S3Service;
import com.example.demo.service.VideothekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class VideothekApplicationTests {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private S3Service s3Service;

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
    void testGetFilmById() {
        Film film = new Film(1, 120, "TestFilm", "Description", "random_video_key");
        when(filmRepository.findById(1L)).thenReturn(Optional.of(film));
        Optional<Film> result = videothekService.getFilmById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("TestFilm");
        verify(filmRepository, times(1)).findById(1L);
    }

    @Test
    void testGetFilmById_NotFound() {
        when(filmRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> videothekService.getFilmById(1L).orElseThrow(() -> new ResponseStatusException(404)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void testGetFilmByName() {
        Film film = new Film(1, 120, "TestFilm", "Description", "random_video_key");
        when(filmRepository.findByName("TestFilm")).thenReturn(Optional.of(film));
        Optional<Film> result = videothekService.getFilmByName("TestFilm");
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Description");
        verify(filmRepository, times(1)).findByName("TestFilm");
    }

    @Test
    void testGetAllFilms() {
        Film film1 = new Film(1, 120, "Film1", "Desc1", "random_video_key");
        Film film2 = new Film(2, 90, "Film2", "Desc2", "random_video_key");
        when(filmRepository.findAll()).thenReturn(List.of(film1, film2));
        var result = videothekService.getAllFilms();
        assertThat(result).hasSize(2);
        verify(filmRepository, times(1)).findAll();
    }

    @Test
    void testSaveFilm() throws IOException {
        Film film = new Film(1, 120, "New Film", "Description", "");
        String mockVideoKey = "mock_video_key";
        when(s3Service.uploadFile(any())).thenReturn(mockVideoKey);
        when(filmRepository.save(any())).thenReturn(film);
        
        Film savedFilm = videothekService.saveFilm(film, null);
        
        assertThat(savedFilm.getVideoKey()).isEqualTo(mockVideoKey);
        verify(filmRepository, times(1)).save(film);
    }

    @Test
    void testSaveFilm_FailUpload() throws IOException {
        Film film = new Film(1, 120, "New Film", "Description", "");
        when(s3Service.uploadFile(any())).thenThrow(new IOException("Upload failed"));
        assertThatThrownBy(() -> videothekService.saveFilm(film, null))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Upload failed");
    }

    @Test
    void testGetPlaylistById() {
        Playlist playlist = new Playlist(1, "MyPlaylist", List.of());
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
        Optional<Playlist> result = videothekService.getPlaylistById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("MyPlaylist");
        verify(playlistRepository, times(1)).findById(1L);
    }

    @Test
    void testDeletePlaylist() {
        Playlist playlist = new Playlist(1, "MyPlaylist", List.of());
        doNothing().when(playlistRepository).delete(playlist);
        videothekService.deletePlaylist(playlist);
        verify(playlistRepository, times(1)).delete(playlist);
    }
}
