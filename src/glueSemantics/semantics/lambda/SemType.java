/*
 * Copyright 2018 Mark-Matthias Zymla & Moritz Messmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package glueSemantics.semantics.lambda;


import static glueSemantics.semantics.lambda.SemType.AtomicType.E;
import static glueSemantics.semantics.lambda.SemType.AtomicType.TEMP;

public class SemType {
    private SemType left;
    private SemType right;
    private AtomicType simple;

    // An enumeration of possible atomic types
    // TEMP is a special type used for variables introduced
    // during the compilation process
    public enum AtomicType {
        E,T,V,I,TEMP
    }

    public SemType getLeft() {
        return left;
    }

    public void setLeft(SemType left) {
        this.left = left;
    }

    public SemType getRight() {
        return right;
    }

    public void setRight(SemType right) {
        this.right = right;
    }

    public SemType(AtomicType simple) {
        this.simple = simple;
        this.left = null;
        this.right = null;
    }

    public SemType(SemType left, SemType right) {
        this.left = left;
        this.right = right;
    }

    public SemType(AtomicType left, AtomicType right) {
        this.left = new SemType(left);
        this.right = new SemType(right);
    }

    @Override
    public String toString() {
        if (left != null)
            return "<" + left.toString() + "," + right.toString() + ">";
        else {
            if (simple == E)
                return "e";
            else
                return "t";
        }
    }

    // Check if both types are matching simple types or if
    // their LHS and RHS match up (recursively).
    // If one of the variables is TEMP also return true
    public boolean equalsType(SemType t) {
        if (this.simple != null) {
            return t.simple != null && (this.simple.equals(t.simple)||this.simple == TEMP||t.simple == TEMP);
        }
        else
            return (this.left.equalsType(t.left) && this.right.equalsType(t.right));
    }
}