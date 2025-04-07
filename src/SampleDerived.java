public class SampleDerived extends SampleSuper {
    public SampleDerived() {}
    public SampleDerived(double fraction) {
        this.fraction = fraction;
    }

    public double fraction;

    public String getString() {
        return text + " (" + number + ")";
    }
    public String getAlternateString() {
        return "alternate (" + fraction + ")";
    }
}
