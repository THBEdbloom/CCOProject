package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "playlist")


public class Playlist {
    @Id
    private long id;

    private int laenge;

    private String name;
    private String description;
}
