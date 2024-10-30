package lox.scanner;

public class Token{

    final public String lexeme;
    final public Object value;
    final public int line;
    final public TokenType type;

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