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
    void testShowStartPage() throws Exception {
        // Wir simulieren hier eine Antwort von videothekservice.getAllFilms()
        List<Film> films = List.of(new Film(1L, 120, "Film1", "Beschreibung 1", "videoKey1"));
        when(videothekservice.getAllFilms()).thenReturn(films);
    
        mockMvc.perform(get("/videothek"))
                .andExpect(status().isOk()) // Überprüfen, dass der Status 200 (OK) zurückgegeben wird
                .andExpect(view().name("startPage")) // Überprüfen, dass die View "startPage" gerendert wird
                .andExpect(model().attribute("films", films)); // Überprüfen, dass das Modell die Liste von Filmen enthält
    }

    @Test
    void testShowPlaylistPage() throws Exception {
        // Wir simulieren eine Antwort von videothekservice.getAllFilmsFromPlaylist()
        List<Playlist> playlists = List.of(new Playlist(1L, "Playlist1", "Beschreibung Playlist"));
        when(videothekservice.getAllFilmsFromPlaylist()).thenReturn(playlists);
    
        mockMvc.perform(get("/playlist"))
                .andExpect(status().isOk()) // Überprüfen, dass der Status 200 (OK) zurückgegeben wird
                .andExpect(view().name("playlistPage")) // Überprüfen, dass die View "playlistPage" gerendert wird
                .andExpect(model().attribute("playlists", playlists)); // Überprüfen, dass das Modell die Playlist enthält
    }

    @Test
    void testShowFilmDetailsId() throws Exception {
        // Wir simulieren eine Antwort von videothekservice.getFilmById()
        Film film = new Film(1L, 120, "Film1", "Beschreibung 1", "videoKey1");
        when(videothekservice.getFilmById(1L)).thenReturn(Optional.of(film));
    
        mockMvc.perform(get("/filmsId/1"))
                .andExpect(status().isOk()) // Überprüfen, dass der Status 200 (OK) zurückgegeben wird
                .andExpect(view().name("detailsFilm")) // Überprüfen, dass die View "detailsFilm" gerendert wird
                .andExpect(model().attribute("film", film)); // Überprüfen, dass das Modell den Film enthält
    }

    @Test
    void testShowFilmDetailsIdFromPlaylist() throws Exception {
        // Wir simulieren eine Antwort von videothekservice.getFilmByIdFromPlaylist()
        Playlist playlist = new Playlist(1L, "Playlist1", "Beschreibung Playlist");
        when(videothekservice.getFilmByIdFromPlaylist(1L)).thenReturn(Optional.of(playlist));
    
        mockMvc.perform(get("/playlistId/1"))
                .andExpect(status().isOk()) // Überprüfen, dass der Status 200 (OK) zurückgegeben wird
                .andExpect(view().name("detailsPlaylist")) // Überprüfen, dass die View "detailsPlaylist" gerendert wird
                .andExpect(model().attribute("playlist", playlist)); // Überprüfen, dass das Modell die Playlist enthält
    }

    @Test
    void testShowFilmDetailsName() throws Exception {
        // Wir simulieren eine Antwort von videothekservice.getFilmByName()
        Film film = new Film(1L, 120, "Film1", "Beschreibung 1", "videoKey1");
        when(videothekservice.getFilmByName("Film1")).thenReturn(Optional.of(film));
    
        mockMvc.perform(get("/filmsName/Film1"))
                .andExpect(status().isOk()) // Überprüfen, dass der Status 200 (OK) zurückgegeben wird
                .andExpect(view().name("detailsFilm")) // Überprüfen, dass die View "detailsFilm" gerendert wird
                .andExpect(model().attribute("film", film)); // Überprüfen, dass das Modell den Film enthält
    }

    @Test
    void testShowFilmDetailsNameFromPlaylist() throws Exception {
        // Wir simulieren eine Antwort von videothekservice.getFilmByNameFromPlaylist()
        Playlist playlist = new Playlist(1L, "Playlist1", "Beschreibung Playlist");
        when(videothekservice.getFilmByNameFromPlaylist("Playlist1")).thenReturn(Optional.of(playlist));
    
        mockMvc.perform(get("/playlistName/Playlist1"))
                .andExpect(status().isOk()) // Überprüfen, dass der Status 200 (OK) zurückgegeben wird
                .andExpect(view().name("detailsPlaylist")) // Überprüfen, dass die View "detailsPlaylist" gerendert wird
                .andExpect(model().attribute("playlist", playlist)); // Überprüfen, dass das Modell die Playlist enthält
    }

    @Test
    void testSaveFilm() throws Exception {
        // Simulierte Eingabewerte
        SaveFilmDTO film = new SaveFilmDTO("Film1", "Beschreibung 1", 120);
        MultipartFile file = new MockMultipartFile("file", "test_video.mp4", "video/mp4", new byte[0]);
    
        // Hier simulieren wir das Hochladen und Speichern des Films
        when(s3Service.uploadFile(file)).thenReturn("mocked_video_key");
    
        mockMvc.perform(multipart("/saveFilm")
                        .file(file)
                        .param("name", film.getName())
                        .param("description", film.getDescription())
                        .param("laenge", String.valueOf(film.getLaenge())))
                .andExpect(status().is3xxRedirection()) // Überprüfen, dass eine Weiterleitung erfolgt
                .andExpect(redirectedUrl("/videothek")); // Überprüfen, dass nach "/videothek" weitergeleitet wird
    }

    @Test
    void testSaveFilmPlaylist() throws Exception {
        Playlist playlist = new Playlist(1L, "Playlist1", "Beschreibung Playlist");
    
        mockMvc.perform(get("/saveFilmPlaylist")
                        .flashAttr("playlist", playlist)) // Überprüfen, dass die Playlist korrekt übergeben wird
                .andExpect(status().isOk()) // Überprüfen, dass der Status 200 (OK) zurückgegeben wird
                .andExpect(view().name("saveFilmPlaylist")); // Überprüfen, dass die View "saveFilmPlaylist" gerendert wird
    }
}
