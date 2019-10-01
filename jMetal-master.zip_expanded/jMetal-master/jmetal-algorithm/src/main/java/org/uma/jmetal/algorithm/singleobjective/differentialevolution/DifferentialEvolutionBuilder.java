package org.uma.jmetal.algorithm.singleobjective.differentialevolution;

import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * DifferentialEvolutionBuilder class
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class DifferentialEvolutionBuilder {
  private DoubleProblem problem;
  private int populationSize;
  private int maxEvaluations;
  private DifferentialEvolutionCrossover crossoverOperator;
  private DifferentialEvolutionSelection selectionOperator;
  private SolutionListEvaluator<DoubleSolution> evaluator;
  private int execucao;
  private String classificador;
  private String metodo;
  private int nTreino;
  
  public DifferentialEvolutionBuilder(DoubleProblem problem) {
    this.problem = problem;
    this.populationSize = 100;
    this.maxEvaluations = 25000;
    this.crossoverOperator = new DifferentialEvolutionCrossover(0.5, 0.5, "rand/1/bin");
    this.selectionOperator = new DifferentialEvolutionSelection();
    this.evaluator = new SequentialSolutionListEvaluator<DoubleSolution>();
  }

  public DifferentialEvolutionBuilder setPopulationSize(int populationSize) {
    if (populationSize < 0) {
      throw new JMetalException("Population size is negative: " + populationSize);
    }

    this.populationSize = populationSize;

    return this;
  }

  public DifferentialEvolutionBuilder setMaxEvaluations(int maxEvaluations) {
    if (maxEvaluations < 0) {
      throw new JMetalException("MaxEvaluations is negative: " + maxEvaluations);
    }

    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public DifferentialEvolutionBuilder setCrossover(DifferentialEvolutionCrossover crossover) {
    this.crossoverOperator = crossover;

    return this;
  }

  public DifferentialEvolutionBuilder setSelection(DifferentialEvolutionSelection selection) {
    this.selectionOperator = selection;

    return this;
  }

  public DifferentialEvolutionBuilder setSolutionListEvaluator(SolutionListEvaluator<DoubleSolution> evaluator) {
    this.evaluator = evaluator;

    return this;
  }
  
  public DifferentialEvolutionBuilder setExecucao(int execucao)
  {
	  this.execucao = execucao;
	  return this;
  }

  public DifferentialEvolutionBuilder setClassificador(String classificador)
  {
	  this.classificador = classificador;
	  return this;
  }
  public DifferentialEvolutionBuilder setMetodo(String metodo)
  {
	  this.metodo = metodo;
	  return this;
  }
  public DifferentialEvolutionBuilder setNtreino(int nTreino)
  {
	  this.nTreino = nTreino;
	  return this;
  }

  public DifferentialEvolution build() {
    return new DifferentialEvolution(problem, maxEvaluations, populationSize, crossoverOperator,
        selectionOperator, evaluator, execucao, classificador, metodo, nTreino);
  }

  /* Getters */
  public DoubleProblem getProblem() {
    return problem;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
  }

  public DifferentialEvolutionCrossover getCrossoverOperator() {
    return crossoverOperator;
  }

  public DifferentialEvolutionSelection getSelectionOperator() {
    return selectionOperator;
  }

  public SolutionListEvaluator<DoubleSolution> getSolutionListEvaluator() {
    return evaluator;
  }
}

