package com.senla.readingbooks.enums.book;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum Genre {
    FANTASY("fantasy"),
    FANTASTIC("fantastic"),
    SCIENCE_FICTION("science_fiction"),
    MYSTERY("mystery"),
    THRILLER("thriller"),
    ROMANCE("romance"),
    HORROR("horror"),
    SUPERNATURAL("supernatural"),
    FANFICTION("fanfiction"),
    LIT_RPG("lit_rpg"),
    REAL_RPG("real_rpg"),
    PROSE("prose"),
    ISEKAI("isekai"),
    ACTION("action"),
    ADVENTURE("adventure"),
    DRAMA("drama"),
    COMEDY("comedy"),
    DYSTOPIA("dystopia"),
    URBAN_FANTASY("urban_fantasy"),
    PARANORMAL("paranormal"),
    HISTORICAL_FICTION("historical_fiction");

    private final String value;

    Genre(String value) {
        this.value = value;
    }


    @JsonCreator
    public static Genre fromValue(String value) {
        for (Genre genre : Genre.values()) {
            if (genre.value.equalsIgnoreCase(value)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Unknown genre: " + value);
    }


}

