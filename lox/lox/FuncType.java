package lox.lox;

public enum FuncType {
   STATIC("static"), METHOD("method"), FUNCTION("fn"), INIT("init");
   
   String name;
   private FuncType(String name){
    this.name = name;
   }
}
