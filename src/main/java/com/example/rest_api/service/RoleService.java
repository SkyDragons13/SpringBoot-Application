package com.example.rest_api.service;

import com.example.rest_api.database.model.users.PermissionEntity;
import com.example.rest_api.database.model.users.RoleEntity;
import com.example.rest_api.database.model.users.UserEntity;
import com.example.rest_api.database.repository.users.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void save(RoleEntity role) {
        this.roleRepository.save(role);
    }

    public Boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    public Optional<RoleEntity> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public List<RoleEntity> findAll() {
        return roleRepository.findAll();
    }
    public RoleEntity findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
    }
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }
    public RoleEntity createRoleWithPermissions(String roleName, List<String> permissions, String resourcePath) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        RoleEntity savedRole=roleRepository.save(role);
        for (String permission : permissions)
        {
            PermissionEntity perm = new PermissionEntity();
            perm.setHttpMethod(permission);
            perm.setUrl(resourcePath);
            perm.setRole(savedRole);
            savedRole.getPermissions().add(perm);
        }

        return roleRepository.save(savedRole);
    }


}
