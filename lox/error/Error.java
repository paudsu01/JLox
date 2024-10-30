package lox.error;

import static lox.scanner.TokenType.STRING;

import lox.scanner.Token;
import lox.scanner.TokenType;

public class Error{
	
	public static boolean hadError = false;

	public static void reportParserError(Token tokenGot, String message){
		reportError(tokenGot.line, String.format("Got %s, %s", tokenGot.type, message));
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
