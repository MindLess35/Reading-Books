package com.senla.readingbooks.mapper;

import com.senla.readingbooks.document.UserDocument;
import com.senla.readingbooks.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElasticUserMapper {

    UserDocument toDocument(User user);


}
