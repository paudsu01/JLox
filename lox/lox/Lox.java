package lox.lox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.ArrayList;

import lox.error.Error;
import lox.scanner.*;

public class Lox{

    public static void main(String[] commandLineArguments) throws IOException{

        if (commandLineArguments.length == 1){
            runFile(commandLineArguments[0]);

        } else if (commandLineArguments.length == 0){
            runPrompt();

        } else {
            Error.reportUsageError();
        }
    }

    private static void runFile(String filePath) throws IOException{
        String entire_file = new String(Files.readAllBytes(Paths.get(filePath)));
        run(entire_file, new Environment());
    }

    private static void runPrompt(){
        Scanner scanner = new Scanner(System.in);
        String user_input;

        System.out.println("Jlox Interpreter v2.1.0. Type 'exit' to exit the interpreter.");
        Environment env = new Environment();

        while (true){
            System.out.print(">> ");
            user_input = scanner.nextLine();

            if (user_input.equals("exit")) break;

            run(user_input, env);
            Error.hadError = false;  
            Error.hadRuntimeError = false;  
        }

        scanner.close();
    }

    private static void run(String input, Environment env){
        LoxScanner scanner = new LoxScanner(input);
        if (Error.hadError) return;

        Parser parser = new Parser(scanner.scanTokens());
        ArrayList<Statement> statements = parser.parse();
        if (Error.hadError) return;

        Interpreter interpreter = new Interpreter(statements, env);

        Resolver resolver = new Resolver(interpreter, statements);
        resolver.resolve();

        if (Error.hadError) return;

        interpreter.interpret();

        return;
    }

}