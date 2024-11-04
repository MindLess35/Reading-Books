package com.senla.readingbooks.service.interfaces.user;

import com.senla.readingbooks.dto.UserLibraryAsPageDto;
import com.senla.readingbooks.dto.UserLibraryFilterDto;
import com.senla.readingbooks.dto.user.UserLibraryAddDto;
import com.senla.readingbooks.dto.user.UserLibraryEditDto;
import com.senla.readingbooks.enums.book.LibrarySection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserLibraryService {

    Page<UserLibraryAsPageDto> findBooksInLibrary(UserLibraryFilterDto filter, Pageable pageable);

    void addBookToLibrary(UserLibraryAddDto dto);

    void deleteBookFromLibrary(UserLibraryEditDto dto);

    void updateLibrarySection(UserLibraryEditDto dto, LibrarySection section);
}
