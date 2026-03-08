package com.glotrush.utils;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;


public class LocaleUtils {

    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
    
}
