package com.example.rest_api.database.repository.resources;

import com.example.rest_api.database.model.resources.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ImageRepository extends JpaRepository<ImageEntity,Long> {
}
