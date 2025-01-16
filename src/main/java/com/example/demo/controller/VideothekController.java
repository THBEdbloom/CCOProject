package com.example.demo.controller;

import com.example.demo.entity.Film;
import com.example.demo.entity.Playlist;
import com.example.demo.repository.FilmRepository;
import com.example.demo.repository.PlaylistRepository;
import com.example.demo.service.VideothekService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@AllArgsConstructor
public class VideothekController {

    private final VideothekService videothekservice;

    FilmRepository filmRepo;
    PlaylistRepository playlRepo;

    @GetMapping("/")
    public String redirectToStartPage(){
        return "redirect:/videothek";
    }
    /**
     * http://localhost:8080/addfilm
     */
    @GetMapping("addfilm")
    public String showAddFilm(){
        return "addFilm";
    }

    /**
     * http://localhost:8080/videothek
     */
    @GetMapping("videothek")
    public String showStartPage(Model model){
        List<Film> films = videothekservice.getAllFilms();
        model.addAttribute("films", films);
        return "startPage";
    }

    /**
     * http://localhost:8080/playlist
     */
    @GetMapping("playlist")
    public String showPlaylistPage(Model model){
        List<Playlist> films = videothekservice.getAllFilmsFromPlaylist();
        model.addAttribute("playlists", films);
        return "playlistPage";
    }

    /**
     * http://localhost:8080/filmsId/id
     */
    @GetMapping("filmsId/{id}")
    public String showFilmDetailsId(@PathVariable("id") Long id, Model model){
        Film film = videothekservice.getFilmById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        //attribute name to access
        // notebook from template
        model.addAttribute("film", film);
        return "detailsFilm";
    }

    /**
     * http://localhost:8080/playlistId/id
     */
    @GetMapping("playlistId/{id}")
    public String showFilmDetailsIdFromPlaylist(@PathVariable("id") Long id, Model model){
        Playlist film = videothekservice.getFilmByIdFromPlaylist(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        //attribute name to access
        // notebook from template
        model.addAttribute("playlist", film);
        return "detailsPlaylist";
    }

    /**
     * http://localhost:8080/filmsName/name
     */
    @GetMapping("filmsName/{name}")
    public String showFilmDetailsName(@PathVariable("name") String name, Model model){
        Film film = videothekservice.getFilmByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        //attribute name to access
        // notebook from template
        model.addAttribute("film", film);
        return "detailsFilm";
    }

    /**
     * http://localhost:8080/playlistName/name
     */
    @GetMapping("playlistName/{name}")
    public String showFilmDetailsNameFromPlaylist(@PathVariable("name") String name, Model model){
        Playlist film = videothekservice.getFilmByNameFromPlaylist(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        //attribute name to access
        // notebook from template
        model.addAttribute("playlist", film);
        return "detailsPlaylist";
    }

    @GetMapping("/saveFilm")
    public String saveFilm(Film film){
        filmRepo.save(film);
        /*System.out.println(film);*/

        return "saveFilm";
    }

    @GetMapping("/saveFilmPlaylist")
    public String saveFilmPlaylist(Playlist playlist){
        playlRepo.save(playlist);
        /*System.out.println(film);*/

        return "saveFilmPlaylist";
    }

    @GetMapping("/deleteFilmPlaylist")
    public String deleteFilmPlaylist(Playlist playlist){
        playlRepo.delete(playlist);
        /*System.out.println(film);*/

        return "deleteFilmPlaylist";
    }
}
