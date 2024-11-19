package lox.lox;

import lox.scanner.Token;

abstract class Statement {
	abstract <R> R accept(Visitor<R> visitor);
}

class ExpressionStatement extends Statement{
	final Expression expression;

	ExpressionStatement(Expression expression){
		this.expression = expression;
	}

	@Override
	<R> R accept(Visitor<R> visit){
	 return visit.visitExpressionStatement(this);}
}

class PrintStatement extends Statement{
	final Expression expression;

	PrintStatement(Expression expression){
		this.expression = expression;
	}

	@Override
	<R> R accept(Visitor<R> visit){
	 return visit.visitPrintStatement(this);}
}


interface Visitor<R>{
	R visitExpressionStatement(ExpressionStatement expr);
	R visitPrintStatement(PrintStatement expr);
}
