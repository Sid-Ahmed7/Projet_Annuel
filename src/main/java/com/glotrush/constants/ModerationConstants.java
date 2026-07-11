package com.glotrush.constants;

import java.util.List;

public class ModerationConstants {
    private ModerationConstants() {}

    public static final List<String> GERMAN_INSULT_BLOCK = List.of(
        "scheiße", "scheisse",
        "fick dich", "ficken",
        "arschloch",
        "wichser", "wichsen",
        "hurensohn",
        "hure",
        "vollidiot",
        "trottel",
        "blödmann", "blodmann",
        "drecksau",
        "schwuchtel",
        "fotze",
        "schwanz"
    );

    public static final List<String> FRENCH_INSULT_BLOCK = List.of(
        "fils de pute", "fdp",
        "enculer", "encule",
        "putain", "pute",
        "connard", "connasse", "conne",
        "salope", "salaud",
        "batard",
        "niquer", "nique ta",
        "ta mere",
        "pd", "pede",
        "bite", "couille",
        "debile",
        "abruti", "cretin",
        "imbecile"
    );


    public static final String DIACRITICS_REGEX = "\\p{InCombiningDiacriticalMarks}+";
    public static final String LETTERS_REGEX = "[^a-z ]";
    public static final String WHITESPACE_REGEX = "\\s+";
    public static final String DIGITS_REGEX = "[0]";
    public static final String DIGIT_THREE_REGEX = "[3€]";
    public static final String DIGIT_ONE_REGEX = "[1!]";
    public static final String DIGIT_FIVE_REGEX = "[5$]";
    public static final String DIGIT_FOUR_REGEX = "[4]";

}
