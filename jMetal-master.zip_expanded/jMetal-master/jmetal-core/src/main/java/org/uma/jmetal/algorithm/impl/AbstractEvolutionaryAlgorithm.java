package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;

import java.util.List;

/**
 * Abstract class representing an evolutionary algorithm
 * @param <S> Solution
 * @param <R> Result
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractEvolutionaryAlgorithm<S, R>  implements Algorithm<R>{
  protected List<S> population;
  protected Problem<S> problem ;
  private List<S> Pt;
  private List<S> Pk;
  private List<S> Pk1;
  public List<S> getPopulation() {
    return population;
  }
  public void setPopulation(List<S> population) {
    this.population = population;
  }

  public void setProblem(Problem<S> problem) {
    this.problem = problem ;
  }
  public Problem<S> getProblem() {
    return problem ;
  }

  protected abstract void initProgress();

  protected abstract void updateProgress();

  protected abstract boolean isStopping();
  protected abstract boolean isStoppingConditionReached();

  protected abstract  List<S> createInitialPopulation();
  protected abstract List<S> createInitialDESurrogate(int variaveis, int solucoes);
  protected abstract List<S> evaluateSurogateDE(List<S> population);
  
  protected abstract double avaliation(List<S> population);
  protected abstract List<S> evaluatePopulation(List<S> population);
  
  
  protected abstract List<S> selection(List<S> population);

  protected abstract List<S> reproduction(List<S> population);

  protected abstract List<S> replacement(List<S> population, List<S> offspringPopulation);
  protected abstract List<S> Uniao(List<S> population, List<S> population1);
  protected abstract  List<S> getLideres(List<S> population);
  
  @Override public abstract R getResult();

  @Override public void run() {
    
	List<S> offspringPopulation;
    List<S> matingPopulation;

    population = createInitialPopulation();
    population = evaluatePopulation(population);
    initProgress();
    while (!isStoppingConditionReached()) {
      matingPopulation = selection(population);
      offspringPopulation = reproduction(matingPopulation);
      offspringPopulation = evaluatePopulation(offspringPopulation);
      population = replacement(population, offspringPopulation);
      updateProgress();
    }
	//  M1();
  }
  
  
  public List<S> EMO(List<S> population)
  {
	  List<S> offspringPopulation;
	  List<S> matingPopulation;
	  
	  population = evaluateSurogateDE(population);
	  
	  while (!isStoppingConditionReached()) {
	      matingPopulation = selection(population);
	      offspringPopulation = reproduction(matingPopulation);
	      offspringPopulation = evaluateSurogateDE(offspringPopulation);
	      population = replacement(population, offspringPopulation);
	      updateProgress();
	    }
	  
	  return population;
			  
  }
  
  
  public void M1()
  {	
	  int teta = 5;
	  int t = 0;
	  
	  int k = t%teta;
	  // inicializa as populacoes 
	  Pk = createInitialDESurrogate(20,100);
	   
	  
	  double eval = 1000;
	  
	  while (!isStopping())
	  {
		  //treina o surrogate
		  if (t % teta == 0)
		  {
			  
			  Pk1 = Uniao(Pk, Pt);
			  
			  // Avalia com a Funcao Real
			  // Treina
			  evaluatePopulation(Pk1);
			  if(isStopping())
				  break;
			  
			  //atualiza o eval eval = eval 
			  
			  if (k==0) 
			  {
				  Pt = createInitialDESurrogate(20,100);
				  Pt = evaluatePopulation(Pt);
				  if(isStopping())
					  break;
			  } 
			  else 
			  {
				  //updateLeaders(Pk1);
				  Pt = getLideres(Pk1);
			  }
			  eval = avaliation(Pt);
			  System.out.println("Evaluation "+k+": "+eval+"");
			  k += 1;
			  Pk = Pk1;
		  }
		  //aplica o algoritmo evolutivo
		  Pt = EMO(Pt);
		  t += 1;
		  
	  }
  }
  
}
