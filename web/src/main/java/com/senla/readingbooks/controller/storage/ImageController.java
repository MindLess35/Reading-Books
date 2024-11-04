package com.senla.readingbooks.controller.storage;

import com.senla.readingbooks.enums.ImageEntityType;
import com.senla.readingbooks.service.interfaces.storage.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images/{id}")
public class ImageController {
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpStatus> uploadImage(@PathVariable Long id,
                                                  @RequestParam ImageEntityType imageEntityType,
                                                  @RequestPart MultipartFile image) {
        String imageUrl = imageService.uploadImage(image, id, imageEntityType);
        return ResponseEntity.created(URI.create(imageUrl)).build();
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpStatus> updateImage(@PathVariable Long id,
                                                  @RequestParam ImageEntityType imageEntityType,
                                                  @RequestPart MultipartFile image) {
        String imageUrl = imageService.updateImage(image, id, imageEntityType);
        return ResponseEntity.ok().location(URI.create(imageUrl)).build();
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteImage(@PathVariable Long id,
                                                  @RequestParam ImageEntityType imageEntityType) {
        imageService.deleteImage(id, imageEntityType);
        return ResponseEntity.noContent().build();
    }
}