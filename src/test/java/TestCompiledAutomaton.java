import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.CompiledAutomaton;
import org.apache.lucene.util.automaton.DaciukMihovAutomatonBuilder;
import org.apache.lucene.util.automaton.Operations;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class TestCompiledAutomaton {

    private CompiledAutomaton build(int maxDeterminizedStates, String... strings) {
        final List<BytesRef> terms = new ArrayList<>();
        for(String s : strings) {
            terms.add(new BytesRef(s));
        }
        Collections.sort(terms);
        final Automaton a = DaciukMihovAutomatonBuilder.build(terms);
        return new CompiledAutomaton(a, true, false, maxDeterminizedStates, false);
    }

    private void testFloor(CompiledAutomaton c, String input, String expected) {
        final BytesRef b = new BytesRef(input);
        final BytesRef result = c.floor(b, new BytesRefBuilder());
        if (expected == null) {
            assertNull(result);
        } else {
            assertNotNull(result);
            assertEquals("actual=" + result.utf8ToString() + " vs expected=" + expected + " (input=" + input + ")",
                    result, new BytesRef(expected));
        }
    }

    @Test
    public void testBasic() throws Exception {
        CompiledAutomaton c = build(Operations.DEFAULT_MAX_DETERMINIZED_STATES,
                "fob", "foo", "goo");
        testFloor(c, "goo", "goo");
        testFloor(c, "ga", "foo");
        testFloor(c, "g", "foo");
        testFloor(c, "foc", "fob");
        testFloor(c, "foz", "foo");
        testFloor(c, "f", null);
        testFloor(c, "", null);
        testFloor(c, "aa", null);
        testFloor(c, "zzz", "goo");
    }

}
