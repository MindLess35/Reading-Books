package com.senla.readingbooks.aspect;

import org.aspectj.lang.annotation.Pointcut;

public abstract class CommonPointcut {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void isPutMappingMethod() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void isPostMappingMethod() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void isPatchMappingMethod() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void isDeleteMappingMethod() {
    }

    @Pointcut("isPutMappingMethod() || isDeleteMappingMethod()")
    public void isPutOrDeleteMappingMethod() {
    }

    @Pointcut("isPostMappingMethod() || isDeleteMappingMethod()")
    public void isPostOrDeleteMappingMethod() {
    }

    @Pointcut("isPutOrDeleteMappingMethod() || isPatchMappingMethod()")
    public void isPutOrPatchOrDeleteMappingMethod() {
    }

}

