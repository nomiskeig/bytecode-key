package de.uka.ilkd.key.rule.conditions;

import org.key_project.logic.Name;
import org.key_project.logic.sort.Sort;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.visitor.Visitor;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.SVSubstitute;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.rule.VariableCondition;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

public class IsContentCondition implements VariableCondition {
    private final SchemaVariable source;
    private final SchemaVariable target;

    public IsContentCondition(SchemaVariable source, SchemaVariable target) {
        this.source = source;
        this.target = target;

    }

    @Override
    public MatchConditions check(SchemaVariable var, SVSubstitute instCandidate, MatchConditions matchCond,
            Services services) {
        final SVInstantiations svInst = matchCond.getInstantiations();
        Visitor visitor;
        System.out.println(svInst.getExecutionContext().toString());

        Object inst = svInst.getInstantiation(source);

        // System.out.println("lookUpValue: " + lookUpValue);
        // System.out.println("lookUpValueClass: " + lookUpValue.getClass()
        System.out.println("svInst: " + svInst);
        System.out.println("instantiation of source:\n");
        System.out.println("Source: " + source);
        System.out.println("Instantiation: " + inst);
        System.out.println("InstantiationClass: " + inst.getClass());

        // we get a location variable

        ProgramElementName newName = new ProgramElementName("de.aload.JVM::op_stack");
        Sort newSort = services.getNamespaces().sorts().lookup("Seq");
        LocationVariable lv = new LocationVariable(newName, newSort);
        // return matchCond;
        return matchCond.setInstantiations(svInst.add(target, lv, services));
    }

}
