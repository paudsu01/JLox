package lox.lox;

import java.util.ArrayList;
import lox.scanner.Token;

class LoxFunction implements LoxCallable{

    private final FunctionStatement function;

    LoxFunction(FunctionStatement function){ this.function = function;}

    @Override
    public int arity() {
        return function.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
        Environment environment = interpreter.environment;
        interpreter.environment = new Environment(environment);

        for (int i=0; i < arguments.size(); i++){
            interpreter.environment.define(function.parameters.get(i).lexeme, arguments.get(i));
        }
        interpreter.visitBlockStatement((BlockStatement) function.body);
        interpreter.environment = environment;
        return null;
    }

    @Override
    public String toString(){
        return String.format("<fn %s>", function.name.lexeme);
    }
    
}
