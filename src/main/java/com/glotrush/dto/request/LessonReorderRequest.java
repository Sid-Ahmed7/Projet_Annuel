package com.glotrush.dto.request;

import java.util.UUID;

public record LessonReorderRequest(UUID id, Integer orderIndex) {}
