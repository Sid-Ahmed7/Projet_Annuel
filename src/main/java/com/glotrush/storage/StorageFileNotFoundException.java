package com.glotrush.storage;

public class StorageFileNotFoundException extends RuntimeException {
    public StorageFileNotFoundException(String key) {
        super("File not found: " + key);
    }
}
