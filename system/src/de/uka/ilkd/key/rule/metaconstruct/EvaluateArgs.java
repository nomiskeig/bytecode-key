// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2011 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.rule.metaconstruct;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.java.*;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.declaration.LocalVariableDeclaration;
import de.uka.ilkd.key.java.declaration.VariableSpecification;
import de.uka.ilkd.key.java.expression.operator.CopyAssignment;
import de.uka.ilkd.key.java.expression.operator.New;
import de.uka.ilkd.key.java.reference.*;
import de.uka.ilkd.key.logic.VariableNamer;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.rule.inst.SVInstantiations;


public class EvaluateArgs extends ProgramTransformer{

    /** creates a typeof ProgramTransformer 
     * @param pe the instance of expression contained by 
     * the meta construct 
     */
    public EvaluateArgs(ProgramElement pe) {
	super("#evaluate-arguments", pe); 
    }

    
    public static ProgramVariable evaluate(Expression e, 
                                           List<? super LocalVariableDeclaration> l, 
                                           Services services, 
                                           ExecutionContext ec) {

	final VariableNamer varNamer = services.getVariableNamer();
	final KeYJavaType t = e.getKeYJavaType(services, ec);
	final ProgramVariable pv = 
	    new LocationVariable(VariableNamer.parseName
				(varNamer.
				 getSuggestiveNameProposalForSchemaVariable(e)), t);
	
	l.add(new LocalVariableDeclaration(new TypeRef(t), 
					   new VariableSpecification(pv, e, t)));
	return pv;
    }


    @Override
    public ProgramElement transform(ProgramElement pe,
					    Services services,
					    SVInstantiations svInst) {

	final ExecutionContext ec = svInst.getExecutionContext();

	MethodOrConstructorReference mr
	  = (MethodOrConstructorReference) (pe instanceof CopyAssignment 
		  			    ? ((CopyAssignment)pe).getChildAt(1)
		                            : pe);
	
	List<Statement> evalstat = new LinkedList<Statement>();

	final ReferencePrefix newCalled;	
	final ReferencePrefix invocationTarget = mr.getReferencePrefix();

	if (invocationTarget instanceof Expression && 
	    !(invocationTarget instanceof ThisReference)) {
	    newCalled = evaluate
		((Expression)invocationTarget, evalstat, services, ec);
	} else {
	    newCalled = mr.getReferencePrefix();
	}
	
	ImmutableArray<? extends Expression> args = mr.getArguments();
	Expression[] newArgs = new Expression[args.size()];
	for(int i = 0; i < args.size(); i++) { 
	    newArgs[i] = evaluate(args.get(i), evalstat, services, ec);
	}

	Statement[] res = new Statement[1+evalstat.size()];
	final Iterator<Statement> it = evalstat.iterator();
	for(int i = 0; i < evalstat.size(); i++) {
	    res[i] = it.next();
	}

	final MethodOrConstructorReference resMR;
	if(mr instanceof MethodReference) {
	    resMR = new MethodReference(new ImmutableArray<Expression>(newArgs),
		    		        ((MethodReference)mr).getMethodName(), 
		    		        newCalled);
	} else if(mr instanceof New) {
	    resMR = new New(newArgs, 
		    	    ((New)mr).getTypeReference(), 
		    	    mr.getReferencePrefix());
	} else if(mr instanceof SuperConstructorReference) {
	    resMR = new SuperConstructorReference(mr.getReferencePrefix(), 
		    			          newArgs);
	} else if(mr instanceof ThisConstructorReference) {
	    resMR = new ThisConstructorReference(newArgs);
	} else {	    
	    assert false : "unexpected subclass of MethodOrConstructorReference";
	    resMR = null;	
	}

	if(pe instanceof CopyAssignment) {
	    res[res.length-1] = new CopyAssignment
		(((CopyAssignment)pe).getExpressionAt(0), (Expression)resMR);
	} else {
	    res[res.length-1] = resMR;
	}

	return new StatementBlock(res);
    }
}