package com.senla.readingbooks.service.impl.user;

import com.senla.readingbooks.dto.UserLibraryAsPageDto;
import com.senla.readingbooks.dto.UserLibraryFilterDto;
import com.senla.readingbooks.dto.user.UserLibraryAddDto;
import com.senla.readingbooks.dto.user.UserLibraryEditDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.entity.user.UserLibrary;
import com.senla.readingbooks.enums.book.LibrarySection;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.repository.jpa.user.UserLibraryRepository;
import com.senla.readingbooks.service.interfaces.book.BookService;
import com.senla.readingbooks.service.interfaces.user.UserLibraryService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLibraryServiceImpl implements UserLibraryService {
    private final UserLibraryRepository userLibraryRepository;
    private final UserService userService;
    private final BookService bookService;
    private static final String LIBRARY_NOT_FOUND = "User library for the user with id [%d] and books with id [%d] not found";

    @Override
    public Page<UserLibraryAsPageDto> findBooksInLibrary(UserLibraryFilterDto filter, Pageable pageable) {
        return userLibraryRepository.findUserLibrariesAsPage(filter, pageable);
    }

    @Override
    @Transactional
    public void addBookToLibrary(UserLibraryAddDto dto) {
        Long userId = dto.userId();
        Long bookId = dto.bookId();
        User user = userService.findById(userId);
        Book book = bookService.findByIdLazy(bookId);

        if (userLibraryRepository.existsByUserIdAndBookId(userId, bookId))
            throw new BadRequestBaseException("Book with id %d for the user with id %d has already been added to the library"
                    .formatted(bookId, userId));

        UserLibrary userLibrary = UserLibrary.builder()
                .user(user)
                .book(book)
                .section(dto.section())
                .build();
        userLibraryRepository.save(userLibrary);
    }

    @Override
    @Transactional
    public void deleteBookFromLibrary(UserLibraryEditDto dto) {
        Long userId = dto.userId();
        Long bookId = dto.bookId();
        UserLibrary userLibrary = userLibraryRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException(LIBRARY_NOT_FOUND.formatted(userId, bookId)));
        userLibraryRepository.delete(userLibrary);
    }

    @Override
    @Transactional
    public void updateLibrarySection(UserLibraryEditDto dto, LibrarySection section) {
        Long userId = dto.userId();
        Long bookId = dto.bookId();
        UserLibrary userLibrary = userLibraryRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException(LIBRARY_NOT_FOUND.formatted(userId, bookId)));

        if (userLibrary.getSection() == section)
            throw new BadRequestBaseException(
                    "Book with id [%d] has already been added to the library of the user with id [%d] in the section [%s]"
                            .formatted(bookId, userId, section));

        userLibrary.setSection(section);
        userLibraryRepository.save(userLibrary);
    }

}