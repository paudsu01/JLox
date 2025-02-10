package lox.scanner;

import java.util.ArrayList;
import java.util.HashMap;

import lox.error.Error;

import static lox.scanner.TokenType.*;

public class LoxScanner{

    private final String LoxCode;
    private int line = 1;
    private int currentTokenStartIndex = 0;
    private int currentIndex = 0;
    private final ArrayList<Token> tokens = new ArrayList<>();
    private final static HashMap<String, TokenType> keywordToTokenType = new HashMap<>();

    // static initalization block 
    static{
        keywordToTokenType.put("class", CLASS);
        keywordToTokenType.put("fun", FUN);
        keywordToTokenType.put("var", VAR);
        keywordToTokenType.put("for", FOR);
        keywordToTokenType.put("while", WHILE);
        keywordToTokenType.put("print", PRINT);
        keywordToTokenType.put("return", RETURN);
        keywordToTokenType.put("if", IF);
        keywordToTokenType.put("else", ELSE);
        keywordToTokenType.put("super", SUPER);
        keywordToTokenType.put("this", THIS);
        keywordToTokenType.put("or", OR);
        keywordToTokenType.put("and", AND);
        keywordToTokenType.put("true", TRUE);
        keywordToTokenType.put("false", FALSE);
        keywordToTokenType.put("nil", NIL);
        keywordToTokenType.put("static", STATIC);
    }

    public LoxScanner(String LoxCode){
        this.LoxCode = LoxCode;
    }

    public ArrayList<Token> scanTokens(){
        while (!outOfTokensToConsume()){
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
            case '[':
                addToken(LEFT_BRACKET); break;
            case ']':
                addToken(RIGHT_BRACKET); break;
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
               currentTokenStartIndex = currentIndex+1;

               // consume characters until ending '"' not found
               // support multi line strings
               while (peekAhead() != '"'){
                    if (peekAhead() == '\n') line++;
                    currentIndex++;
               }

               if (cannotPeekAhead()) reportError(line, "Unterminated String");
               else{
                    String lexeme = LoxCode.substring(currentTokenStartIndex, currentIndex+1);
                    addToken(STRING, lexeme);
                    // consume ending '"'
                    currentIndex++;
                }
               break;

            default:
                if (isValidIdentifierChar(currentChar)){

                    while(isValidIdentifierChar(peekAhead())){
                        currentIndex++;
                    }

                    String lexeme = LoxCode.substring(currentTokenStartIndex, currentIndex+1);
                    TokenType lexemeToken = keywordToTokenType.get(lexeme);
                    if (lexemeToken == null){
                        addToken(IDENTIFIER);
                    } else {
                        addToken(lexemeToken);
                    }

                } else if (isNum(currentChar)){

                    int numOfDots=0;
                    while (isNumOrDot(peekAhead())){
                        if (peekAhead() == '.') numOfDots++;
                        currentIndex++;
                    }

                    if (numOfDots > 1) reportError(line, "Invalid number literal");

                    else if (LoxCode.charAt(currentIndex) == '.') reportError(line, "Number literal cannot end with a '.'");

                    else{ 
                        Double value = Double.parseDouble(LoxCode.substring(currentTokenStartIndex, currentIndex+1));
                        addToken(NUMBER, value);
                    }

                } else {
                    reportScannerError(line, String.format("Unknown character: %c", currentChar));
                }
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

    private boolean outOfTokensToConsume(){
        return currentIndex >= (LoxCode.length());
    }

    private boolean cannotPeekAhead(){
        return currentIndex >= (LoxCode.length() - 1);
    }
    // Get the next char
    private char peekAhead(){
        return (cannotPeekAhead()) ? '\0' : LoxCode.charAt(currentIndex+1);
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

    public ArrayList<Token> tokens(){
        return this.tokens;
    }

    private void reportError(int line, String message){
        currentIndex++;
        Error.reportError(line, message);
    }

    private void reportScannerError(int line, String message){
        currentIndex++;
        Error.reportScannerError(line, message);
    }
}