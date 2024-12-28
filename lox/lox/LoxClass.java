package lox.lox;

import java.util.ArrayList;
import java.util.HashMap;

import lox.scanner.Token;

public class LoxClass implements LoxCallable{
   
    private Token name;
    HashMap<String,LoxFunction> methods;
    HashMap<String,LoxFunction> staticMethods;
    LoxClass superclass;

    LoxClass(Token name, LoxClass superclass, HashMap<String,LoxFunction> methods, HashMap<String,LoxFunction> staticMethods){
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
        this.staticMethods = staticMethods;
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
        } else
            if (superclass != null) return superclass.findMethod(name);

        return null;
    }

    LoxFunction findStaticMethod(String name){
        if (staticMethods.containsKey(name)){
            return staticMethods.get(name);
        } else
            if (superclass != null) return superclass.findStaticMethod(name);

        return null;
    }
}
