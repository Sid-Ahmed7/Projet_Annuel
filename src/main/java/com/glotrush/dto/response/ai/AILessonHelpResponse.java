package com.glotrush.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AILessonHelpResponse {

    private String title;

    private String explanation;

    private String visualAnchor;

    private List<String> examples;

    private String warning;
}
