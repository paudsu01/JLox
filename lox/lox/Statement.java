package lox.lox;

import lox.scanner.Token;
import java.util.ArrayList;

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

class VarDecStatement extends Statement{
	final Token name;
	final Expression initializer;

	VarDecStatement(Token name, Expression initializer){
		this.name = name;
		this.initializer = initializer;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitVarDecStatement(this);}
}

class BlockStatement extends Statement{
	final ArrayList<Statement> statements;

	BlockStatement(ArrayList<Statement> statements){
		this.statements = statements;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitBlockStatement(this);}
}


interface StatementVisitor<R>{
	R visitExpressionStatement(ExpressionStatement stmt);
	R visitPrintStatement(PrintStatement stmt);
	R visitVarDecStatement(VarDecStatement stmt);
	R visitBlockStatement(BlockStatement stmt);
}
