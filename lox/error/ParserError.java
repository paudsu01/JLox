package lox.error;

import lox.scanner.Token;

public class ParserError extends LoxError{
    public ParserError(Token token, String message){
        super(token, message);
    }
}
