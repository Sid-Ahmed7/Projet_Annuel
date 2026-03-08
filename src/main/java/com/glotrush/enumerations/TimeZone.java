package com.glotrush.enumerations;

import lombok.Getter;

@Getter
public enum TimeZone {
    UTC("UTC"),
    ECT("Europe/Paris"), // Central European Time
    EET("Europe/Bucharest"), // Eastern European Time
    ART("Africa/Cairo"), // (Arabic) Egypt Standard Time
    EAT("Africa/Addis_Ababa"), // Eastern African Time
    MET("Asia/Tehran"), // Middle East Time
    NET("Asia/Yerevan"), // Near East Time
    PLT("Asia/Karachi"), // Pakistan Lahore Time
    IST("Asia/Kolkata"), // India Standard Time
    BST("Asia/Dhaka"), // Bangladesh Standard Time
    VST("Asia/Ho_Chi_Minh"), // Vietnam Standard Time
    CTT("Asia/Shanghai"), // China Taiwan Time
    JST("Asia/Tokyo"), // Japan Standard Time
    ACT("Australia/Darwin"), // Australia Central Time
    AET("Australia/Sydney"), // Australia Eastern Time
    SST("Pacific/Guadalcanal"), // Solomon Standard Time
    NST("Pacific/Auckland"), // New Zealand Standard Time
    MIT("Pacific/Apia"), // Midway Islands Time
    HST("Pacific/Honolulu"), // Hawaii Standard Time
    AST("America/Anchorage"), // Alaska Standard Time
    PST("America/Los_Angeles"), // Pacific Standard Time
    MST("America/Denver"), // Mountain Standard Time
    CST("America/Chicago"), // Central Standard Time
    EST("America/New_York"), // Eastern Standard Time
    IET("America/Indiana/Indianapolis"), // Indiana Eastern Standard Time
    PRT("America/Halifax"), // Puerto Rico and US Virgin Islands Time
    CNT("America/St_Johns"), // Canada Newfoundland Time
    AGT("America/Argentina/Buenos_Aires"), // Argentina Standard Time
    BET("America/Sao_Paulo"), // Brazil Eastern Time
    CAT("Africa/Harare"); // Central African Time

    private final String zoneId;

    TimeZone(String zoneId) {
        this.zoneId = zoneId;
    }
}
