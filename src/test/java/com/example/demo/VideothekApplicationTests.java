
package com.example.demo;

import com.example.demo.controller.VideothekController;
import com.example.demo.service.VideothekService;
import com.example.demo.service.S3Service;
import com.example.demo.model.Film;
import com.example.demo.model.Playlist;
import com.example.demo.dto.SaveFilmDTO;
import com.example.demo.repository.FilmRepository;
import com.example.demo.repository.PlaylistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

@WebMvcTest(VideothekController.class)
class VideothekApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideothekService videothekService;  // Mock the service
    
    @MockBean
    private FilmRepository filmRepo;           // Mock Film Repository
    
    @MockBean
    private PlaylistRepository playlRepo;      // Mock Playlist Repository
    
    @MockBean
    private S3Service s3Service;               // Mock S3Service

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRedirectToStartPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/videothek"));
    }

    @Test
    void testShowAddFilm() throws Exception {
        mockMvc.perform(get("/addfilm"))
                .andExpect(status().isOk())
                .andExpect(view().name("addFilm"));
    }

    @Test
    public void testShowStartPage() throws Exception {
        when(videothekService.getAllFilms()).thenReturn(List.of(new Film("Film1", "Description1", 120)));
        
        mockMvc.perform(get("/videothek"))
            .andExpect(status().isOk())
            .andExpect(view().name("startPage"))
            .andExpect(model().attributeExists("films"));
    }

    @Test
    public void testShowPlaylistPage() throws Exception {
        when(videothekService.getAllFilmsFromPlaylist()).thenReturn(List.of(new Playlist("Playlist1", "Description1")));
        
        mockMvc.perform(get("/playlist"))
            .andExpect(status().isOk())
            .andExpect(view().name("playlistPage"))
            .andExpect(model().attributeExists("playlists"));
    }

    @Test
    public void testShowFilmDetailsId() throws Exception {
        Long filmId = 1L;
        Film film = new Film("Film1", "Description1", 120);
        
        when(videothekService.getFilmById(filmId)).thenReturn(Optional.of(film));
        
        mockMvc.perform(get("/filmsId/{id}", filmId))
            .andExpect(status().isOk())
            .andExpect(view().name("detailsFilm"))
            .andExpect(model().attributeExists("film"));
    }

    @Test
    void testShowFilmDetailsIdFromPlaylist() throws Exception {
        Playlist playlist = new Playlist(1L, "Playlist1", "Beschreibung Playlist");
        when(videothekService.getFilmByIdFromPlaylist(1L)).thenReturn(Optional.of(playlist));
    
        mockMvc.perform(get("/playlistId/1"))
                .andExpect(status().isOk()) 
                .andExpect(view().name("detailsPlaylist"))
                .andExpect(model().attribute("playlist", playlist));
    }

    @Test
    void testShowFilmDetailsName() throws Exception {
        Film film = new Film(1L, 120, "Film1", "Beschreibung 1", "videoKey1");
        when(videothekService.getFilmByName("Film1")).thenReturn(Optional.of(film));
    
        mockMvc.perform(get("/filmsName/Film1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detailsFilm"))
                .andExpect(model().attribute("film", film));
    }

    @Test
    void testShowFilmDetailsNameFromPlaylist() throws Exception {
        Playlist playlist = new Playlist(1L, "Playlist1", "Beschreibung Playlist");
        when(videothekService.getFilmByNameFromPlaylist("Playlist1")).thenReturn(Optional.of(playlist));
    
        mockMvc.perform(get("/playlistName/Playlist1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detailsPlaylist"))
                .andExpect(model().attribute("playlist", playlist));
    }

    @Test
    public void testSaveFilm() throws Exception {
        SaveFilmDTO saveFilmDTO = new SaveFilmDTO("Film1", "Description1", 120, "Drama");
        MockMultipartFile file = new MockMultipartFile("file", "film.mp4", "video/mp4", "dummy content".getBytes());

        when(s3Service.uploadFile(file)).thenReturn("mock-file-key");

        mockMvc.perform(multipart("/saveFilm")
                .file(file)
                .param("name", saveFilmDTO.getName())
                .param("description", saveFilmDTO.getDescription())
                .param("laenge", String.valueOf(saveFilmDTO.getLaenge())))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/videothek"))
            .andExpect(flash().attributeExists("message"));
    }

    @Test
    void testSaveFilmPlaylist() throws Exception {
        Playlist playlist = new Playlist(1L, "Playlist1", "Beschreibung Playlist");
    
        mockMvc.perform(get("/saveFilmPlaylist")
                        .flashAttr("playlist", playlist)) 
                .andExpect(status().isOk()) 
                .andExpect(view().name("saveFilmPlaylist")); 
    }
}
