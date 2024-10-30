package lox.lox;

import lox.scanner.Token;
import lox.scanner.TokenType;

import static lox.scanner.TokenType.SEMICOLON;

import java.util.ArrayList;;

public class Parser {
    
    ArrayList<Token> tokens;
    private int currentToken=0;

    public Parser(ArrayList<Token> tokens){
        this.tokens = tokens;
    }

    public Expression createParseTree(){
        return parseExpression();
    }

    // Methods for grammar definitions

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
    private Expression parsePrimary(){
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
                consumeToken();
                return new GroupingExpression(expr);
            default:
                return new LiteralExpression(currentToken.value);
        }
    }

    private boolean matchCurrentToken(TokenType... toMatchTokens){
        if (noMoreTokens()) return false;

        Token current = getCurrentToken();
        for (TokenType toMatchToken : toMatchTokens) {
            if (toMatchToken == current.type) return true;
        }
        return false;
    }

    // Helper methods

    private Token getCurrentToken(){
        return tokens.get(currentToken);
    }

    private boolean noMoreTokens(){
        return currentToken >= (tokens.size() -1);
    }

    private void consumeToken() {
        if (noMoreTokens()) throw new IndexOutOfBoundsException();
        currentToken++;
    }

    private void consumeToken(TokenType tokenType){
        // TO DO ( ERROR )
    }
}
