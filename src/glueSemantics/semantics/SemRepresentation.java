package glueSemantics.semantics;


public abstract class SemRepresentation {
    private SemType type;

    public SemRepresentation() {
    }

    public SemType getType() {
        return type;
    }

    public void setType(SemType.AtomicType type) {
        this.type = new SemType(type);
    }

    public void setType(SemType type) {
        this.type = type;
    }

    public abstract SemRepresentation betaReduce();

    public abstract SemRepresentation applyTo(SemAtom var, SemRepresentation arg);

    // This is not a regular clone() method, it just calls the copy constructor
    // of the respective class.
    public abstract SemRepresentation clone();



}