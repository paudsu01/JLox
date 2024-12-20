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
        LoxFunction init = this.findMethod("init");
        if (init != null) return init.arity();
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
        LoxInstance instance =  new LoxInstance(this);
        LoxFunction init = this.findMethod("init");
        if (init != null) init.bind(instance).call(interpreter, arguments);
        return instance;
    }

    LoxFunction findMethod(String name){
        if (methods.containsKey(name)){
            return methods.get(name);
        }
        return null;
    }
}
