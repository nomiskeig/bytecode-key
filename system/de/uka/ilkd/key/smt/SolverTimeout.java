package de.uka.ilkd.key.smt;

import java.util.TimerTask;

import de.uka.ilkd.key.smt.SMTSolver.ReasonOfInterruption;

class SolverTimeout extends TimerTask {
    static int counter = 0;
    int id = ++counter;
    final SMTSolver solver;
    final Session session;
    final long timeout;

    public SolverTimeout(SMTSolver solver, Session session, long timeout) {
	this.solver = solver;
	this.session = session;
	this.timeout = timeout;
    }

    public SolverTimeout(SMTSolver solver, long timeout) {
	this.solver = solver;
	this.session = null;
	this.timeout = timeout;
    }

    @Override
    public void run() {
	if (session != null) {
	    session.interruptSolver(solver, ReasonOfInterruption.Timeout);
	} else {
	    solver.interrupt(ReasonOfInterruption.Timeout);
	}
	// this.cancel();
    }

    public long getTimeout() {
	return timeout;
    }

}