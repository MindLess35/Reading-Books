package com.senla.readingbooks.aspect;

import com.senla.readingbooks.enums.MediaEntityType;
import com.senla.readingbooks.service.interfaces.storage.MediaService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MediaAccessControlAspect extends CommonPointcut {
    private final MediaService mediaService;

    @Pointcut("within(com.senla.readingbooks.controller.storage.MediaController)")
    public void isMediaController() {
    }

    @Before(value = "isMediaController() && isPostOrDeleteMappingMethod() && args(entityId, *, mediaEntityType)", argNames = "entityId,mediaEntityType")
    public void checkAccessToEditMediaInEntity(Long entityId, MediaEntityType mediaEntityType) {
        mediaService.checkAccessToEditMediaInEntity(entityId, mediaEntityType);
    }

}

