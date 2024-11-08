package lox.error;

import lox.scanner.Token;
import lox.lox.ParserError;

public class Error{
	
	public static boolean hadError = false;
	public static boolean hadRuntimeError = false;

	public static void reportParserError(ParserError err){
		hadRuntimeError = true;
		reportError(err.token.line, String.format("Got %s, %s", err.token.type, err.message));
	}

	public static void reportOperandError(Token token, String message){
		reportError(token.line, String.format("%s with %s operator", message, token.lexeme));
	}

	public static void reportScannerError(int line, String message){
		reportError(line, message);
	}

	public static void reportUsageError(){
		hadError = true;
        System.err.println("Usage: jlox\nUsage: jlox loxFile.lox");
	}
  
	public static void reportError(int line, String message){
		hadError = true;
		System.err.println(String.format("Line [%d] : %s", line, message));
	}
}
