package com.senla.readingbooks.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PredicateBuilder {
    private final List<Predicate> predicates = new ArrayList<>();

    public static PredicateBuilder builder() {
        return new PredicateBuilder();
    }

    public <T> PredicateBuilder add(T value, Function<T, Predicate> function) {
        if (value != null) {
            predicates.add(function.apply(value));
        }
        return this;
    }

    public Predicate buildAnd(CriteriaBuilder cb) {
        return cb.and(predicates.toArray(Predicate[]::new));
    }

    public Predicate buildOr(CriteriaBuilder cb) {
        return cb.or(predicates.toArray(Predicate[]::new));
    }
}