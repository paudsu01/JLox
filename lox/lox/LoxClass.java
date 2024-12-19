package lox.lox;

import java.util.ArrayList;
import lox.scanner.Token;

public class LoxClass {
   
    private Token name;
    private ArrayList<Statement> methods;

    LoxClass(Token name, ArrayList<Statement> methods){
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString(){
        return String.format("class < %s >", name.lexeme);
    }
}
