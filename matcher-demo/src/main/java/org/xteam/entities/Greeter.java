package org.xteam.entities;

import java.util.List;

import org.xteam.matchers.Matchable;

@Matchable
public class Greeter {

    private String message;
    private List<Hello> hellos;

    public String getMessage() {
        return message;
    }

    public List<Hello> getHellos() {
        return hellos;
    }

}
