// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2011 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.smt;

import java.util.Collection;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;



public interface SMTTranslator {

    
    /**
     * Translates a problem into the given syntax. The only difference to
     * <code>translate(Term t, Services services)</code> is that assumptions
     * will be added.
     * @param problem the problem to be translated.
     * @param services 
     * @return a StringBuffer representing the term in the given syntax.
     * @throws IllegalFormulaException
     */
    public StringBuffer translateProblem(Term problem, Services services,SMTSettings settings) 
           throws IllegalFormulaException;
    

    /**
     * Returns all exceptions that have occurred while translating the taclets.
     */
    public Collection<Throwable> getExceptionsOfTacletTranslation();

}