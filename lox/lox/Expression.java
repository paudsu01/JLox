package lox.lox;

import lox.scanner.Token;

abstract class Expression {
	abstract <R> R accept(ExpressionVisitor<R> visitor);
}

class BinaryExpression extends Expression{
	final Expression left;
	final Token operator;
	final Expression right;

	BinaryExpression(Expression left, Token operator, Expression right){
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitBinaryExpression(this);}
}

class UnaryExpression extends Expression{
	final Token operator;
	final Expression expression;

	UnaryExpression(Token operator, Expression expression){
		this.operator = operator;
		this.expression = expression;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitUnaryExpression(this);}
}

class GroupingExpression extends Expression{
	final Expression expression;

	GroupingExpression(Expression expression){
		this.expression = expression;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitGroupingExpression(this);}
}

class LiteralExpression extends Expression{
	final Object value;

	LiteralExpression(Object value){
		this.value = value;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitLiteralExpression(this);}
}

class VariableExpression extends Expression{
	final Token name;

	VariableExpression(Token name){
		this.name = name;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitVariableExpression(this);}
}

class AssignmentExpression extends Expression{
	final Token name;
	final Expression value;

	AssignmentExpression(Token name, Expression value){
		this.name = name;
		this.value = value;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitAssignmentExpression(this);}
}


interface ExpressionVisitor<R>{
	R visitBinaryExpression(BinaryExpression expr);
	R visitUnaryExpression(UnaryExpression expr);
	R visitGroupingExpression(GroupingExpression expr);
	R visitLiteralExpression(LiteralExpression expr);
	R visitVariableExpression(VariableExpression expr);
	R visitAssignmentExpression(AssignmentExpression expr);
}
