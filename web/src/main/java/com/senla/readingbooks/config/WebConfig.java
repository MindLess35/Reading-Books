package com.senla.readingbooks.config;

import com.senla.readingbooks.enums.EntityType;
import com.senla.readingbooks.enums.ImageEntityType;
import com.senla.readingbooks.enums.MediaEntityType;
import com.senla.readingbooks.enums.book.TimeInterval;
import com.senla.readingbooks.enums.book.AccessType;
import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.LibrarySection;
import com.senla.readingbooks.enums.book.PublicationStatus;
import com.senla.readingbooks.enums.user.Gender;
import com.senla.readingbooks.enums.user.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(accessTypeConverter());
        registry.addConverter(timeIntervalConverter());
        registry.addConverter(publicationFilterStatusConverter());
        registry.addConverter(bookFormConverter());
        registry.addConverter(entityTypeConverter());
        registry.addConverter(genreConverter());
        registry.addConverter(genreListConverter());
        registry.addConverter(librarySectionConverter());
        registry.addConverter(mediaEntityTypeConverter());
        registry.addConverter(roleConverter());
        registry.addConverter(genderConverter());
        registry.addConverter(imageEntityTypeConverter());
    }

    @Bean
    public Converter<String, Role> roleConverter() {
        return new Converter<>() {
            @Override
            public Role convert(String source) {
                return Role.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, Gender> genderConverter() {
        return new Converter<>() {
            @Override
            public Gender convert(String source) {
                return Gender.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, MediaEntityType> mediaEntityTypeConverter() {
        return new Converter<>() {
            @Override
            public MediaEntityType convert(String source) {
                return MediaEntityType.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, ImageEntityType> imageEntityTypeConverter() {
        return new Converter<>() {
            @Override
            public ImageEntityType convert(String source) {
                return ImageEntityType.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, LibrarySection> librarySectionConverter() {
        return new Converter<>() {
            @Override
            public LibrarySection convert(String source) {
                return LibrarySection.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, List<Genre>> genreListConverter() {
        return new Converter<>() {
            @Override
            public List<Genre> convert(String source) {
                String cleanedSource = source.replaceAll("[\\[\\]\"]", "");
                return Arrays.stream(cleanedSource.split(","))
                        .map(Genre::fromValue)
                        .toList();
            }
        };
    }

    @Bean
    public Converter<String, Genre> genreConverter() {
        return new Converter<>() {
            @Override
            public Genre convert(String source) {
                return Genre.fromValue(source);
            }
        };
    }


    @Bean
    public Converter<String, AccessType> accessTypeConverter() {
        return new Converter<>() {
            @Override
            public AccessType convert(String source) {
                return AccessType.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, TimeInterval> timeIntervalConverter() {
        return new Converter<>() {
            @Override
            public TimeInterval convert(String source) {
                return TimeInterval.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, PublicationStatus> publicationFilterStatusConverter() {
        return new Converter<>() {
            @Override
            public PublicationStatus convert(String source) {
                return PublicationStatus.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, BookForm> bookFormConverter() {
        return new Converter<>() {
            @Override
            public BookForm convert(String source) {
                return BookForm.fromValue(source);
            }
        };
    }


    @Bean
    public Converter<String, EntityType> entityTypeConverter() {
        return new Converter<>() {
            @Override
            public EntityType convert(String source) {
                return EntityType.fromValue(source);
            }
        };
    }
}


