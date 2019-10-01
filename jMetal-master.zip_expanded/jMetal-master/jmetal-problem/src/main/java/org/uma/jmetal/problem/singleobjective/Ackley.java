
package org.uma.jmetal.problem.singleobjective;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Sphere problem.
 */
@SuppressWarnings("serial")
public class Ackley extends AbstractDoubleProblem {
  /** Constructor */
  

  /** Constructor */
  public Ackley(Integer numberOfVariables) {
    setNumberOfVariables(numberOfVariables);
    setNumberOfObjectives(1);
    setName("Ackley");

    List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables()) ;
    List<Double> upperLimit = new ArrayList<>(getNumberOfVariables()) ;

    for (int i = 0; i < getNumberOfVariables(); i++) {
      lowerLimit.add(-32.768);
      upperLimit.add(32.768);
    }

    setLowerLimit(lowerLimit);
    setUpperLimit(upperLimit);
  }

  /** Evaluate() method */
  @Override
  public void evaluate(DoubleSolution solution) {
    int numberOfVariables = getNumberOfVariables() ;
    double term1= 0.0;
    double term2 = 0.0;
    double saida = 0.0;
    
    double c = 2*Math.PI;
    double b = 0.2;
    int a = 20;
    
    double[] x = new double[numberOfVariables] ;

    for (int i = 0; i < numberOfVariables; i++) {
      x[i] = solution.getVariableValue(i) ;
    }

    double sum1 = 0.0;
    double sum2 = 0.0;
    double value = 0.0;
    
    for (int var = 0; var < numberOfVariables; var++) {
      value = x[var];
      sum1 = sum1 + (value*value);
      sum2 = sum2 + Math.cos(c * value);
    }
    
    term1 = (-1.0*a)*Math.exp((-1.0*b)*Math.sqrt(sum1/numberOfVariables));
    term2 = (-1.0*Math.exp(sum2/numberOfVariables));
    
    saida = term1 + term2 + a + Math.exp(1);
    solution.setObjective(0, saida);
  }
}
