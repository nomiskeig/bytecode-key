package de.uka.ilkd.key.rule.conditions;

import java.util.List;

import org.key_project.logic.Name;
import org.key_project.logic.sort.Sort;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.expression.literal.StringLiteral;
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
    private final List<String> parameters;

    public IsContentCondition(SchemaVariable source, SchemaVariable target, List<String> parameters) {
        this.source = source;
        this.target = target;
        this.parameters = parameters;

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
        String name = ((StringLiteral)inst).getValue();
        // remove first and last char since the initializiation includes the " at front and end
        name = name.substring(1, name.length()-1);
        if (!this.parameters.contains("set_to_parameter")) {
            name = this.parameters.get(1);
            System.out.println("initial string: " + name);
            name = name.replaceAll("_", ".");
            int lastIndex = name.lastIndexOf('.');
            System.out.println("name and index of .: " + name + " "+ lastIndex);
            if (this.parameters.contains("full")) {
            name = name.substring(0, lastIndex) + "::" + name.substring(lastIndex + 1);
            } else {
            name = name.substring(0, lastIndex);

            }


        }
        System.out.println("final name : " + name);

        ProgramElementName newName = new ProgramElementName(name);
        Sort newSort = services.getNamespaces().sorts().lookup("Seq");
        LocationVariable lv = new LocationVariable(newName, newSort);
        // return matchCond;
        return matchCond.setInstantiations(svInst.add(target, lv, services));
    }

}
