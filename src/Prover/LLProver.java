package Prover;

import gluePaP.linearLogic.*;

import java.util.*;

public class LLProver {

    private List<Equality> equalities;

    /*
    Does a deduction of a given sequent by evaluating the list of premises on its LHS
    and trying to find a valid proof for its RHS.
    TODO Check if compilation works properly
     */
    public List<Premise> deduce(Sequent seq) throws ProverException,VariableBindingException {
        /*
        Initialize an agenda stack initially containing all premises from the sequent.
        Premises are popped from the stack into the database and additionally created
        premises get pushed onto the stack.
        Then initialize a database of all premises which is used to look for possible
        implication elimination steps.
        */
        Stack<Premise> agenda = new Stack<>();
        List<Premise> database = new ArrayList<>();
        List<Premise> solutions = new ArrayList<>();
        for (Premise p: seq.getLhs()) {

            /*
            * Check all premises for nested formulas. Alle nested formulas
            * (with two or more nested operators) are compiled following the algorithm
            * outlined by Hepple(1996). All extracted assumptions are added to the agenda
            * as new premises with new IDs. Assumptions are premises that contain themselves
            * in their set of assumptions, but in the course of the derivation they may carry
            * additional assumptions (when they combine with other assumptions).
            * */
            /*
            NOTE: due to the design of the conversion algorithm, a given term's discharge is always
            contained in that term's set of assumptions. This shouldn't be a problem, however, as
            terms with discharges are by design always formulas and can therefore not be arguments.
            Their assumptions are thus not relevant in the derivation process.
            */
            if (p.getTerm() instanceof LLFormula) {
                LLFormula f = ((LLFormula) p.getTerm());
                if (f.isNested()) {
                    // call the conversion method which does all the work
                    f = convert(f);
                    // System.out.println(f);
                    p.setTerm(f);

                    // add all assumptions generated by the conversion to the agenda
                    for (LLTerm term : f.assumptions) {
                        HashSet<Integer> newIDs= new HashSet<>();
                        newIDs.add(seq.getNewID());
                        Premise assumptionPremise = new Premise(newIDs,term);
                        agenda.push(assumptionPremise);
                    }
                    f.assumptions.clear();
                }
            }
            agenda.push(p);
        }
        seq.getLhs().clear();
        seq.getLhs().addAll(agenda);
        /*
        Initialize the set containing the IDs of all premises of the sequent.
        This set is used to determine possible goal terms.
        */
        HashSet<Integer> goalIDs = seq.getMaxIDSet();

        /*
        The algorithm loops over the agenda until it is empty or until a premise is created
        that contains all indexes of the sequent's premises and is therefore the goal.
        */
        while (!agenda.empty()) {
            Premise curr_premise = agenda.pop();
            // add premise to database
            database.add(curr_premise);
            for (int i = 0; i < database.size(); i++) {
                Premise db_premise = database.get(i);

                if (db_premise == curr_premise)
                    continue;

                /*
                Check if the database term is a (complex) formula, if so try to do an
                implication elimination step with the current term on the agenda (curr_premise).
                If successful add the newly created Premise to the database.
                */
                if (db_premise.getTerm() instanceof LLFormula) {

                    Premise new_premise = this.combinePremises(db_premise,curr_premise);
                    if (new_premise != null) {
                        System.out.println("Combining premises " + db_premise +" and " + curr_premise + " : " + new_premise);
                        if (new_premise.getPremiseIDs().equals(goalIDs)) {
                            solutions.add(new_premise);
                        }
                        else {
                            agenda.push(new_premise);
                        }
                        continue;
                    }
                }
                /*
                Check if the current term on the agenda is a (complex) formula. If so do the same procedure
                as above, but reverse (apply db_premise to curr_premise).
                 */
                if (curr_premise.getTerm() instanceof LLFormula) {
                    Premise new_premise = this.combinePremises(curr_premise,db_premise);
                    if (new_premise != null) {
                        System.out.println("Combining premises " + curr_premise +" and " + db_premise + " : " + new_premise);

                        if (new_premise.getPremiseIDs().equals(goalIDs)) {
                            solutions.add(new_premise);
                        }
                        else {
                            agenda.push(new_premise);
                        }
                    }
                }
            }
        }

        /*
        All premises of the agenda were added to the database. If there are
        no possible solutions now, return a ProverException, otherwise return
        the set of solutions.
        */
        if (solutions.isEmpty())
            throw new ProverException("No valid proof found for premises");
        else
            return solutions;
    }


    /*
    implementation of the linear implication elimination rule for indexed premises
    check if arg is equivalent to LHS of func and then return RHS of func
    then check if the sets of indexes are disjoint
    if both checks succeed a new Premise is created containing the unified set of indexes
    and the RHS LL term of func (see below)
    */
    private Premise combinePremises(Premise func, Premise arg) throws VariableBindingException {


        // possible substitutions for variables and constants
        LinkedHashSet<Equality> eqs = new LinkedHashSet<>();

        eqs = ((LLFormula) func.getTerm()).getLhs().checkCompatibility(arg.getTerm());

        if (eqs == null) {return null;}


            if (eqs.size() > 0) {

                //If there are duplicate bindings no valid proof can be reached.
                if (LLProver.checkDuplicateBinding(eqs)) {
                    throw new VariableBindingException();
                } else {
                    //instantiates variables with constants (i.e. skolemizes the formula so it can take a constant)
                    for (Equality eq : eqs) {
                        ((LLFormula) func.getTerm()).instantiateVariables(eq);
                    }
                }

            }

            Premise combined;

            // TODO review this again
            /*
            * No assumptions or discharges involved, proceed with a "normal" implication elimination
            * */

            if (arg.getTerm().assumptions.isEmpty()
                    && arg.getTerm().getDischarge() == null
                    && func.getTerm().assumptions.isEmpty()
                    && func.getTerm().getDischarge() == null) {
                return combineDisjointID(func, arg);
            }
            /*
            * Func or arg contain assumptions, but no discharges.
            * Combine the terms and their sets of assumptions
            * */
            else if ((!arg.getTerm().assumptions.isEmpty()
                    || !func.getTerm().assumptions.isEmpty())
                    && arg.getTerm().getDischarge() == null
                    && func.getTerm().getDischarge() == null) {
                combined = combineDisjointID(func, arg);
                try {
                    combined.getTerm().assumptions = new HashSet<>();
                    /* create new set of assumptions which can be modified independently from
                    the set of assumptions of arg and func and add all assumptions to it */
                    combined.getTerm().assumptions.addAll(arg.getTerm().assumptions);
                    combined.getTerm().assumptions.addAll(func.getTerm().assumptions);
                //    LLTerm discharge = func.getTerm().getDischarge();
                 //   combined.getTerm().assumptions.remove(discharge);
                    // add this back to the functor's assumptions


   //                     arg.getTerm().assumptions.add(discharge);


                } catch (NullPointerException npe){
                    return null;
                }
                return combined;
            }
            /*
            Functor has discharges, check if they are a subset of the argument's assumptions.
            If so call combineDisjointID which checks the ID sets of func and arg and then
            does the actual implication elimination step. For the new premise, all assumptions
            from arg are copied, except the one that was discharged in func.
            func: (b[a] -o c); arg: {a,(x -o y)} ==> c with assumption {(x -o y)}
            */
            else if (func.getTerm().getDischarge() != null) {
                if (arg.getTerm().assumptions.contains(func.getTerm().getDischarge()))
                {

                    combined = combineDisjointID(func, arg);
                    /* create new set of assumptions which can be modified independently from
                    the sets of assumptions of arg and func and add all assumptions to it*/
                    combined.getTerm().assumptions = new HashSet<>();
                    combined.getTerm().assumptions.addAll(arg.getTerm().assumptions);
                    combined.getTerm().assumptions.addAll(func.getTerm().assumptions);

                /*
                    for (LLTerm as : (arg.getTerm().assumptions))
                    {
                        if (as != func.getTerm().getDischarge())
                        {
                            combined.getTerm().assumptions.add(as);
                        }
                    }
                */


                    Iterator it = combined.getTerm().assumptions.iterator();

                    while (it.hasNext())
                    {
                        if (it.next() == func.getTerm().getDischarge())
                        {
                            it.remove();
                        }
                    //    combined.getTerm().assumptions.remove(func.getTerm().getDischarge());
                    }

                    return combined;
                }
            }
            // The discharges are somehow incompatible, return null.
            return null;
    }


    /*
    * Check if the LHS of func is equivalent to arg
    * and if the two sets of indexes associated with them are disjoint.
    * If so return the simplified term (the RHS of func) with combined ID sets.
    * */
    private Premise combineDisjointID(Premise func, Premise arg) {
        HashSet<Integer> combined_IDs = new HashSet<>();
        if (((LLFormula) func.getTerm()).getLhs().checkEquivalence(arg.getTerm())
                && Collections.disjoint(func.getPremiseIDs(),arg.getPremiseIDs())){
            combined_IDs.addAll(func.getPremiseIDs());
            combined_IDs.addAll(arg.getPremiseIDs());

            /*TODO this is a problem since if we use the same func twice
            the resulting object uses the same term in both occasions.
            Thus, if a future modification of one instances of the term occurs,
            the other "copy" will also receive this modification leading to
            unwanted combinations of terms
            */
            // TODO make sure that the HashSet of assumptions is not just a reference to
            // the hashset of arg??
            // Solved by creating a new LLAtom as copy of the RHS of func. If the RHS
            // is an LLFormula then just copy the reference, it shouldn't cause any problems.
            if (((LLFormula) func.getTerm()).getRhs() instanceof  LLAtom)
                return new Premise(combined_IDs,new LLAtom((LLAtom) ((LLFormula) func.getTerm()).getRhs()));
            return new Premise(combined_IDs,((LLFormula) func.getTerm()).getRhs());
        }
        return null;
    }


    /*
    Similar to combinePremises(), but for simple LL terms
    implementation of the linear implication elimination rule for LL terms
    check if arg is equivalent to LHS of func and then return RHS of func
    e.g. func = a -o b; arg = a --> returns b
    */
    public LLTerm combineTerms(LLFormula func, LLTerm arg) {

        if (func.getLhs().checkEquivalence(arg)) {
            return func.getRhs();
        }
        else {
            return null;
        }
    }

    // wrapper method for later
    public LLFormula convert(LLFormula term) {
        return (LLFormula) convert((LLTerm) term);
    }

    // TODO add lists for modifiers and skeletons (see Dick's code)
    // TODO use premises instead of formulas?
    public LLTerm convert(LLTerm term) {
        if (term instanceof LLFormula) {
            LLFormula f = (LLFormula) term;

            // the formula is a modifer no need to convert it
/*            if (f.getLhs().checkEquivalence(f.getRhs()))
                return term;*/

            /*
            The LHS of the LHS of f will become an assumption which in turn gets converted as well.
            The assumption gets converted as well and is marked as an assumption
            by adding itself to its set of assumptions. That is, an LLTerm "a" is an assumption
            iff its set of assumptions contains "a". This way of marking assumptions allows easy
            combination with other assumptions and LLTerms with discharges.
            All extracted assumptions are stored in a HashSet in dependency
            Ex. if f = ((a -o b) -o c) then dependency = (b -o c) and assumption = {a}
            Dependency is a new formula consisting of the rest of f, that is, the RHS of the LHS of f
            and the RHS of f.
            */
            // TODO add semantic operations for conversion steps (i.e. lambda abstraction)
            if (f.getLhs() instanceof LLFormula &&
                    ((LLFormula) f.getLhs()).getOperator() instanceof LLImplication) {
                LLTerm assumption = convert(((LLFormula) f.getLhs()).getLhs());
                LLTerm discharge = assumption;
                assumption.assumptions.add(assumption);
                LLTerm dependency = convert(new LLFormula(f.getTermId(),((LLFormula) f.getLhs()).getRhs(),
                        f.getOperator(),f.getRhs(),f.isPolarity(),f.getVariable()));
                ((LLFormula) dependency).assumptions.addAll(assumption.assumptions);
                dependency.setDischarge(discharge);

                return dependency;
            }
        }
        return term;
    }


    //returns false if a variable is asigned more than one value

    public static boolean checkDuplicateBinding(LinkedHashSet<Equality> in) {
         List<Equality> eqs = new ArrayList<>();
         eqs.addAll(0,in);

         // no multiple assignments possible
        if (eqs.size() <= 1){
            return false;
        }

        for (int i = 0; i < eqs.size(); i++)
        {
            for (int j = 0; j <eqs.size(); j++)
            {
                if (eqs.get(i).getVariable().getName().equals(eqs.get(j).getVariable().getName())
                        && eqs.get(i).getVariable().getType().equals(eqs.get(j).getVariable().getType())
                        && !(eqs.get(i).getConstant().getName().equals(eqs.get(j).getConstant().getName())))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /*

    public Premise resolveQuantifiers(Premise func, Premise arg) throws VariableBindingException{
        LinkedHashSet<Equality> substitutions =
                ((LLFormula) func.getTerm()).getLhs().checkCompatibility(arg.getTerm());

        if (LLProver.checkDuplicateBinding(substitutions)){ throw new VariableBindingException();
        }

        if (!substitutions.isEmpty())
        {
            for (Equality eq : substitutions)
            {
                if (((LLFormula) func.getTerm()).getLhs() instanceof LLUniversalQuant)
                {
                    ((LLUniversalQuant) (((LLFormula) func.getTerm()).getLhs())).instantiateVariables(eq);
                }
                if (arg.getTerm() instanceof LLUniversalQuant)
                {
                    ((LLUniversalQuant) arg.getTerm()).instantiateVariables(eq);
                }
            }

        }
        return null;
    }
*/

}
