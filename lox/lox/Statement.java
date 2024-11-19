package lox.lox;

import lox.scanner.Token;

abstract class Statement {
	abstract <R> R accept(StatementVisitor<R> visitor);
}

class ExpressionStatement extends Statement{
	final Expression expression;

	ExpressionStatement(Expression expression){
		this.expression = expression;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitExpressionStatement(this);}
}

class PrintStatement extends Statement{
	final Expression expression;

	PrintStatement(Expression expression){
		this.expression = expression;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitPrintStatement(this);}
}


interface StatementVisitor<R>{
	R visitExpressionStatement(ExpressionStatement stmt);
	R visitPrintStatement(PrintStatement stmt);
}
