package com.senla.readingbooks.aspect;

import com.senla.readingbooks.enums.ImageEntityType;
import com.senla.readingbooks.service.interfaces.storage.ImageService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ImageAccessControlAspect extends CommonPointcut {
    private final ImageService imageService;

    @Pointcut("within(com.senla.readingbooks.controller.storage.ImageController)")
    public void isImageController() {
    }

    @Before(value = """
            isImageController()
            && (isPostOrDeleteMappingMethod() || isPatchMappingMethod())
            && args(id, imageEntityType, ..)
            """, argNames = "id,imageEntityType")
    public void checkAccessToEditImageInEntity(Long id, ImageEntityType imageEntityType) {
        imageService.checkAccessToEditImageInEntity(id, imageEntityType);
    }

}

