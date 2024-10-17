package lox.scanner;

import java.util.ArrayList;
import lox.error.Error;

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
        char currentChar = LoxCode.charAt(currentIndex);

        switch (currentChar) {
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

            case '"':
               // consume starting '"'
               currentIndex++;
               currentTokenStartIndex = currentIndex;

               // consume characters until ending '"' not found
               // support multi line strings
               while ((!outOfTokens()) && peekAhead() != '"'){
                    if (peekAhead() == '\n') line++;
                    currentIndex++;
               }

               if (outOfTokens()) Error.reportError(line, "Unterminated String");
               else{
                    String lexeme = LoxCode.substring(currentTokenStartIndex, currentIndex+1);
                    addToken(STRING, lexeme);
                    // consume ending '"'
                    currentIndex++;
                }
               break;

            default:
                if (isValidIdentifierChar(currentChar)){

                } else if (isNum(currentChar)){

                    int numOfDots=0;
                    while (!outOfTokens() && isNumOrDot(peekAhead())){
                        if (peekAhead() == '.') numOfDots++;
                        currentIndex++;
                    }

                    if (numOfDots > 1) Error.reportError(line, "Invalid number literal");
                    else if (LoxCode.charAt(currentIndex) == '.') Error.reportError(line, "Number literal cannot end with a '.'");
                    else{ 
                        Double value = Double.parseDouble(LoxCode.substring(currentTokenStartIndex, currentIndex+1));
                        addToken(STRING, value);
                    }

                } else {
                Error.reportScannerError(line, String.format("Unknown character: %c", currentChar));}
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
        return currentIndex >= (LoxCode.length() -1);
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

    // checks if current char is a num
    private boolean isNum(char character){
        return character >= '0' && character <= '9';
    }

    private boolean isNumOrDot(char character){
        return (isNum(character)) || (character == '.');
    }

    private boolean isAlpha(char character){
        return (character >= 'a' && character <= 'z')
            || (character >= 'A' && character <= 'Z');
    }

    private boolean isValidIdentifierChar(char character){
        return (isAlpha(character)) || (character == '_');
    }
}