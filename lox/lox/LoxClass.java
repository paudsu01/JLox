package lox.lox;

import java.util.ArrayList;
import java.util.HashMap;

import lox.scanner.Token;

public class LoxClass implements LoxCallable{
   
    private Token name;
    HashMap<String,LoxFunction> methods;

    LoxClass(Token name, HashMap<String,LoxFunction> methods){
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString(){
        return String.format("<class %s>", name.lexeme);
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
        return new LoxInstance(this);
    }
}
