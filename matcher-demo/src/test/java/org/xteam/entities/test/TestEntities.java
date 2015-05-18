package org.xteam.entities.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.xteam.entities.Hello;
import org.xteam.entities.matchers.EntitiesMatchers;

public class TestEntities {

    @Test
    public void testHello() {
        Hello hello = new Hello("john", 10);
        assertThat(hello, EntitiesMatchers.isHello().withName(is("john")).withAge(is(10)));
    }
}
