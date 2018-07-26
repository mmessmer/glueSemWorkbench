/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package glueSemantics.parser;

import glueSemantics.semantics.lambda.SemType;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanticParserTest {
    private SemanticParser sp = new SemanticParser();

    @Test
    void parseType() {
        SemType st;
        try {
            st = sp.parseType("<e,<v,t>>");
            Assert.assertEquals(st.getLeft().toString(), SemType._e.toString());
            Assert.assertEquals(st.getRight().getLeft().toString(), SemType._v.toString());
            Assert.assertEquals(st.getRight().getRight().toString(), SemType._t.toString());

        }
        catch (ParserInputException e) {
            e.printStackTrace();
        }
    }

    @Test
    void parseSemTerm() {
        try {
            sp.parseSemTerm("/x.student(x)");
        }
        catch (ParserInputException e) {
            e.printStackTrace();
        }
    }
}