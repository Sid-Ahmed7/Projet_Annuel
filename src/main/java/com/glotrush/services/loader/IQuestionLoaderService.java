package com.glotrush.services.loader;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.UserMistake;
import com.glotrush.mapping.model.LessonTypeMaps;

public interface IQuestionLoaderService {
    
    LessonTypeMaps loadQuestionMaps(List<UserMistake> mistakes);
    <T> Map<UUID, T> fetchById(Collection<UUID> ids, JpaRepository<T, UUID> repo, Function<T, UUID> getId);


}
