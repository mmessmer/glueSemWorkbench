package gluePaP.lexicon;

import java.util.StringJoiner;

public class Verb implements LexicalEntry {

    LexType lexType;
    String formula;

    public Verb(LexType type, String lemma) {

        //StringJoiner sj = new StringJoiner(" ");

        //f is standard variable for complete f-structure
        //g is standard variable for subject
        //h is standard variable for object

        switch (type) {
            case V_INTR:

                this.formula = "(g -o f)";

                break;

            case V_TRANS:

                this.formula = "(g -o (h -o f))";


        }
    }
}

