package lox.lox;

import java.util.ArrayList;

interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, ArrayList<Object> arguments);
}