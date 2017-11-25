package Prover;

import gluePaP.linearLogic.LLAtom;
import gluePaP.semantics.SemAtom;

public class SemEquality {

    private final SemAtom variable;
    private final SemAtom constant;

    public SemEquality(SemAtom variable, SemAtom constant)
    {
        this.variable = variable;
        this.constant = constant;
    }


    public SemAtom getVariable() {
        return variable;
    }

    public SemAtom getConstant() {
        return constant;
    }


    @Override
    public String toString() {
        return variable.getName() +variable.getType() + constant.getName() + variable.getType();
    }



    // equals for this object yields true if within the constant and the variable name and type are equal
    @Override
    public boolean equals(Object b)
    {


        if (!(b instanceof SemEquality))
        {
            return false;
        }
        if (b == this)
        {
            return true;
        }

        SemEquality eq = (SemEquality) b;

        return eq.variable.getName().equals(this.variable.getName()) &&
                eq.variable.getType().equals(this.variable.getType()) &&
                eq.constant.getName().equals(this.constant.getName()) &&
                eq.constant.getType().equals(this.constant.getType());

        /*

        if (this.variable.getName().equals(((Equality) b).variable.getName()) &&
                this.variable.getType().equals(((Equality) b).variable.getType()) &&
                this.constant.getName().equals(((Equality) b).constant.getName()) &&
                this.constant.getType().equals(((Equality) b).constant.getType()) )
        {
            return true;
        }
        return false;
    */
    }


    //used by equals() to determine similarity between elements relevant for equals()
    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + this.variable.getName().hashCode();
        result = 31 * result + this.variable.getType().hashCode();
        result = 31 * result + this.constant.getName().hashCode();
        result = 31 * result + this.constant.getType().hashCode();
        return result;
    }

}