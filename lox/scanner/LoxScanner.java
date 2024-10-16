package lox.scanner;

import java.util.ArrayList;
import static lox.scanner.TokenType.*;

public class LoxScanner{

    private final String LoxCode;
    private int line = 1;
    private int currentTokenStartIndex = 0;
    private int currentIndex = 0;
    private final ArrayList<Token> tokens = new ArrayList<>();


    // static initalization block 
    static{

    }

    public LoxScanner(String LoxCode){
        this.LoxCode = LoxCode;
    }

    public ArrayList<Token> scanTokens(){
        while (!outOfTokens()){
            currentTokenStartIndex = currentIndex;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){
        char current_char = LoxCode.charAt(currentIndex);

        switch (current_char) {
            // no need to tokenize whitespaces
            case '\n':
                line++;
            case ' ' :
            case '\r':
            case '\t':
                currentIndex++; break;

            // Handle single character symbols
            case '{':
                addToken(LEFT_BRACE); break;
            case '}':
                addToken(RIGHT_BRACE); break;
            case '(':
                addToken(LEFT_PAREN); break;
            case ')':
                addToken(RIGHT_PAREN); break;
            case ';':
                addToken(SEMICOLON); break;
            case '.':
                addToken(DOT); break;
            case ',':
                addToken(COMMA); break;

            // Handle arithmetic operators
            case '+':
                addToken(ADD); break;
            case '-':
                addToken(SUBTRACT); break;
            case '*':
                addToken(MULTIPLY); break;
            case '/':
                // Handle comments
               if (peekAhead() == '/'){
                    while (LoxCode.charAt(currentIndex)!= '\n'){
                        currentIndex++;
                    }
               }
               else {
                    addToken(DIVIDE);
               }
               break;
            
            case '>':
               addToken((matchNext('=')) ? GEQ : GT);
               break;

            case '<':
               addToken((matchNext('=')) ? LEQ : LT);
               break;

            case '=':
               addToken((matchNext('=')) ? EQ : ASSIGNMENT);
               break;

            case '!':
               addToken((matchNext('=')) ? NEQ : NOT);
               break;

            default:
                break;
        }
    }

    // Helper methods
    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String lexeme = LoxCode.substring(currentTokenStartIndex, ++currentIndex);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private boolean outOfTokens(){
        return currentIndex > LoxCode.length();
    }

    // Get the next char
    private char peekAhead(){
        return (outOfTokens()) ? '\0' : LoxCode.charAt(currentIndex+1);
    }

    // Consume the next char if the next char is the provided char 
    //      and return True
    // else return False
    private boolean matchNext(char nextChar){
        if (peekAhead() != nextChar) return false;
        currentIndex++;
        return true;
    }
}