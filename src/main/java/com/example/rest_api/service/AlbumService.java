package com.example.rest_api.service;

import com.example.rest_api.database.model.resources.AlbumEntity;
import com.example.rest_api.database.model.users.PermissionEntity;
import com.example.rest_api.database.model.users.RoleEntity;
import com.example.rest_api.database.repository.resources.AlbumRepository;
import com.example.rest_api.database.repository.users.PermissionRepository;
import com.example.rest_api.database.repository.users.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService {
    private final AlbumRepository albumRepository;


    @Autowired
    public AlbumService(AlbumRepository albumRepository) {

        this.albumRepository = albumRepository;
    }

    public List<AlbumEntity> findAll(){return this.albumRepository.findAll();}

    public AlbumEntity findById(Long id){
        return this.albumRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Image not  not found with ID: " + id));}
    public AlbumEntity findByName(String name) {
        return albumRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Album not found with name: " + name));
    }


    public void save(AlbumEntity albumEntity)
    {
        this.albumRepository.save(albumEntity);
    }
    public void delete(AlbumEntity albumEntity){this.albumRepository.delete(albumEntity);}

}
