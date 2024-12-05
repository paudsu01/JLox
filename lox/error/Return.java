package lox.error;

import lox.scanner.Token;

public class Return extends RuntimeError{
    public Object returnValue; 

    public Return(Token token, Object retValue){
        super(token, "Return");
        returnValue = retValue;
   }
}
