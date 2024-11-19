package lox.lox;

import lox.scanner.Token;
import lox.scanner.TokenType;

public class ASTPrinter implements ExpressionVisitor<String>{

    public static void main(String[] commandLineArguments){
        Expression expression = new BinaryExpression(
        new UnaryExpression(
            new Token(TokenType.NEQ, "-", null, 1),
            new LiteralExpression(123)),
        new Token(TokenType.MULTIPLY, "*", null, 1),
        new GroupingExpression(
            new LiteralExpression(45.67)));
        
        System.out.println(new ASTPrinter().print(expression));
    }

    public ASTPrinter(){

    }

    public String print(Expression expression){
        return expression.accept(this);
    }

    public String visitBinaryExpression(BinaryExpression expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    public String visitUnaryExpression(UnaryExpression expr) {
        return parenthesize(expr.operator.lexeme, expr.expression);
    }

    public String visitGroupingExpression(GroupingExpression expr) {
        return parenthesize("group", expr.expression);
    }

    public String visitLiteralExpression(LiteralExpression expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    private String parenthesize(String firsString, Expression... expressions){

        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(firsString);

        for (Expression expression: expressions){
            builder.append(" ");
            builder.append(expression.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }
}