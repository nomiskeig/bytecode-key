package de.uka.ilkd.key.java.expression.operator.adt;

import org.key_project.util.ExtList;

import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.expression.operator.BinaryOperator;
import de.uka.ilkd.key.java.visitor.Visitor;

public class SetMinus extends BinaryOperator {

    public SetMinus(ExtList children) {
        super(children);
    }


    public int getPrecedence() {
        return 0;
    }


    public int getNotation() {
        return PREFIX;
    }


    public void visit(Visitor v) {
        v.performActionOnSetMinus(this);
    }


    public void prettyPrint(PrettyPrinter p) throws java.io.IOException {
        p.printSetMinus(this);
    }

}