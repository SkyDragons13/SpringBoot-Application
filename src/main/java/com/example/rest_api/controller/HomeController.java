package com.example.rest_api.controller;

import com.example.rest_api.database.model.resources.AlbumEntity;
import com.example.rest_api.database.model.resources.ImageEntity;
import com.example.rest_api.database.model.users.RoleEntity;
import com.example.rest_api.database.model.users.UserEntity;
import com.example.rest_api.security.AuthenticatedUser;
import com.example.rest_api.service.AlbumService;
import com.example.rest_api.service.ImageService;
import com.example.rest_api.service.RoleService;
import com.example.rest_api.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/home")
public class HomeController {
    private final AlbumService albumService;
    private final UserService userService;
    private final ImageService imageService;

    private final RoleService roleService;
    private UserEntity loggedUser;

    private Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    public HomeController(AlbumService albumService, UserService userService, ImageService imageService,RoleService roleService) {
        this.albumService=albumService;
        this.userService=userService;
        this.imageService=imageService;
        this.roleService=roleService;

    }

    @GetMapping()
    public String home(@RequestParam(name = "searchQuery", required = false) String searchQuery ,Model model, Principal principal) {
        List<AlbumEntity> albums=albumService.findAll();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            albums = albums.stream()
                    .filter(album -> album.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                    .toList();
        }

        if (principal instanceof AuthenticatedUser authenticatedUser) {
            // Since AuthenticatedUser contains email and other attributes
            model.addAttribute("username", authenticatedUser.getEmail());
            loggedUser=userService.findByEmail(authenticatedUser.getEmail());
        } else {
            // Fallback if principal is not AuthenticatedUser for some reason
            model.addAttribute("username", principal.getName());
            loggedUser=userService.findByEmail(principal.getName());
        }
        for(AlbumEntity album:albums)
        {
            boolean canAccess=checkUserAccess(loggedUser,album);
            album.setCanAccess(canAccess);
        }
        model.addAttribute("albums",albums);
        model.addAttribute("searchQuery", searchQuery);
        return "user/home";
    }
    @GetMapping("/create")
    public String createAlbumForm(Model model) {
        model.addAttribute("album", new AlbumEntity());
        return "user/create_album";
    }

    @PostMapping("/create")
    public String createAlbum(@ModelAttribute AlbumEntity album, BindingResult result, Model model)
    {
        try {
            if (albumService.findByName(album.getName()) != null) {
                model.addAttribute("error", "An album with this name already exists.");
                model.addAttribute("album", album);
                return "user/create_album";
            }
        } catch (IllegalArgumentException ignored) {}

        albumService.save(album);

        String albumName=album.getName();
        String adminRole=albumName+"_Album_Admin";
        String viewRole=albumName+"_Album";

        String resourcePath="/home/album/"+albumName+"/**";

        RoleEntity admin=roleService.createRoleWithPermissions(adminRole,List.of("POST","DELETE","GET"),resourcePath);
        RoleEntity view=roleService.createRoleWithPermissions(viewRole,List.of("GET"),resourcePath);

        loggedUser.addRole(admin);
        admin.getUsers().add(loggedUser);
        userService.save(loggedUser);
        // this part refreshes the authorities for the authenticated user without the need of a new log in
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
        updatedAuthorities.add(new SimpleGrantedAuthority(admin.getName()));

        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return "redirect:/home";

    }
    @GetMapping("/album/{name}/view")
    public String viewAlbum(@PathVariable String name, Model model) {
        AlbumEntity album = albumService.findByName(name);
        boolean isAdmin = userService.hasRole(loggedUser, name + "_Album_Admin");
        model.addAttribute("isAdmin",isAdmin);
        model.addAttribute("album", album);

        return "user/view_album";
    }
    @DeleteMapping("/album/{name}/view")
    public String deleteAlbum(@PathVariable String name) {
        AlbumEntity album = albumService.findByName(name);
        albumService.delete(album);
        String adminRoleName = name + "_Album_Admin";
        String viewRoleName = name + "_Album";

        Optional<RoleEntity> adminRole = roleService.findByName(adminRoleName);
        Optional<RoleEntity> viewRole = roleService.findByName(viewRoleName);

        if (adminRole.isPresent()) {
            roleService.deleteById(adminRole.get().getId());
        }
        if (viewRole.isPresent()) {
            roleService.deleteById(viewRole.get().getId());
        }

        return "redirect:/home";
    }

    @GetMapping("/album/{name}/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String name,@PathVariable Long id) {
        ImageEntity image = imageService.findById(id);
        AlbumEntity album = albumService.findByName(name);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or use IMAGE_PNG based on the actual image type

        return new ResponseEntity<>(image.getImage(), headers, HttpStatus.OK);
    }
    @PostMapping("/album/{name}/add-image")
    public String addImage(@PathVariable String name, @RequestParam("file") MultipartFile file, @RequestParam("name") String imageName, Principal principal) {
        try {
            AlbumEntity album = albumService.findByName(name);

            ImageEntity image = new ImageEntity();
            image.setName(imageName);
            image.setImage(file.getBytes()); // May throw IOException
            image.setAlbum(album);
            imageService.save(image);

            return "redirect:/home/album/" + name+ "/view";
        } catch (IOException e) {
            logger.error("Error occurred while reading file bytes", e);

            return "redirect:/home/album/" + name + "?error=upload_failed";
        }
    }
    @Transactional(transactionManager = "resourcesTransactionManager")
    @DeleteMapping("/album/{name}/images/{id}")
    public String deleteImage(@PathVariable String name, @PathVariable Long id) {
        ImageEntity image = imageService.findById(id);
        AlbumEntity album = albumService.findByName(name);

        imageService.delete(image);
        album.getImages().remove(image);
        albumService.save(album);
        return "redirect:/home/album/"+album.getName()+"/view";
    }

    private boolean checkUserAccess(UserEntity user,AlbumEntity album)
    {
        String albumName=album.getName();
        String adminRole=albumName+"_Album_Admin";
        String viewRole=albumName+"_Album";

        for(RoleEntity role : user.getRoles())
        {
            if (role.getName().equals(viewRole) || role.getName().equals(adminRole)) {
                return true;
            }
        }
        return false;
    }
}

