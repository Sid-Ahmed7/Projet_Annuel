package com.glotrush.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.LessonType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MistakeQuestionResponse {

    private UUID userMistakeId;
    private UUID questionId;
    private LessonType lessonType;
    private String front;
    private String frontLanguage;
    private String backLanguage;
    private String question;
    private List<String> options;
    private String item1;
    private List<String> items;

}
