// This file is part of KeY - Integrated Deductive Software Design
//
// Copyright (C) 2001-2011 Universitaet Karlsruhe (TH), Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
// Copyright (C) 2011-2015 Karlsruhe Institute of Technology, Germany
//                         Technical University Darmstadt, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General
// Public License. See LICENSE.TXT for details.
//

package de.uka.ilkd.key.rule.merge;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;

import de.uka.ilkd.key.control.KeYEnvironment;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.macros.AbstractProofMacro;
import de.uka.ilkd.key.macros.FinishSymbolicExecutionUntilMergePointMacro;
import de.uka.ilkd.key.macros.FullAutoPilotWithJMLSpecMergesProofMacro;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.init.JavaProfile;
import de.uka.ilkd.key.proof.io.ProblemLoaderException;
import de.uka.ilkd.key.rule.merge.MergeProcedure;
import de.uka.ilkd.key.rule.merge.MergeRule;
import de.uka.ilkd.key.rule.merge.MergeRuleBuiltInRuleApp;
import de.uka.ilkd.key.rule.merge.procedures.MergeIfThenElseAntecedent;
import de.uka.ilkd.key.rule.merge.procedures.MergeTotalWeakening;
import de.uka.ilkd.key.util.ProofStarter;

/**
 * Test suite for the {@link MergeRule}.
 *
 * @author Dominic Scheurer
 */
public class MergeRuleTests extends TestCase {

    private static final String TEST_RESOURCES_DIR_PREFIX =
            "resources/testcase/merge/";

    /**
     * Simple regression test case loading an existing closed proof (standard
     * Gcd example) including two merges with ITE antecedent merges and trying to
     * replay it.
     *
     * @throws ProblemLoaderException
     *             If the proof could not be loaded.
     */
    @Test
    public void testLoadGcdProof() {
        Proof proof = loadProof("gcd.closed.proof");
        assertTrue(proof.closed());
    }

    /**
     * Simple regression test case loading an existing closed proof (standard
     * Gcd example) including two merges with predicate abstraction and trying to
     * replay it.
     *
     * @throws ProblemLoaderException
     *             If the proof could not be loaded.
     */
    @Test
    public void testLoadGcdProofWithPredAbstr() {
        Proof proof = loadProof("gcd.closed.predicateabstraction.proof");
        assertTrue(proof.closed());
    }

    /**
     * Simple regression test case loading an existing closed proof (standard
     * Gcd example) including two merges with predicate abstraction (with lattice
     * elements manually chosen by the user) and trying to replay it.
     *
     * @throws ProblemLoaderException
     *             If the proof could not be loaded.
     */
    @Test
    public void testLoadGcdProofWithPredAbstrAndUserChoices() {
        Proof proof = loadProof("gcd.closed.predicateAbstractionWithUserChoices.proof");
        assertTrue(proof.closed());
    }

    /**
     * Runs the FullAutoPilotWithJMLSpecMergesProofMacro on the problem with merge
     * blocks specified in JML, following by an automatic strategy finish. At
     * the end, there should be two merge applications, and the proof should be
     * closed.
     */
    @Test
    public void testDoAutomaticGcdProofWithMergePointStatements() {
        final Proof proof = loadProof("gcd.mergePointStatements.key");
        startAutomaticStrategy(proof);

        assertTrue(proof.closed());
        //TODO (DS): Assert that there is the expected number of merge rule applications
    }

    /**
     * Runs the FullAutoPilotWithJMLSpecMergesProofMacro on the problem with merge
     * blocks specified in JML, following by an automatic strategy finish. At
     * the end, there should be two merge applications, and the proof should be
     * closed.
     */
    @Test
    public void testDoAutomaticGcdProofWithMerges() {
        final Proof proof = loadProof("gcd.joinBlocks.key");
        runMacro(new FullAutoPilotWithJMLSpecMergesProofMacro(), proof.root());
        startAutomaticStrategy(proof);

        assertTrue(proof.closed());
        assertEquals("There should be two merge rule applications in the proof.", 2,
                proof.getStatistics().mergeRuleApps);
    }

    /**
     * This test case semi-automatically proves the Gcd problem with two merges
     * in the following way:
     * <p>
     * 
     * <ol>
     * <li>Run the {@link FinishSymbolicExecutionUntilMergePointMacro} on the
     * root</li>
     * <li>Do one merge</li>
     * <li>Again run the macro on the one open goal</li>
     * <li>Do another merge</li>
     * <li>Let the automatic strategy finish the proof</li>
     * </ol>
     * <p>
     * 
     * At the end, the proof should be closed.
     *
     * @throws ProblemLoaderException
     *             If the proof could not be loaded.
     */
    @Test
    public void testDoManualGcdProof() throws Exception {
        final Proof proof = loadProof("gcd.key");

        for (int i = 0; i < 2; i++) {
            runMacro(new FinishSymbolicExecutionUntilMergePointMacro(), proof
                    .openGoals().head().node());
            mergeFirstGoal(proof, MergeIfThenElseAntecedent.instance());
        }

        startAutomaticStrategy(proof);
        assertTrue(proof.closed());
    }

    /**
     * Merges for SE states with different symbolic states are only allowed if
     * the path conditions are distinguishable -- for the case that if-then-else
     * conditions are employed. This test case tries to merge two states with
     * equal path condition but different symbolic states -- therefore, the merge
     * should fail due to an incomplete rule application.
     */
    @Test
    public void testMergeIndistinguishablePathConditionsWithITE() {
        final Proof proof = loadProof("IndistinguishablePathConditions.proof");

        try {
            mergeFirstGoal(proof, MergeIfThenElseAntecedent.instance());
            fail("The merge operation should not be applicable.");
        }
        catch (IncompleteRuleAppException e) {
        }
    }

    /**
     * Same as testMergeIndistinguishablePathConditionsWithITE(), but with two
     * merge partners.
     */
    @Test
    public void testMergeThreeIndistinguishablePathConditionsWithITE() {
        final Proof proof =
                loadProof("IndistinguishablePathConditions.twoJoins.proof");

        try {
            mergeFirstGoal(proof, MergeIfThenElseAntecedent.instance());
            fail("The merge operation should not be applicable.");
        }
        catch (IncompleteRuleAppException e) {
        }
    }

    /**
     * Merges two SE states with different symbolic states and equal path
     * condition, but uses the "Full Anonymization" merge method for which this
     * is irrelevant. The merge should succeed and the proof should be closable.
     */
    @Test
    public void testMergeIndistinguishablePathConditionsWithFullAnonymization() {
        final Proof proof = loadProof("IndistinguishablePathConditions.proof");

        mergeFirstGoal(proof, MergeTotalWeakening.instance());
        startAutomaticStrategy(proof);

        assertTrue(proof.closed());
        assertEquals(1, proof.getStatistics().mergeRuleApps);
    }

    /**
     * Runs the automatic JavaDL strategy on the given proof.
     *
     * @param proof
     *            Proof to prove automatically.
     */
    private void startAutomaticStrategy(final Proof proof) {
        ProofStarter starter = new ProofStarter(false);
        starter.init(proof);
        starter.start();
    }

    /**
     * Merges the first open goal in the given proof. Asserts that the
     * constructed merge rule application is complete.
     *
     * @param proof
     *            The proof the first goal of which to merge with suitable
     *            partner(s).
     */
    private void mergeFirstGoal(final Proof proof, MergeProcedure mergeProc) {
        final Services services = proof.getServices();
        final MergeRule mergeRule = MergeRule.INSTANCE;

        final Goal mergeGoal = proof.openGoals().head();
        final Node mergeNode = mergeGoal.node();
        final PosInOccurrence mergePio = getPioFirstFormula(mergeNode.sequent());
        final MergeRuleBuiltInRuleApp mergeApp =
                (MergeRuleBuiltInRuleApp) mergeRule.createApp(mergePio, services);

        {
            mergeApp.setMergePartners(MergeRule.findPotentialMergePartners(proof
                    .openGoals().head(), mergePio));
            mergeApp.setConcreteRule(mergeProc);
            mergeApp.setMergeNode(mergeNode);
        }

        if (!mergeApp.complete()) {
            throw new IncompleteRuleAppException();
        }

        mergeGoal.apply(mergeApp);
    }

    /**
     * @param sequent
     *            Sequent to get the PIO of the first succedent formula for.
     * @return The PIO for the first succedent formula of the given sequent.
     */
    private PosInOccurrence getPioFirstFormula(Sequent sequent) {
        return new PosInOccurrence(sequent.succedent().getFirst(),
                PosInTerm.getTopLevel(), false);
    }

    /**
     * Runs the given macro on the given proof node.
     *
     * @param macro
     *            The macro to execute.
     * @param node
     *            The node to execute the macro on.
     */
    private void runMacro(AbstractProofMacro macro, Node node) {
        try {
            macro.applyTo(null, node, null, null);
        }
        catch (Exception e) {
            fail("Could not apply macro.");
        }
    }

    /**
     * Loads the given proof file. Checks if the proof file exists and the proof
     * is not null, and fails if the proof could not be loaded.
     *
     * @param proofFileName
     *            The file name of the proof file to load.
     * @return The loaded proof.
     */
    static Proof loadProof(String proofFileName) {
        File proofFile = new File(TEST_RESOURCES_DIR_PREFIX + proofFileName);
        assertTrue(proofFile.exists());

        try {
            KeYEnvironment<?> environment =
                    KeYEnvironment.load(JavaProfile.getDefaultInstance(),
                            proofFile, null, null, null, true);
            Proof proof = environment.getLoadedProof();
            assertNotNull(proof);

            return proof;
        }
        catch (ProblemLoaderException e) {
            e.printStackTrace();
            fail("Proof could not be loaded:\n" + e.getMessage());
            return null;
        }
    }

    private class IncompleteRuleAppException extends RuntimeException {
        private static final long serialVersionUID = 774109478701810300L;
    }

}