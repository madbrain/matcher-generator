package org.xteam.matchers.processor.test;

import org.junit.Test;
import org.xteam.matchers.processor.MatcherProcessor;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;

public class MatcherProcessorTest {

    @Test
    public void test() {
        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(JavaFileObjects.forResource("Hello.java"))
                .processedWith(new MatcherProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("TestMatchers.java"));
    }

}
