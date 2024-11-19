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

    Object get(Token token){
        if (environment.containsKey(token.lexeme)) return environment.get(token.lexeme);
        
        // Undefined variable
        RuntimeError err = new RuntimeError(token, String.format("Undefined variable: %s", token.lexeme));
        Error.reportRuntimeError(err);
        throw err;
    }

    void add(String name, Object value){
        environment.put(name, value);
    }
}
