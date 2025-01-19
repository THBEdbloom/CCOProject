package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveFilmDTO {
    String name;
    int laenge;
    String description;
    String videoKey;
}
