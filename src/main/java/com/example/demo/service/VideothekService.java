package com.example.demo.service;


import com.example.demo.entity.Film;
import com.example.demo.entity.Playlist;
import com.example.demo.repository.FilmRepository;
import com.example.demo.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideothekService {
    private final FilmRepository filmrepository;
    private final PlaylistRepository playlistrepository;

    public List<Film> getAllFilms() {
        return (List<Film>) filmrepository.findAll();
    }

    public List<Playlist> getAllFilmsFromPlaylist() {
        return (List<Playlist>) playlistrepository.findAll();
    }

    public Optional<Film> getFilmById(Long id){return filmrepository.findById(id);}

    public Optional<Playlist> getFilmByIdFromPlaylist(Long id){return playlistrepository.findById(id);}

    public Optional<Film> getFilmByName(String name){return filmrepository.findByName(name);}

    public Optional<Playlist> getFilmByNameFromPlaylist(String name){return playlistrepository.findByName(name);}

}
