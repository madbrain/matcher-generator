package org.xteam.entities;

import org.xteam.matchers.Matchable;

@Matchable
public class Hello {

    private String name;
    private int age;

    public Hello(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

}
