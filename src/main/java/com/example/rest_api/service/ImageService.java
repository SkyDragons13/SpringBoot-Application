package com.example.rest_api.service;

import com.example.rest_api.database.model.resources.AlbumEntity;
import com.example.rest_api.database.model.resources.ImageEntity;
import com.example.rest_api.database.repository.resources.AlbumRepository;
import com.example.rest_api.database.repository.resources.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {
    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository=imageRepository;
    }

    public List<ImageEntity> findAll(){return this.imageRepository.findAll();}

    public ImageEntity findById(Long id){
        return this.imageRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Image not  not found with ID: " + id));}

    public void delete(ImageEntity image)
    {
        this.imageRepository.delete(image);
    }
    public void save(ImageEntity image)
    {
        this.imageRepository.save(image);
    }
}