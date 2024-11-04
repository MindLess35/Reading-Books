package com.senla.readingbooks.controller.storage;

import com.senla.readingbooks.dto.MediaUrlsDto;
import com.senla.readingbooks.enums.MediaEntityType;
import com.senla.readingbooks.service.interfaces.storage.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/media/{id}")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpStatus> uploadMedia(@PathVariable Long id,
                                                  @RequestPart MultipartFile media,
                                                  @RequestParam MediaEntityType entityType) {
        String mediaUrl = mediaService.uploadMedia(id, media, entityType);
        return ResponseEntity.created(URI.create(mediaUrl)).build();
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteMediaByUrls(@PathVariable Long id,
                                                        @RequestBody @Validated MediaUrlsDto mediaUrlsDto,
                                                        @RequestParam MediaEntityType entityType) {
        mediaService.deleteMediaByUrls(id, mediaUrlsDto, entityType);
        return ResponseEntity.noContent().build();
    }


}