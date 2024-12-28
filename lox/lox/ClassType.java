package lox.lox;

public enum ClassType {
    NONE("none"), CLASS("class"), SUBCLASS("subclass");
    String name;
    private ClassType(String name) {
        this.name = name;
    }
}
