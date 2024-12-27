package com.example.rest_api.controller;

import com.example.rest_api.database.model.resources.AlbumEntity;
import com.example.rest_api.database.model.resources.ImageEntity;
import com.example.rest_api.database.repository.resources.AlbumRepository;
import com.example.rest_api.database.repository.users.UserRepository;
import com.example.rest_api.security.AuthenticatedUser;
import com.example.rest_api.service.AlbumService;
import com.example.rest_api.service.ImageService;
import com.example.rest_api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/home")
public class HomeController {
    private final AlbumService albumService;
    private final UserService userService;
    private final ImageService imageService;

    private Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    public HomeController(AlbumService albumService, UserService userService, ImageService imageService) {
        this.albumService=albumService;
        this.userService=userService;
        this.imageService=imageService;

    }

    @GetMapping()
    public String home(Model model, Principal principal) {
        List<AlbumEntity> albums=albumService.findAll();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            // Since AuthenticatedUser contains email and other attributes
            model.addAttribute("username", authenticatedUser.getEmail());
        } else {
            // Fallback if principal is not AuthenticatedUser for some reason
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("albums",albums);
        return "user/home";
    }
    @GetMapping("/album/{id}")
    public String viewAlbum(@PathVariable Long id, Model model) {
        AlbumEntity album = albumService.findById(id);
        model.addAttribute("album", album);
        return "user/album_view";
    }
    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        // Fetch the image entity by ID (assuming you have an ImageService)
        ImageEntity image = imageService.findById(id);

        // Assuming the image is a byte array and you know the content type (e.g., JPEG)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or use IMAGE_PNG based on the actual image type

        return new ResponseEntity<>(image.getImage(), headers, HttpStatus.OK);
    }

}

