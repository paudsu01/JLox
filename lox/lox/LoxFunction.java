package lox.lox;

import java.util.ArrayList;
import lox.error.Return;

class LoxFunction implements LoxCallable{

    private final FunctionStatement function;
    private Environment closure;
    private FuncType type;

    LoxFunction(FunctionStatement function, Environment closure, FuncType type){
        this.function = function;
        this.closure = closure;
        this.type = type;
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
        if (type == FuncType.INIT) return closure.getThis();
        return null;
    }

    @Override
    public String toString(){
        String name = (type == FuncType.FUNCTION) ? "fn" : "method";
        return String.format("<%s %s>", name, function.name.lexeme);
    }
    
    LoxFunction bind(LoxInstance instance){
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(this.function, environment, this.type);
    }

}
