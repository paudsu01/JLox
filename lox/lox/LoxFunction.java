package lox.lox;

import java.util.ArrayList;
import lox.error.Return;

class LoxFunction implements LoxCallable{

    private final FunctionStatement function;
    private Environment closure;

    LoxFunction(FunctionStatement function, Environment closure){
        this.function = function;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return function.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
        Environment environment = interpreter.environment;
        interpreter.environment = new Environment(closure);

        for (int i=0; i < arguments.size(); i++){
            interpreter.environment.define(function.parameters.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.visitBlockStatement((BlockStatement) function.body);
        } catch (Return ret) {
            interpreter.environment = environment;
            return ret.returnValue;
        }
        interpreter.environment = environment;
        return null;
    }

    @Override
    public String toString(){
        return String.format("<fn %s>", function.name.lexeme);
    }
    
}
