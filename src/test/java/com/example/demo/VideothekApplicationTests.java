package com.example.demo;

// Standard Java imports
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

// JUnit & Mockito imports
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

// Spring-specific test imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

// Project-specific imports
import com.example.demo.controller.VideothekController;
import com.example.demo.service.VideothekService;
import com.example.demo.service.S3Service;
import com.example.demo.entity.Film;
import com.example.demo.entity.Playlist;
import com.example.demo.dto.SaveFilmDTO;
import com.example.demo.repository.FilmRepository;
import com.example.demo.repository.PlaylistRepository;

// Misc imports
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import static org.mockito.Mockito.*;


@WebMvcTest(VideothekController.class)
class VideothekApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideothekService videothekService;

    @MockBean
    private FilmRepository filmRepo;

    @MockBean
    private PlaylistRepository playlRepo;

    @MockBean
    private S3Service s3Service;

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
        when(videothekService.getAllFilms()).thenReturn(List.of(new Film(1L, 120, "Film1", "Description1", "videoKey1")));
        
        mockMvc.perform(get("/videothek"))
            .andExpect(status().isOk())
            .andExpect(view().name("startPage"))
            .andExpect(model().attributeExists("films"));
    }

    @Test
    public void testShowPlaylistPage() throws Exception {
        when(videothekService.getAllFilmsFromPlaylist()).thenReturn(List.of(new Playlist(1L, 120, "Playlist1", "Description1")));

        mockMvc.perform(get("/playlist"))
            .andExpect(status().isOk())
            .andExpect(view().name("playlistPage"))
            .andExpect(model().attributeExists("playlists"));
    }

    @Test
    public void testShowFilmDetailsId() throws Exception {
        Long filmId = 1L;
        Film film = new Film(1L, 120, "Film1", "Description1", "videoKey1");
        
        when(videothekService.getFilmById(filmId)).thenReturn(Optional.of(film));
        
        mockMvc.perform(get("/filmsId/{id}", filmId))
            .andExpect(status().isOk())
            .andExpect(view().name("detailsFilm"))
            .andExpect(model().attributeExists("film"));
    }

    @Test
    void testShowFilmDetailsIdFromPlaylist() throws Exception {
        Playlist playlist = new Playlist(1L, 120, "Playlist1", "Beschreibung Playlist");
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
        Playlist playlist = new Playlist(1L, 120, "Playlist1", "Beschreibung Playlist");
        when(videothekService.getFilmByNameFromPlaylist("Playlist1")).thenReturn(Optional.of(playlist));
    
        mockMvc.perform(get("/playlistName/Playlist1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detailsPlaylist"))
                .andExpect(model().attribute("playlist", playlist));
    }

    @Test
    public void testSaveFilmIOException() throws Exception {
        // Vorbereiten der Daten
        SaveFilmDTO filmDTO = new SaveFilmDTO("Film Name", 120, "Film Description", "videoKey");

        // Simulieren einer IOException
        doThrow(new IOException("File upload failed")).when(s3Service).uploadFile(any(MultipartFile.class));

        mockMvc.perform(post("/saveFilm")
                        .flashAttr("film", filmDTO)
                        .param("file", "dummyFile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/addfilm"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Error uploading file: File upload failed"));
    }

    @Test
    void testSaveFilmPlaylist() throws Exception {
        Playlist playlist = new Playlist(1L, 120, "Playlist1", "Beschreibung Playlist");
    
        mockMvc.perform(get("/saveFilmPlaylist")
                        .flashAttr("playlist", playlist))
                .andExpect(status().isOk())
                .andExpect(view().name("saveFilmPlaylist"));
    }

    @Test
    public void testDeleteFilmPlaylist() throws Exception {
        Playlist playlist = new Playlist(1L, 120, "Playlist Name", "Playlist Description");
    
        mockMvc.perform(get("/deleteFilmPlaylist")
                        .flashAttr("playlist", playlist))
                .andExpect(status().isOk())
                .andExpect(view().name("deleteFilmPlaylist"));
    
        verify(playlRepo, times(1)).delete(playlist);  // Vergewissere dich, dass die Methode aufgerufen wurde
    }

    @Test
    public void testShowUploadPage() throws Exception {
        mockMvc.perform(get("/upload"))
                .andExpect(status().isOk())
                .andExpect(view().name("upload"));
    }

    @Test
    public void testHandleFileUpload() throws Exception {
        // Erstelle eine Mock-Datei
        MockMultipartFile mockFile = new MockMultipartFile("file", "testVideo.mp4", "video/mp4", "dummy content".getBytes());
        
        String objectKey = "testObjectKey";
        String presignedUrl = "https://s3.amazonaws.com/testObjectKey";
        
        // Simuliere erfolgreiche Upload- und URL-Generierung
        when(s3Service.uploadFile(mockFile)).thenReturn(objectKey);
        when(s3Service.generatePresignedUrl(objectKey, Duration.ofHours(6))).thenReturn(presignedUrl);
        
        // Verwende multipart() anstelle von file() auf MockHttpServletRequestBuilder
        mockMvc.perform(multipart("/upload")  // multipart() statt MockHttpServletRequestBuilder.file()
                            .file(mockFile))  // Hier wird die Datei hinzugefügt
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/upload"))
                    .andExpect(flash().attributeExists("message"))
                    .andExpect(flash().attribute("message", "You successfully uploaded testVideo.mp4!"))
                    .andExpect(flash().attribute("fileUrl", presignedUrl));
    }

    @Test
    public void testGetFileUrl() throws Exception {
        String objectKey = "testObjectKey";
        String presignedUrl = "https://s3.amazonaws.com/testObjectKey";
    
        // Simuliere die URL-Generierung
        when(s3Service.generatePresignedUrl(objectKey, Duration.ofHours(1))).thenReturn(presignedUrl);
    
        mockMvc.perform(get("/files/{objectKey}/url", objectKey))
                .andExpect(status().isOk())
                .andExpect(content().string(presignedUrl));
    }
    
    @Test
    public void testGetFileUrlFailure() throws Exception {
        String objectKey = "testObjectKey";
    
        // Simuliere eine Ausnahme
        when(s3Service.generatePresignedUrl(objectKey, Duration.ofHours(1)))
                .thenThrow(new RuntimeException("Failed to generate URL"));
    
        mockMvc.perform(get("/files/{objectKey}/url", objectKey))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to generate URL: Failed to generate URL"));
    }
}
