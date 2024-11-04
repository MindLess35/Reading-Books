package com.senla.readingbooks.service.impl.user;


import com.senla.readingbooks.document.UserDocument;
import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.mapper.ElasticUserMapper;
import com.senla.readingbooks.projection.UserWithBooksCountProjection;
import com.senla.readingbooks.repository.elastic.ElasticUserRepository;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.service.interfaces.user.ElasticUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElasticUserServiceImpl implements ElasticUserService {
    private final ElasticUserRepository elasticUserRepository;
    private final UserRepository userRepository;
    private final ElasticUserMapper elasticUserMapper;

    @Override
    public void saveUserDocument(User user) {
        UserDocument userDocument = elasticUserMapper.toDocument(user);
        elasticUserRepository.save(userDocument);
    }

    @Override
    public void deleteById(Long id) {
        elasticUserRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserWithBooksCountProjection> searchByUsername(FullTextSearchDto searchDto) {
        PageRequest pageRequest = PageRequest.of(searchDto.pageNumber(), searchDto.pageSize());
        Page<UserDocument> userDocumentPage = elasticUserRepository.searchByUsername(searchDto.query(), pageRequest);

        if (userDocumentPage.isEmpty()) {
            return Page.empty(pageRequest);
        }
        List<Long> userIds = userDocumentPage.getContent().stream()
                .map(UserDocument::getId)
                .toList();

        Map<Long, Integer> userIdOrderIndexMap = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            userIdOrderIndexMap.put(userIds.get(i), i);
        }

        List<UserWithBooksCountProjection> result = userRepository.findUserDetailsWithBookCount(userIds).stream()
                .sorted(Comparator.comparingInt(projection -> userIdOrderIndexMap.get(projection.getId())))
                .toList();
        return new PageImpl<>(result, pageRequest, userDocumentPage.getTotalElements());
    }

}