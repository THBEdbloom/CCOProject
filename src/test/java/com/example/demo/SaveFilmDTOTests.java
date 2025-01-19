package com.example.demo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SaveFilmDTOTests {

    @Test
    public void testSaveFilmDTOConstructor() {
        // Arrange
        String name = "Film Name";
        int laenge = 120;
        String description = "Beschreibung des Films";
        String videoKey = "video-key-123";

        // Act
        SaveFilmDTO saveFilmDTO = new SaveFilmDTO(name, laenge, description, videoKey);

        // Assert
        assertNotNull(saveFilmDTO);
        assertEquals(name, saveFilmDTO.getName());
        assertEquals(laenge, saveFilmDTO.getLaenge());
        assertEquals(description, saveFilmDTO.getDescription());
        assertEquals(videoKey, saveFilmDTO.getVideoKey());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Arrange
        SaveFilmDTO saveFilmDTO1 = new SaveFilmDTO("Film Name", 120, "Beschreibung des Films", "video-key-123");
        SaveFilmDTO saveFilmDTO2 = new SaveFilmDTO("Film Name", 120, "Beschreibung des Films", "video-key-123");

        // Act & Assert
        assertEquals(saveFilmDTO1, saveFilmDTO2);  // Test the equals method
        assertEquals(saveFilmDTO1.hashCode(), saveFilmDTO2.hashCode()); // Test the hashCode method
    }

    @Test
    public void testToString() {
        // Arrange
        SaveFilmDTO saveFilmDTO = new SaveFilmDTO("Film Name", 120, "Beschreibung des Films", "video-key-123");

        // Act
        String toStringValue = saveFilmDTO.toString();

        // Assert
        assertTrue(toStringValue.contains("Film Name"));
        assertTrue(toStringValue.contains("120"));
        assertTrue(toStringValue.contains("Beschreibung des Films"));
        assertTrue(toStringValue.contains("video-key-123"));
    }
}
