package com.senla.readingbooks.event;

import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.user.User;

import java.util.List;

public record BookCreatedEvent(Book book, List<User> users) {
}
