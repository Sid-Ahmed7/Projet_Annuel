package com.glotrush.utils;

import java.text.Normalizer;
import com.glotrush.constants.ModerationConstants;

public class ModerationUtils {

    private ModerationUtils() {}

    public static String normalizeWords(String text) {
        String normalizedForm = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedForm
            .replaceAll(ModerationConstants.DIACRITICS_REGEX, "")
            .toLowerCase()
            .replace("ß", "ss")
            .replaceAll(ModerationConstants.DIGITS_REGEX, "o")
            .replaceAll(ModerationConstants.DIGIT_THREE_REGEX, "e")
            .replaceAll(ModerationConstants.DIGIT_ONE_REGEX, "i")
            .replaceAll(ModerationConstants.DIGIT_FIVE_REGEX, "s")
            .replaceAll(ModerationConstants.DIGIT_FOUR_REGEX, "a")
            .replaceAll(ModerationConstants.LETTERS_REGEX, "")
            .replaceAll(ModerationConstants.WHITESPACE_REGEX, " ")
            .trim();
    }

    public static String normalize(String text) {
        return normalizeWords(text).replaceAll(" ", "");
    }
}
