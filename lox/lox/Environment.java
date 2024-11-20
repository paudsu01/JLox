package lox.lox;

import java.util.HashMap;

import lox.error.RuntimeError;
import lox.error.Error;

import lox.scanner.Token;

public class Environment {
    
    Environment superEnvironment = null;
    private final HashMap<String, Object> environment = new HashMap<>();

    // Init : Overloaded

    Environment(){
    }

    Environment(Environment superEnv){
        this.superEnvironment = superEnv;
    }

    // get, define and assign methods

    Object get(Token token){
        if (environment.containsKey(token.lexeme)) return environment.get(token.lexeme);
        if (superEnvironment != null) return superEnvironment.get(token);

        reportAndThrowUndefinedVariableError(token);
        // Unreachable
        return null;
    }

    void define(String name, Object value){
        environment.put(name, value);
    }

    void assign(Token name, Object newValue){

        if (environment.containsKey(name.lexeme)) environment.put(name.lexeme, newValue);
        else if (superEnvironment != null) superEnvironment.assign(name, newValue);
        
        reportAndThrowUndefinedVariableError(name);
    }

    // Error report methods

    void reportAndThrowUndefinedVariableError(Token token){
        // Undefined variable
        RuntimeError err = new RuntimeError(token, String.format("Undefined variable: %s", token.lexeme));
        Error.reportRuntimeError(err);
        throw err;

    }
}
