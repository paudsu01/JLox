package lox.lox;

public enum FuncType {
   NONE("none"), STATIC("static"), METHOD("method"), FUNCTION("fn"), INIT("init");
   
   String name;
   private FuncType(String name){
    this.name = name;
   }
}
