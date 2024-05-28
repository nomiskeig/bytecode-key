package de.uka.ilkd.key.rule.metaconstruct;

import org.key_project.logic.Name;

import de.uka.ilkd.key.java.Expression;
import de.uka.ilkd.key.java.KeYJavaASTFactory;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.declaration.LocalVariableDeclaration;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.VariableNamer;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.ProgramSV;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.rule.inst.SVInstantiations;
import de.uka.ilkd.key.java.expression.literal.StringLiteral;
import de.uka.ilkd.key.java.expression.operator.CopyAssignment;
import de.uka.ilkd.key.java.reference.MethodReference;

public class BytecodeNew extends ProgramTransformer {

    public BytecodeNew(SchemaVariable sv) {
        super(new Name("#bytecode-new"), (ProgramSV) sv);
        System.out.println("got sv in bytecode new: " + sv.name());
    }

	@Override
	public ProgramElement[] transform(ProgramElement pe, Services services, SVInstantiations svInst) {
		// TODO Auto-generated method stub

        String classString = ((StringLiteral) pe).getValue();
        // remove the "" around the literal
        classString = classString.substring(1, classString.length()-1);
        System.out.println("programElement: "+classString + " of class " + pe.getClass());

        VariableNamer varNamer = services.getVariableNamer();
        ProgramElementName name = VariableNamer.parseName(varNamer.getSuggestiveNameProposalForSchemaVariable((Expression)pe));
        KeYJavaType type = services.getJavaInfo().getKeYJavaType(classString);
        System.out.println(type.toString());
        System.out.println(name.toString());
        // TODO: this should use localvariable method of KeYJavaASTFactor line 276

        //LocalVariableDeclaration newObj = KeYJavaASTFactory.declare(name, type);
        ProgramVariable pvnew = KeYJavaASTFactory.localVariable(services, "newObject", type);
        LocationVariable pv = new LocationVariable(name, type);
        LocalVariableDeclaration lvd = KeYJavaASTFactory.declareMethodCall(pvnew, pvnew, "<createObject>");
        
        MethodReference allocateCall = KeYJavaASTFactory.methodCall(pv, "<createObject>");
        LocationVariable jvmVar = new LocationVariable(new ProgramElementName("jvm"), services.getJavaInfo().getKeYJavaType("de.jvm.JVM"));
        MethodReference pushObjectOnOpStack = KeYJavaASTFactory.methodCall(jvmVar, "directPush", pvnew);
        KeYJavaType boolType = services.getJavaInfo().getKeYJavaType("Boolean");
        LocationVariable initAttribute = new LocationVariable(new ProgramElementName("<initialized>"),boolType);
        Expression initAttributeAccess = KeYJavaASTFactory.attribute(pvnew, initAttribute);

        CopyAssignment ca = KeYJavaASTFactory.assign(initAttributeAccess, KeYJavaASTFactory.trueLiteral());
        return new ProgramElement[] {
            lvd,
            //ca,
            pushObjectOnOpStack,
        };
	}

}
