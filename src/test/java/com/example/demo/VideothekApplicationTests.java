package com.example.demo.controller;

import com.example.demo.entity.Film;
import com.example.demo.entity.Playlist;
import com.example.demo.service.VideothekService;
import com.example.demo.service.S3Service;
import com.example.demo.repository.PlaylistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VideothekControllerTests {

    private MockMvc mockMvc;

    @Mock
    private VideothekService videothekService;

    @Mock
    private S3Service s3Service;

    @Mock
    private PlaylistRepository playlRepo;

    @InjectMocks
    private VideothekController videothekController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(videothekController).build();
    }

    @Test
    void testShowStartPage() throws Exception {
        when(videothekService.getAllFilms()).thenReturn(List.of(new Film()));
        mockMvc.perform(get("/videothek"))
                .andExpect(status().isOk())
                .andExpect(view().name("startPage"))
                .andExpect(model().attributeExists("films"));
        verify(videothekService, times(1)).getAllFilms();
    }

    @Test
    void testShowFilmDetailsById_Found() throws Exception {
        Film film = new Film(1L, 120, "TestFilm", "Description", "videoKey");
        when(videothekService.getFilmById(1L)).thenReturn(Optional.of(film));

        mockMvc.perform(get("/filmsId/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detailsFilm"))
                .andExpect(model().attributeExists("film"));
    }

    @Test
    void testShowFilmDetailsById_NotFound() throws Exception {
        when(videothekService.getFilmById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/filmsId/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveFilmPlaylist() throws Exception {
        Playlist playlist = new Playlist();
        when(playlRepo.save(any(Playlist.class))).thenReturn(playlist);

        mockMvc.perform(get("/saveFilmPlaylist"))
                .andExpect(status().is3xxRedirection()) // Expect redirect
                .andExpect(redirectedUrl("/playlist")); // URL nach Redirect
    }
}
