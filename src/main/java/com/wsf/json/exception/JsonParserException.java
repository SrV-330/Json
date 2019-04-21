package com.wsf.json.exception;

public class JsonParserException extends RuntimeException{
    public JsonParserException() {
    }

    public JsonParserException(String message) {
        super(message);
    }
}
