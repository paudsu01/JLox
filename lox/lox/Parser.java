package lox.lox;

import lox.scanner.Token;
import lox.scanner.TokenType;
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

    private Expression parseExpression(){
        return parseEquality();
    }

    private Expression parseEquality(){
        Expression expression = parseComparison();

        while (matchCurrentToken(TokenType.NEQ, TokenType.EQ)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseComparison();
            new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    private Expression parseComparison(){
        Expression expression = parseTerm();

        while (matchCurrentToken(TokenType.GEQ, TokenType.GT, TokenType.LEQ, TokenType.LT)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseTerm();
            new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    private Expression parseTerm(){
        Expression expression = parseFactor();

        while (matchCurrentToken(TokenType.SUBTRACT, TokenType.ADD)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseFactor();
            new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    private Expression parseFactor(){
        Expression expression = parseUnary();

        while (matchCurrentToken(TokenType.MULTIPLY, TokenType.DIVIDE)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseUnary();
            new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    private Expression parseUnary(){
        // TO DO 
    }

    private Expression parsePrimary(){
        // TO DO
    }

    private boolean matchCurrentToken(TokenType... toMatchTokens){
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
        return currentToken >= tokens.size();
    }

    private void consumeToken() {
        if (noMoreTokens()) throw new IndexOutOfBoundsException();
        currentToken++;
    }
}
