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

class IfElseStatement extends Statement{
	final Expression expr;
	final Statement ifStatement;
	final Statement elseStatement;

	IfElseStatement(Expression expr, Statement ifStatement, Statement elseStatement){
		this.expr = expr;
		this.ifStatement = ifStatement;
		this.elseStatement = elseStatement;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitIfElseStatement(this);}
}

class WhileStatement extends Statement{
	final Expression expr;
	final Statement statement;

	WhileStatement(Expression expr, Statement statement){
		this.expr = expr;
		this.statement = statement;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitWhileStatement(this);}
}

class FunctionStatement extends Statement{
	final Token name;
	final ArrayList<Token> parameters;
	final Statement body;

	FunctionStatement(Token name, ArrayList<Token> parameters, Statement body){
		this.name = name;
		this.parameters = parameters;
		this.body = body;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitFunctionStatement(this);}
}

class ReturnStatement extends Statement{
	final Token keyword;
	final Expression returnValue;

	ReturnStatement(Token keyword, Expression returnValue){
		this.keyword = keyword;
		this.returnValue = returnValue;
	}

	@Override
	<R> R accept(StatementVisitor<R> visit){
	 return visit.visitReturnStatement(this);}
}


interface StatementVisitor<R>{
	R visitExpressionStatement(ExpressionStatement stmt);
	R visitPrintStatement(PrintStatement stmt);
	R visitVarDecStatement(VarDecStatement stmt);
	R visitBlockStatement(BlockStatement stmt);
	R visitIfElseStatement(IfElseStatement stmt);
	R visitWhileStatement(WhileStatement stmt);
	R visitFunctionStatement(FunctionStatement stmt);
	R visitReturnStatement(ReturnStatement stmt);
}
