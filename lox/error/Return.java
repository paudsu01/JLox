package lox.error;

import lox.scanner.Token;

public class Return extends RuntimeError{
    public Object returnValue; 
    public boolean noReturnValue; 

    public Return(Token token, Object retValue, boolean noReturnValue){
        super(token, "Return not allowed");
        returnValue = retValue;
        this.noReturnValue = noReturnValue;
   }
}
