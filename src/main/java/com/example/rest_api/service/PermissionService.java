package com.example.rest_api.service;

import com.example.rest_api.database.model.PermissionEntity;
import com.example.rest_api.database.model.RoleEntity;
import com.example.rest_api.database.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void save(PermissionEntity permissionEntity) {
        this.permissionRepository.save(permissionEntity);
    }
    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }
    public void delete(PermissionEntity entity)
    {
        permissionRepository.delete(entity);
    }
    public PermissionEntity findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
    }
}
