package com.glotrush.services.ai;

import com.glotrush.dto.request.ai.AILessonHelpRequest;
import com.glotrush.dto.response.ai.AILessonHelpResponse;
import java.util.UUID;

public interface IAILessonHelpService {

    AILessonHelpResponse getLessonHelp(UUID accountId, AILessonHelpRequest request);
}
