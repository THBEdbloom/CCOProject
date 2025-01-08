package com.example.demo.repository;

import com.example.demo.entity.Film;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.Optional;

@RepositoryDefinition(domainClass = Film.class, idClass = Long.class)
public interface FilmRepository extends CrudRepository<Film, Long> {
    Optional<Film> findByName(String name);
}
