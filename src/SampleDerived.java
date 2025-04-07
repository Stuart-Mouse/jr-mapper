public class SampleDerived extends SampleSuper {
    double fraction;

    String getString() {
        return text + " (" + number + ")";
    }
    String getAlternateString() {
        return "alternate (" + fraction + ")";
    }
}
