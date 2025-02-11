package lox.lox;

import lox.scanner.Token;
import java.util.ArrayList;

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

class LogicalExpression extends Expression{
	final Expression left;
	final Token operator;
	final Expression right;

	LogicalExpression(Expression left, Token operator, Expression right){
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitLogicalExpression(this);}
}

class CallExpression extends Expression{
	final Expression callee;
	final Token closingParen;
	final ArrayList<Expression> arguments;

	CallExpression(Expression callee, Token closingParen, ArrayList<Expression> arguments){
		this.callee = callee;
		this.closingParen = closingParen;
		this.arguments = arguments;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitCallExpression(this);}
}

class GetExpression extends Expression{
	final Expression object;
	final Token name;

	GetExpression(Expression object, Token name){
		this.object = object;
		this.name = name;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitGetExpression(this);}
}

class SetExpression extends Expression{
	final Expression object;
	final Token name;
	final Expression value;

	SetExpression(Expression object, Token name, Expression value){
		this.object = object;
		this.name = name;
		this.value = value;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitSetExpression(this);}
}

class ThisExpression extends Expression{
	final Token keyword;

	ThisExpression(Token keyword){
		this.keyword = keyword;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitThisExpression(this);}
}

class SuperExpression extends Expression{
	final Token keyword;
	final Token method;

	SuperExpression(Token keyword, Token method){
		this.keyword = keyword;
		this.method = method;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitSuperExpression(this);}
}

class ArrayExpression extends Expression{
	final ArrayList<Expression> elements;

	ArrayExpression(ArrayList<Expression> elements){
		this.elements = elements;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitArrayExpression(this);}
}

class ArrayElementExpression extends Expression{
	final Token leftBracket;
	final Expression arrayExpression;
	final  Expression index;

	ArrayElementExpression(Token leftBracket, Expression arrayExpression,  Expression index){
		this.leftBracket = leftBracket;
		this.arrayExpression = arrayExpression;
		this.index = index;
	}

	@Override
	<R> R accept(ExpressionVisitor<R> visit){
	 return visit.visitArrayElementExpression(this);}
}


interface ExpressionVisitor<R>{
	R visitBinaryExpression(BinaryExpression expr);
	R visitUnaryExpression(UnaryExpression expr);
	R visitGroupingExpression(GroupingExpression expr);
	R visitLiteralExpression(LiteralExpression expr);
	R visitVariableExpression(VariableExpression expr);
	R visitAssignmentExpression(AssignmentExpression expr);
	R visitLogicalExpression(LogicalExpression expr);
	R visitCallExpression(CallExpression expr);
	R visitGetExpression(GetExpression expr);
	R visitSetExpression(SetExpression expr);
	R visitThisExpression(ThisExpression expr);
	R visitSuperExpression(SuperExpression expr);
	R visitArrayExpression(ArrayExpression expr);
	R visitArrayElementExpression(ArrayElementExpression expr);
}
