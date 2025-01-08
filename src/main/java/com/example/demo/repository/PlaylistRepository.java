package com.example.demo.repository;

import com.example.demo.entity.Playlist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.Optional;

@RepositoryDefinition(domainClass = Playlist.class, idClass = Long.class)
public interface PlaylistRepository extends CrudRepository<Playlist, Long> {
    Optional<Playlist> findByName(String name);
}
