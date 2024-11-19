package lox.error;

import lox.scanner.Token;

public class RuntimeError extends LoxError{

    public RuntimeError(Token token, String message){
        super(token, message);
    }
}

