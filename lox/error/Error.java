package lox.error;

public class Error{
	
	public static void reportScannerError(int line, String message){
		System.err.println(String.format("Line [%d] : %s", line, message));
	}

	public static void reportUsageError(){
        System.err.println("Usage: jlox\nUsage: jlox loxFile.lox");
	}
}
