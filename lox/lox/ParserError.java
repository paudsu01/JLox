package lox.lox;

import lox.scanner.Token;

public class ParserError extends RuntimeException{
    public Token token;
    public String message;

    ParserError(Token token, String message){
        this.token = token;
        this.message = message;
    }
}
