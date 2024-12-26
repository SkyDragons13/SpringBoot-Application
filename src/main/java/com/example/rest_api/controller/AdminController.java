package com.example.rest_api.controller;

import com.example.rest_api.database.model.PermissionEntity;
import com.example.rest_api.database.model.Role;
import com.example.rest_api.database.model.RoleEntity;
import com.example.rest_api.database.model.UserEntity;
import com.example.rest_api.service.PermissionService;
import com.example.rest_api.service.RoleService;
import com.example.rest_api.service.UrlService;
import com.example.rest_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("admin")
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UrlService urlService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService, PermissionService permissionService, UrlService urlService) {
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.urlService = urlService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("welcomeMessage", "Hello Admin");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/users/{id}/update")
    public String updateUserForm(@PathVariable Long id, Model model){
        UserEntity user= userService.findById(id);
        model.addAttribute("user",user);
        model.addAttribute("roles",roleService.findAll());
        return "admin/edit-user";
    }
    @PostMapping("users/{id}/update/add")
    public String addRoleToUser(@PathVariable Long id,@RequestParam Long roleId){
        UserEntity user=userService.findById(id);
        RoleEntity role= roleService.findById(roleId);
        if(!user.getRoles().contains(role))
        {
            user.addRole(role);
            role.getUsers().add(user);
            userService.save(user);
        }
        return "redirect:/admin/users/{id}/update";
    }
    @PostMapping("users/{userId}/update/{roleId}/delete")
    public String removeRoleFromUser(@PathVariable Long userId,@PathVariable Long roleId)
    {
        UserEntity user=userService.findById(userId);
        RoleEntity role= roleService.findById(roleId);
        if(user.getRoles().contains(role))
        {
            user.getRoles().remove(role);
            role.getUsers().remove(user);
            userService.save(user);
        }
        return "redirect:/admin/users/{userId}/update";
    }

    @GetMapping("/roles")
    public String roleManagement(Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "admin/roles";
    }
    @GetMapping("/roles/create")
    public String createRoleForm(Model model) {
        model.addAttribute("role", new RoleEntity());
        return "admin/create-role";
    }

    @PostMapping("/roles/create")
    public String createRole(@ModelAttribute RoleEntity role) {
        roleService.save(role);
        return "redirect:/admin/roles";
    }


    @GetMapping("/roles/{id}/edit")
    public String editRole(@PathVariable Long id, Model model) {
        RoleEntity role = roleService.findById(id);
        model.addAttribute("role", role);
        model.addAttribute("permissions", role.getPermissions());
        model.addAttribute("httpMethods", getHttpMethods());
        model.addAttribute("urls", urlService.getAllUrls());
        return "admin/edit-role";
    }
    @PostMapping("/roles/{id}/edit")
    public String updateRoleName(@PathVariable Long id, @RequestParam String name) {
        RoleEntity role = roleService.findById(id);
        role.setName(name);
        roleService.save(role);
        return "redirect:/admin/roles/" + id + "/edit";
    }

    @GetMapping("/roles/{id}/edit/permissions/create")
    public String createPermissionForm(@PathVariable Long id, Model model) {
        RoleEntity role = roleService.findById(id);
        model.addAttribute("role", role);
        model.addAttribute("httpMethods", getHttpMethods());
        model.addAttribute("urls", urlService.getAllUrls());
        model.addAttribute("permission", new PermissionEntity());
        return "admin/create-permission";
    }

    @PostMapping("/roles/{id}/edit/permissions/create")
    public String createPermission(@PathVariable Long id,
                                   @ModelAttribute PermissionEntity permission,
                                   @RequestParam String httpMethod,
                                   @RequestParam String url) {

        RoleEntity role = roleService.findById(id);
        permission.setRole(role);
        permission.setHttpMethod(httpMethod);
        permission.setUrl(url);

        permissionService.save(permission);

        return "redirect:/admin/roles/" + id + "/edit";
    }

    @GetMapping("/permissions/{id}/edit")
    public String editPermissionForm(@PathVariable Long id, Model model) {
        PermissionEntity permission = permissionService.findById(id);
        model.addAttribute("permission", permission);
        model.addAttribute("httpMethods", getHttpMethods());
        model.addAttribute("urls", urlService.getAllUrls());
        return "admin/edit-permission";
    }

    @PostMapping("/permissions/{id}/edit")
    public String editPermission(@PathVariable Long id,
                                 @ModelAttribute PermissionEntity updatedPermission) {

        PermissionEntity existingPermission = permissionService.findById(id);
        existingPermission.setHttpMethod(updatedPermission.getHttpMethod());
        existingPermission.setUrl(updatedPermission.getUrl());

        permissionService.save(existingPermission);

        return "redirect:/admin/roles/" + existingPermission.getRole().getId() + "/edit";
    }

    @PostMapping("/permissions/{id}/delete")
    public String deletePermission(@PathVariable Long id) {
        PermissionEntity permission = permissionService.findById(id);
        RoleEntity role = permission.getRole();
        role.getPermissions().remove(permission);
        permissionService.delete(permission);
        roleService.save(role);

        return "redirect:/admin/roles/" + role.getId() + "/edit";
    }



    @PostMapping("/roles/{id}/delete")
    public String deleteRole(@PathVariable Long id) {
        roleService.deleteById(id);
        return "redirect:/admin/roles";
    }
    private List<String> getHttpMethods() {
        return Arrays.asList("GET", "POST", "PATCH", "DELETE");
    }
}