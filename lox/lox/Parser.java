package lox.lox;

import lox.error.Error;
import lox.error.ParserError;

import lox.scanner.Token;
import lox.scanner.TokenType;

import static lox.scanner.TokenType.*;

import java.util.ArrayList;;

public class Parser {
    
    ArrayList<Token> tokens;
    private int currentToken=0;

    public Parser(ArrayList<Token> tokens){
        this.tokens = tokens;
    }

    public ArrayList<Statement> parse(){

        ArrayList<Statement> statements = new ArrayList<>();
        while (!noMoreTokensToConsume()){
            if (getCurrentToken().type == TokenType.EOF) break;
                statements.add(parseDeclaration());
        }
        return statements;

    }

    // Methods for grammar definitions

    // declaration -> varDec | statement
    private Statement parseDeclaration(){
       try{
            if (matchCurrentToken(TokenType.VAR)) return parseVarDeclaration();
            else return parseStatement(); 
       } catch (ParserError err){
            synchronize();
            return null;
       }
    }

    // varDec -> "var" IDENTIFIER ("=" expression)? ";"
    private Statement parseVarDeclaration() {

       consumeToken(TokenType.VAR);
       if (! matchCurrentToken(TokenType.IDENTIFIER)) consumeToken(TokenType.IDENTIFIER);

       VariableExpression varName = (VariableExpression) parsePrimary();

       Expression initializer = null;
       if (matchCurrentToken(TokenType.ASSIGNMENT)) {
            consumeToken(TokenType.ASSIGNMENT);
            initializer = parseExpression();
       }

       consumeToken(TokenType.SEMICOLON);
       return new VarDecStatement(varName.name, initializer);

    }


    // statement -> expresssionStatment | printStatement
    private Statement parseStatement(){
        if (matchCurrentToken(TokenType.PRINT)) return parsePrintStatement();
        else return parseExpressionStatement();
    }

    // printStatement -> "print" expression ";"
    private Statement parsePrintStatement(){

        consumeToken(TokenType.PRINT);
        Expression expr = parseExpression();
        consumeToken(TokenType.SEMICOLON);

        return new PrintStatement(expr);
    }

    // expressionStatement -> expression ";"
    private Statement parseExpressionStatement(){

        Expression expr = parseExpression();
        consumeToken(TokenType.SEMICOLON);
        return new ExpressionStatement(expr);
    }

    // expression -> equality
    private Expression parseExpression(){
        return parseEquality();
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )*
    private Expression parseEquality(){
        Expression expression = parseComparison();

        while (matchCurrentToken(TokenType.NEQ, TokenType.EQ)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseComparison();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }
    
    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* 
    private Expression parseComparison(){
        Expression expression = parseTerm();

        while (matchCurrentToken(TokenType.GEQ, TokenType.GT, TokenType.LEQ, TokenType.LT)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseTerm();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    // term -> factor ( ( "-" | "+" ) factor )*
    private Expression parseTerm(){
        Expression expression = parseFactor();

        while (matchCurrentToken(TokenType.SUBTRACT, TokenType.ADD)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseFactor();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    // factor -> unary (("*" | "/") unary)*
    private Expression parseFactor(){
        Expression expression = parseUnary();

        while (matchCurrentToken(TokenType.MULTIPLY, TokenType.DIVIDE)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseUnary();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

     // unary -> ( "!" | "-" ) unary | primary
    private Expression parseUnary(){
        if (matchCurrentToken(TokenType.SUBTRACT, TokenType.NOT)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();
            return new UnaryExpression(token, parseUnary());

        } else {
            return parsePrimary();
        }
    }

    // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expression parsePrimary() {
        Token currentToken = getCurrentToken();
        consumeToken();
        switch (currentToken.lexeme) {
            case "true":
                return new LiteralExpression(true);
            case "false":
                return new LiteralExpression(false);
            case "nil":
                return new LiteralExpression(null);
            case "(":
                Expression expr = parseExpression();
                // consume ")"
                consumeToken(TokenType.RIGHT_PAREN);
                return new GroupingExpression(expr);
            default:
                if (currentToken.type == NUMBER || currentToken.type == STRING){
                    return new LiteralExpression(currentToken.value);
                } else if (currentToken.type == IDENTIFIER){
                    return new VariableExpression(currentToken);
                } else {
                    reportParserError(currentToken, "Expected STRING or NUMBER or IDENTIFIER");
                    return new LiteralExpression(null);
                }
        }
    }

    // Helper methods
    private boolean matchCurrentToken(TokenType... toMatchTokens){
        if (noMoreTokensToConsume()) return false;

        Token current = getCurrentToken();
        for (TokenType toMatchToken : toMatchTokens) {
            if (toMatchToken == current.type) return true;
        }
        return false;
    }

    private Token getCurrentToken(){
        return tokens.get(currentToken);
    }

    // will return True if the last token has been consumed
    private boolean noMoreTokensToConsume(){
        return currentToken >= tokens.size();
    }

    private void consumeToken() {
        if (noMoreTokensToConsume()) throw new IndexOutOfBoundsException();
        currentToken++;
    }

    private void consumeToken(TokenType tokenType){
        if (getCurrentToken().type == tokenType) {
            consumeToken();
        } else {
            reportParserError(getCurrentToken(), String.format("Expected %s", tokenType));
        }
    }

    // Error Handling methods 

    private void reportParserError(Token token, String message){
        ParserError err = new ParserError(token, message);
        Error.reportParserError(err);
        throw err;
    }

    // consumes token until we get to a state we can start parsing from again
    private void synchronize(){
        // consume the token that led to error
        consumeToken();

        // keep consuming tokens until we get either ; (end of that statement, expression) OR
        // one of keywords : CLASS, FUN, VAR, FOR, WHILE, PRINT, RETURN, IF 

        while (!noMoreTokensToConsume()){

            switch (getCurrentToken().type) {
                case SEMICOLON:
                    consumeToken();
                    return; 

                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case WHILE:
                case PRINT:
                case RETURN:
                case IF:
                    return;
                
                default:
                    consumeToken();
            }
        }
    }
}