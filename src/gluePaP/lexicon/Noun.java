package gluePaP.lexicon;

import edu.stanford.nlp.ling.IndexedWord;
import gluePaP.glue.LexVariableHandler;
import gluePaP.linearLogic.LLAtom;
import gluePaP.linearLogic.LLFormula;
import gluePaP.linearLogic.LLTerm;
import gluePaP.semantics.SemAtom;
import gluePaP.semantics.SemFunction;
import gluePaP.semantics.SemPred;
import gluePaP.semantics.SemType;

public class Noun extends LexicalEntry {


    public LexType lexType;


    public Noun(LexType type, String identifier, IndexedWord main) {

        this.identifier = identifier;

        //StringJoiner sj = new StringJoiner(" ");

        //f is standard variable for complete f-structure
        //g is standard variable for subject
        //h is standard variable for object
        this.lexType = type;

        switch (this.lexType) {
            case N_NNP:

                //this.llFormula = identifier + "_e";
                this.llTerm = new LLAtom(identifier, LLTerm.Type.E, LLAtom.LLType.CONST,true);

                this.sem = new SemAtom(SemAtom.SemSort.CONST,main.value().substring(0,1).toLowerCase(),
                        SemType.AtomicType.E);
                break;

            case N_NN:

                 /*Linear Logic*/

                //generating consumer
                LLAtom agentRes = new LLAtom(identifier, LLTerm.Type.E, LLAtom.LLType.CONST,false);

                //generate semantics
                LLAtom fsem = new LLAtom(identifier,
                        LLTerm.Type.T, LLAtom.LLType.CONST,true);

                this.llTerm = new LLFormula(agentRes,fsem,true );

                /*Semantics*/

                SemAtom agentVar = new SemAtom(SemAtom.SemSort.VAR,
                        //binding variable
                        LexVariableHandler.returnNewVar(LexVariableHandler.variableType.SemVarE),
                        SemType.AtomicType.E
                );

                SemFunction nounSem = new SemFunction(agentVar,new SemPred(main.value(),agentVar));
                this.sem = nounSem;

                break;



        }
    }
}

