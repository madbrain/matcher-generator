package test.matchers;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;

public final class TestMatchers {
    
    private TestMatchers() {}
    
    public interface HelloMatcher extends Matcher<test.Hello>  {
        HelloMatcher withName(Matcher<java.lang.String> nameMatcher);
        HelloMatcher withValue(Matcher<Integer> valueMatcher);
    }

    private static class HelloMatcherImpl extends DiagnosingMatcher<test.Hello> implements HelloMatcher {

        private Matcher<java.lang.String> nameMatcher;
        private Matcher<Integer> valueMatcher;

        @Override
        public boolean matches(Object item, Description description) {
            if (!(item instanceof test.Hello)) {
                description.appendText(" was not a Hello");
                return false;
            }
            test.Hello value = (test.Hello) item;
            boolean matches = true;
            if (matches && nameMatcher != null) {
                matches &= nameMatcher.matches(value.getName());
                if (!matches) {
                    description.appendText("name ").appendDescriptionOf(nameMatcher).appendText(" ");
                    nameMatcher.describeMismatch(value.getName(), description);
                }
            }
            if (matches && valueMatcher != null) {
                matches &= valueMatcher.matches(value.getValue());
                if (!matches) {
                    description.appendText("value ").appendDescriptionOf(valueMatcher).appendText(" ");
                    valueMatcher.describeMismatch(value.getValue(), description);
                }
            }
            return matches;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is a Hello with {");
            boolean addComa = false;
            if (nameMatcher != null) {
                if (addComa) {
                    description.appendText(", ");
                }
                description.appendText("name ");
                nameMatcher.describeTo(description);
                addComa = true;
            }
            if (valueMatcher != null) {
                if (addComa) {
                    description.appendText(", ");
                }
                description.appendText("value ");
                valueMatcher.describeTo(description);
                addComa = true;
            }
            description.appendText("}");
        }
        
        @Override
        public HelloMatcher withName(Matcher<java.lang.String> nameMatcher) {
            this.nameMatcher = nameMatcher;
            return this;
        }

        @Override
        public HelloMatcher withValue(Matcher<Integer> valueMatcher) {
            this.valueMatcher = valueMatcher;
            return this;
        }

    }

    public static HelloMatcher isHello() {
        return new HelloMatcherImpl();
    }

}
