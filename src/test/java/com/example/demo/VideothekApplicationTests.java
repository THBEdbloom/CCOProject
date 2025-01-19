package com.example.demo;

// Standard Java imports
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.io.ByteArrayInputStream;

// JUnit & Mockito imports
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

// Spring-specific test imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
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

// AWS S3 imports
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

// Mockito imports (removed duplicates)
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private AmazonS3 s3Client;

    private MockMultipartFile file;
    private String bucketName = "test-bucket";
    private String objectKey = "test-video.mp4";
    private String contentType = "video/mp4";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        file = new MockMultipartFile("file", "test-video.mp4", contentType, "dummy content".getBytes());
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

    @Test
    public void testSaveFilmSuccess() throws Exception {
        // Erstelle einen Film-DTO mit validen Daten
        SaveFilmDTO film = new SaveFilmDTO("Title", 120, "Description", "video-key");
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "dummy content".getBytes());
    
        // Mock die S3Service-Methode uploadFile
        when(s3Service.uploadFile(any(MultipartFile.class))).thenReturn("video-key");
    
        // Führe den POST-Request aus
        mockMvc.perform(multipart("/saveFilm")
                .file(file)
                .param("name", film.getName())
                .param("description", film.getDescription())
                .param("laenge", String.valueOf(film.getLaenge())))
                .andExpect(status().isFound()) // 302 Status für Redirect
                .andExpect(redirectedUrl("/videothek")) // Erwartete Umleitung
                .andExpect(flash().attribute("message", "Film successfully added with video upload!"));
    }
    
    @Test
    public void testSaveFilmError() throws Exception {
        // Erstelle einen Film-DTO mit validen Daten
        SaveFilmDTO film = new SaveFilmDTO("Title", 120, "Description", "video-key");
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "dummy content".getBytes());
    
        // Mocke einen Fehler beim Hochladen
        when(s3Service.uploadFile(any(MultipartFile.class))).thenThrow(new IOException("File upload failed"));
    
        // Führe den POST-Request aus
        mockMvc.perform(multipart("/saveFilm")
                .file(file)
                .param("name", film.getName())
                .param("description", film.getDescription())
                .param("laenge", String.valueOf(film.getLaenge())))
                .andExpect(status().isFound()) // 302 Status für Redirect
                .andExpect(redirectedUrl("/addfilm")) // Erwartete Umleitung
                .andExpect(flash().attribute("error", "Error uploading file: File upload failed"));
    }

    @Test
    public void testUploadFile_Success() throws IOException {
        // Arrange
        when(s3Client.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());

        // Act
        String fileName = s3Service.uploadFile(file);

        // Assert
        assertNotNull(fileName); // Es sollte ein Dateiname zurückgegeben werden
        assertTrue(fileName.startsWith(String.valueOf(System.currentTimeMillis()))); // Es sollte den Timestamp voranstellen
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class)); // Überprüfe, dass die S3 putObject-Methode einmal aufgerufen wurde
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadFile_InvalidContentType() throws IOException {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "dummy content".getBytes());

        // Act
        s3Service.uploadFile(invalidFile); // Sollte eine IllegalArgumentException werfen
    }

    @Test
    public void testGeneratePresignedUrl_Success() {
        // Arrange
        Duration expiration = Duration.ofMinutes(10);
        URL mockUrl = mock(URL.class);
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);

        // Act
        String url = s3Service.generatePresignedUrl(objectKey, expiration);

        // Assert
        assertNotNull(url); // Es sollte eine URL zurückgegeben werden
        verify(s3Client, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class)); // Überprüfe, dass die generatePresignedUrl-Methode einmal aufgerufen wurde
    }

    @Test
    public void testIsVideoContentType_Valid() {
        // Arrange
        String validContentType = "video/mp4";

        // Act
        boolean isVideo = s3Service.isVideoContentType(validContentType);

        // Assert
        assertTrue(isVideo); // Sollte true zurückgeben
    }

    @Test
    public void testIsVideoContentType_Invalid() {
        // Arrange
        String invalidContentType = "image/jpeg";

        // Act
        boolean isVideo = s3Service.isVideoContentType(invalidContentType);

        // Assert
        assertFalse(isVideo); // Sollte false zurückgeben
    }
}
