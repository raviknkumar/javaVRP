package it.unica.informatica.ro.vrp.solver.strategies;

import it.unica.informatica.ro.vrp.problem.Problem;
import it.unica.informatica.ro.vrp.problem.Solution;
import it.unica.informatica.ro.vrp.problem.model.Route;
import it.unica.informatica.ro.vrp.problem.model.Vehicle;
import it.unica.informatica.ro.vrp.solver.opt.InterRouteOptimizer;
import it.unica.informatica.ro.vrp.solver.opt.InterRouteOptimizer.RelocateOption;
import it.unica.informatica.ro.vrp.solver.opt.IntraRouteOptimizer;
import it.unica.informatica.ro.vrp.solver.opt.IntraRouteOptimizer.TwoOptOption;

import java.util.Collections;
import java.util.Iterator;

/**
 * <p>SimpleStrategy minimize the given solution using 2-opt and relocate algorithms for intra-route
 * and inter-route improvements respectively.</p>
 * <p>You can use {@link TwoOptOption} and {@link RelocateOption} to choose between BEST_IMPROVEMENT or
 * FIRST_IMPROVEMENT.</p>
 * 
 * @see Strategy
 * @see IntraRouteOptimizer
 * @see InterRouteOptimizer
 * @author nicola
 *
 */
public class SimpleStrategy implements Strategy {

	private Problem problem;
	private IntraRouteOptimizer intraOpt;
	private InterRouteOptimizer interOpt;
	
	private TwoOptOption twoOptOption;
	private RelocateOption relocateOptOption;
	private boolean shuffleON;
	

	//---------------------------- Constructor -------------------------------//

	public SimpleStrategy(Problem problem, TwoOptOption twoOptOption, RelocateOption relocateOptOption, boolean shuffleOn) {

		this.problem = problem;
		this.twoOptOption = twoOptOption;
		this.relocateOptOption = relocateOptOption;
		this.shuffleON = shuffleOn;
		
		this.intraOpt = new IntraRouteOptimizer(problem.getCostMatrix());
		this.interOpt = new InterRouteOptimizer(problem.getCostMatrix());
		
		double delta = 0.000_000_001;
		IntraRouteOptimizer.GAIN_DELTA = delta;
		InterRouteOptimizer.GAIN_DELTA = delta;
	}
	
	
	//----------------------------- Methods ----------------------------------//
	
	@Override
	public void minimize(Solution solution) {
		
		if (shuffleON)
			Collections.shuffle(solution.getVehicles());
		
		// intra-route improvements on all routes
		intraOpt.twoOpt(solution, twoOptOption);
		
		if (!solution.isValid())
			throw new RuntimeException("found invalid solution "+solution);
		
		// inter-route improvements
		addVoidRoute(solution);
		interOpt.relocate(solution, relocateOptOption);
		cleanSolution(solution);
		
		if (!solution.isValid())
			throw new RuntimeException("found invalid solution "+solution);
	}
	
	
	private void addVoidRoute(Solution solution) {
		Vehicle v = new Vehicle(problem.getVehicleCapacity());
		v.setRoute(new Route(problem.getDepot(), problem.getDepot()));
		solution.getVehicles().add(v);
	}
	
	private void cleanSolution(Solution solution) {
		Iterator<Vehicle> it = solution.getVehicles().iterator();
		
		while (it.hasNext()) {
			Vehicle v = it.next();
			
			if (v.getRoute().demand()==0) {
				it.remove();
			}
		}
	}
	

	//----------------------------- Overrides --------------------------------//
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OPTIONS (twoOpt, relocate) = ").append(twoOptOption).append(",").append(relocateOptOption);
		
		return sb.toString(); 
	}
}
