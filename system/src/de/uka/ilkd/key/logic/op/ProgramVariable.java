// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2011 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.logic.op;

import java.io.IOException;

import de.uka.ilkd.key.java.*;
import de.uka.ilkd.key.java.abstraction.ArrayType;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.abstraction.Type;
import de.uka.ilkd.key.java.reference.*;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.ProgramInLogic;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.util.Debug;
import de.uka.ilkd.key.util.ExtList;

/**
 * The objects of this class represent program variables and program 
 * constants  (resulting from static final declarations in programs; TODO: it is
 * weird that constants are a special case of variables).
 * 
 * Additionally, as a legacy of the past, the RecodeR front end of KeY still 
 * creates objects of this class also for fields (aka. attributes, member 
 * variables), even though theoretically, these are *not* program variables 
 * (not any more)! Such fake "program variables" can be recognized by the fact 
 * that calling isMember() on them returns true. Thus, they must never occur in 
 * actual proofs. The method HeapLDT.getFieldSymbolForPV() serves to convert 
 * such fake program variables to the appropriate constant symbols.
 */
public abstract class ProgramVariable extends AbstractSortedOperator 
    				      implements SourceElement, 
    				                 ProgramElement, 
    				                 Expression, 
    				                 ReferencePrefix, 
    				                 IProgramVariable, 
    				                 ParsableVariable, 
    				                 ReferenceSuffix, 
    				                 ProgramInLogic {

    // attention: this counter is used to get a unique variable name, once the
    // names are unique the counter should be removed %%%%
    private static long COUNTER = 0;
    private long id;
    private final KeYJavaType type;
    private final boolean isStatic;
    private final boolean isModel;
    private final boolean isGhost;
    private final boolean isFinal;

    // the type where this program variable is declared if and only if
    // the program variable denotes a field
    private final KeYJavaType containingType;

    protected ProgramVariable(ProgramElementName name, 
			      Sort               s,
			      KeYJavaType        t, 
			      KeYJavaType        containingType,
			      boolean            isStatic,
			      boolean            isModel,
			      boolean            isGhost,
			      boolean            isFinal) {
	super(name, s == null ?  t.getSort() : s, false);
	this.type = t;
	this.containingType = containingType;	
	this.isStatic = isStatic;
	this.isModel = isModel;
	this.isGhost = isGhost;
	this.isFinal = isFinal;
	// remove this as soon as possible %%%
	id = COUNTER;
	COUNTER++;
	
	assert sort() != Sort.FORMULA;
	assert sort() != Sort.UPDATE;
    }
    
    
    protected ProgramVariable(ProgramElementName name, 
            Sort               s,
            KeYJavaType        t, 
            KeYJavaType        containingType,
            boolean            isStatic,
            boolean            isModel,
            boolean            isGhost) {
        this(name, s, t, containingType, isStatic, isModel, isGhost, false);
    }
    
 
    /** returns unique id %%%% HACK */
    public long id() {
	return id;
    }


    /** @return name of the ProgramVariable */
    public ProgramElementName getProgramElementName() {
	return (ProgramElementName) name();
    }

    
    /**
     * returns true iff the program variable has been declared as static
     */
    public boolean isStatic() {
	return isStatic;
    }

    
    /**
     * returns true iff the program variable has been declared as model
     */    
    public boolean isModel() {
	return isModel;
    }

    /**
     * returns true if the program variable has been declared as ghost
     */
    public boolean isGhost() {
	return isGhost;
    }
    
    
    /**
     * returns true iff the program variable has been declared as final
     */
    public boolean isFinal() {
        return isFinal;
    }

    
    /**
     * returns true iff the program variable is a member
     */
    public boolean isMember() {
	return containingType != null;
    }

    
    /**
     * returns the KeYJavaType where the program variable is declared or
     * null if the program variable denotes not a field
     */
    public KeYJavaType getContainerType() {
	return containingType;
    }
    

    @Override
    public SourceElement getFirstElement(){
	return this;
    }

    
    @Override    
    public SourceElement getLastElement(){
	return this;
    }

    
    @Override    
    public Comment[] getComments() {
	return new Comment[0];
    }

    
    @Override
    public void visit(de.uka.ilkd.key.java.visitor.Visitor v) {
	v.performActionOnProgramVariable(this);
    }


    @Override
    public void prettyPrint(PrettyPrinter w) throws IOException {
	w.printProgramVariable(this);
    }

    
    @Override
    public Position getStartPosition(){
	return Position.UNDEFINED;
    }

    
    @Override
    public Position getEndPosition(){
	return Position.UNDEFINED;
    }
    

    @Override
    public Position getRelativePosition(){
	return  Position.UNDEFINED;
    }

    
    @Override    
    public PositionInfo getPositionInfo(){
	return  PositionInfo.UNDEFINED;
    }

    
    @Override
    public KeYJavaType getKeYJavaType() {
	return type;
    }

    
    @Override    
    public KeYJavaType getKeYJavaType(Services javaServ) {
	return getKeYJavaType();
    }

    
    @Override    
    public KeYJavaType getKeYJavaType(Services javaServ, 
				      ExecutionContext ec) {
	return getKeYJavaType();
    }

    
    /**
     * We do not have a prefix, so fake it!
     * This way we implement ReferencePrefix
     * @author VK
     */
    @Override    
    public ReferencePrefix getReferencePrefix() {
	return null;
    }

    
    /** equals modulo renaming is described in the corresponding
     * comment in class SourceElement. In this case two
     * programvariables are considered to be equal if they are
     * assigned to the same abstract name or if they are the same
     * object.
     */
    @Override    
    public boolean equalsModRenaming(SourceElement se, 
				     NameAbstractionTable nat) {
	return nat.sameAbstractName(this, se);
    }

    
    @Override    
    public Expression convertToProgram(Term t, ExtList l) {
	if(isStatic()) {
	    return new FieldReference(this, 
				      new TypeRef(getContainerType()));
	} else {
	    return this;
	}
    }
    
    
    public String proofToString() {
	final Type javaType = type.getJavaType();
	final String typeName;
	if(javaType instanceof ArrayType) {
	    typeName = ((ArrayType)javaType).getAlternativeNameRepresentation();
	} else if(javaType != null) {
	    typeName = javaType.getFullName();
	} else {
	    typeName = type.getSort().name().toString();
	}
	return typeName + " " + name() + ";\n";
    }

    
    public boolean isImplicit() {
	return getProgramElementName().getProgramName().startsWith("<");
    }


    @Override
    public MatchConditions match(SourceData source, MatchConditions matchCond) {        
        final ProgramElement src = source.getSource();
        source.next();
        if (src == this) {
            return matchCond;
        } else {
            Debug.out("Program match failed. Not same program variable (pattern, source)", this, src);
            return null;
        }     
    }
}