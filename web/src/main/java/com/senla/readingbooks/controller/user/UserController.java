package com.senla.readingbooks.controller.user;


import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.dto.UserFilterDto;
import com.senla.readingbooks.dto.UsersAsPageDto;
import com.senla.readingbooks.dto.auth.ChangePasswordDto;
import com.senla.readingbooks.dto.user.UserReadDto;
import com.senla.readingbooks.dto.user.UserUpdateDto;
import com.senla.readingbooks.projection.UserWithBooksCountProjection;
import com.senla.readingbooks.service.interfaces.user.ElasticUserService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Controller", description = "Controller for working with the user. The creation" +
                                             " of the user and the sing-in to the application is in the" +
                                             " Authentication Controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final ElasticUserService elasticUserService;

    @Operation(summary = "Returns representation of user")
    @GetMapping("/{id}")
    public ResponseEntity<UserReadDto> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }


    @Operation(summary = "Full-text search by username via elasticsearch")
    @GetMapping("/search")
    public Page<UserWithBooksCountProjection> searchUsersByUsername(FullTextSearchDto searchDto) {
        return elasticUserService.searchByUsername(searchDto);
    }

    @Operation(summary = "Returns a list of users with pagination and filtering")
    @GetMapping
    public ResponseEntity<Page<UsersAsPageDto>> getAllUsersByFilter(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 25) Pageable pageable,
            @Validated UserFilterDto filter) {

        return ResponseEntity.ok(userService.findUsersAsPage(pageable, filter));
    }

    @Operation(summary = "Returns updated representation of user")
    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #id")
    @PutMapping("/{id}")
    public ResponseEntity<UserReadDto> updateUser(@PathVariable("id") Long id,
                                                  @RequestBody @Validated UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateDto));
    }

    @Operation(summary = "Deletes the user")
    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #id")
    @DeleteMapping("{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Changes the user's password")
    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #id")
    @PatchMapping("{id}/password")
    public ResponseEntity<HttpStatus> changePassword(@PathVariable("id") Long id,
                                                     @RequestBody @Validated ChangePasswordDto changePasswordDto) {
        userService.changePassword(id, changePasswordDto);
        return ResponseEntity.ok().build();
    }

}

