package lox.error;

import lox.scanner.Token;

public class LoxError extends RuntimeException{
    public Token token;
    public String message;

    LoxError(Token token, String message){
        this.token = token;
        this.message = message;
    }
}