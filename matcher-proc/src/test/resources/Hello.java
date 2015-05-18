package test;

import org.xteam.matchers.Matchable;

@Matchable
public class Hello {

    private String name;
    private int value;

    public Hello(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

}
