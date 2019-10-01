package org.uma.jmetal.problem.singleobjective;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Ellipsoid extends AbstractDoubleProblem {

  /**
   * Constructor
   * Creates a default instance of the Ellipsoid problem
   *
   * @param numberOfVariables Number of variables of the problem
   */
  public Ellipsoid(Integer numberOfVariables) {
    setNumberOfVariables(numberOfVariables);
    setNumberOfObjectives(1);
    setNumberOfConstraints(0) ;
    setName("Ellipsoid");

    List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables()) ;
    List<Double> upperLimit = new ArrayList<>(getNumberOfVariables()) ;

    for (int i = 0; i < getNumberOfVariables(); i++) {
      lowerLimit.add(-65.536);
      upperLimit.add(65.536);
    }

    setLowerLimit(lowerLimit);
    setUpperLimit(upperLimit);
  }

  /** Evaluate() method */
  @Override
  public void evaluate(DoubleSolution solution) {
    int numberOfVariables = getNumberOfVariables() ;
    
    double[] x = new double[numberOfVariables] ;

    for (int i = 0; i < numberOfVariables; i++) {
      x[i] = solution.getVariableValue(i) ;
    }
    
    double outher = 0.0;
    double inner = 0.0;
    double value = 0.0;
    
    for (int i = 0; i < numberOfVariables; i++) 
    {
    	inner = 0.0;
    	for (int j = 0; j < i; j++)
    	{
    		value = x[j];
    		inner += value * value;
    	}
    	outher += inner;
    }

    solution.setObjective(0, outher);
  }
}

