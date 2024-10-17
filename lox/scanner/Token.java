package lox.scanner;

public class Token{

    final String lexeme;
    final Object value;
    final int line;
    final TokenType type;

    public Token(TokenType type, String lexeme, Object value, int line){

        this.type = type;
        this.lexeme = lexeme;
        this.value = value;
        this.line = line;

        return;
    }

    @Override
    public String toString(){
        return String.format("Line [%d] : %-15s %s", line, type.toString(), lexeme);
    }
}