import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestBuildFst {

    @Test
    public void buildSimplePrefixMatchingFst() throws IOException {
        String[] input = new String[] { "lucene", "lucid dream"};

        final Outputs<Object> outputs = NoOutputs.getSingleton();
        final Object NO_OUTPUT = outputs.getNoOutput();

        CharSequenceOutputs charSequenceOutputs = CharSequenceOutputs.getSingleton();
        Builder<Object> builder = new Builder<Object>(FST.INPUT_TYPE.BYTE1, outputs);
        IntsRefBuilder intsRefBuilder = new IntsRefBuilder();
        for (String s : input) {
            builder.add(Util.toIntsRef(new BytesRef(s), intsRefBuilder), NO_OUTPUT);
        }
        FST<Object> fst = builder.finish();

        assertThat(get(fst, new BytesRef("lucene")), is(NO_OUTPUT));
        assertThat(get(fst, new BytesRef("lucid dream")), is(NO_OUTPUT));
        assertThat(get(fst, new BytesRef("luc")), is(NO_OUTPUT));

        assertThat(get(fst, new BytesRef("lucid dreams")), is(nullValue()));
    }

    public static<T> T get(FST<T> fst, BytesRef input) throws IOException {
        assert fst.inputType == FST.INPUT_TYPE.BYTE1;

        final FST.BytesReader fstReader = fst.getBytesReader();

        // TODO: would be nice not to alloc this on every lookup
        final FST.Arc<T> arc = fst.getFirstArc(new FST.Arc<T>());

        // Accumulate output as we go
        T output = fst.outputs.getNoOutput();
        for(int i=0;i<input.length;i++) {
            if (fst.findTargetArc(input.bytes[i+input.offset] & 0xFF, arc, arc, fstReader) == null) {
                return null;
            }
            output = fst.outputs.add(output, arc.output);
        }

    //    if (arc.isFinal()) {
            return fst.outputs.add(output, arc.nextFinalOutput);
      //  } else {
        //    return null;
        //}
    }
}
