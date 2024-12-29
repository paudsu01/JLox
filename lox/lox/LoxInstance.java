package lox.lox;

import java.util.HashMap;

import lox.error.Error;
import lox.scanner.Token;

public class LoxInstance {
    private LoxClass loxClass;
    private HashMap<String, Object> fields= new HashMap<>();

    LoxInstance(LoxClass loxClass){
        this.loxClass = loxClass;
    }

    @Override
    public String toString(){
        return String.format("instance %s", loxClass);
    }

    void set(Token name, Object value){
        if (loxClass.methods.containsKey(name.lexeme)) throw Error.createRuntimeError(name, "Cannot set field to method name");
        else fields.put(name.lexeme, value);
    }

    Object get(Token name){
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        
        } else {
                LoxFunction method = loxClass.findMethod(name.lexeme);
                if (method != null) return method.bind(this);

                method = loxClass.findStaticMethod(name.lexeme);
                if (method != null) return method;
                else throw Error.createRuntimeError(name, "Undefined field");
        }
    }
}
