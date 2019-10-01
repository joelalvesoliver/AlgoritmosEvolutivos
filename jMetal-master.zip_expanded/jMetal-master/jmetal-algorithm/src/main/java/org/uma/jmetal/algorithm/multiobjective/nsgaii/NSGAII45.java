package org.uma.jmetal.algorithm.multiobjective.nsgaii;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.retorno;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import org.uma.jmetal.algorithm.multiobjective.nsgaiii.retorno;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of NSGA-II following the scheme used in jMetal4.5 and former versions, i.e, without
 * implementing the {@link AbstractGeneticAlgorithm} interface.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAII45<S extends Solution<?>> implements Algorithm<List<S>> {
  protected List<S> population ;
  protected final int maxEvaluations;
  protected final int populationSize;

  protected final Problem<S> problem;

  protected final SolutionListEvaluator<S> evaluator;

  protected int evaluations;

  protected SelectionOperator<List<S>, S> selectionOperator ;
  protected CrossoverOperator<S> crossoverOperator ;
  protected MutationOperator<S> mutationOperator ;
  
  private String urlTreino = "http://127.0.0.1:5000/treinamento";
  private String urlClassifica = "http://127.0.0.1:5000/classifica";
  private String urlSalva = "http://127.0.0.1:5000/save";
  private String urlInicializa = "http://127.0.0.1:5000/inicializa";
  private String urlClassificador = "http://127.0.0.1:5000/classificador";
  private String urlBase = "http://127.0.0.1:5000/guarda";
  private Boolean treinou = false;
  

  /**
   * Constructor
   */
  public NSGAII45(Problem<S> problem, int maxEvaluations, int populationSize,
                  CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
    super() ;
    this.problem = problem;
    this.maxEvaluations = maxEvaluations;
    this.populationSize = populationSize;

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;
  }

  /**
   * Run method
   */
  @Override
  public void run() {
    population = createInitialPopulation() ;
    evaluatePopulation(population) ;

    evaluations = populationSize ;

    while (evaluations < maxEvaluations) {
      List<S> offspringPopulation = new ArrayList<>(populationSize);
      for (int i = 0; i < populationSize; i += 2) {
        List<S> parents = new ArrayList<>(2);
        parents.add(selectionOperator.execute(population));
        parents.add(selectionOperator.execute(population));

        List<S> offspring = crossoverOperator.execute(parents);

        mutationOperator.execute(offspring.get(0));
        mutationOperator.execute(offspring.get(1));

        offspringPopulation.add(offspring.get(0));
        offspringPopulation.add(offspring.get(1));
      }

      evaluatePopulation(offspringPopulation) ;

      List<S> jointPopulation = new ArrayList<>();
      jointPopulation.addAll(population);
      jointPopulation.addAll(offspringPopulation);

      Ranking<S> ranking = computeRanking(jointPopulation);

      population = crowdingDistanceSelection(ranking) ;

      evaluations += populationSize ;
      
      if(evaluations >  (maxEvaluations * 0.2))
     	  treinou = true;
      
    }
  }

  @Override public List<S> getResult() {
    return getNonDominatedSolutions(population);
  }

  protected List<S> createInitialPopulation() {
    List<S> population = new ArrayList<>(populationSize);
    for (int i = 0; i < populationSize; i++) {
      S newIndividual = problem.createSolution();
      population.add(newIndividual);
    }
    return population;
  }

  protected List<S> evaluatePopulation(List<S> population) {
	  	
	    population = evaluator.evaluate(population, problem);
	    
	    ///*
	    ArrayList Solucoes = new ArrayList<>(population.size());	
	    ArrayList Objetivos = new ArrayList<>(population.size());
	    
	    for(int p = 0; p < population.size(); p++)
	    {
	    	S solucao = population.get(p);
	    
	    	int tamObj = solucao.getNumberOfObjectives();
	    	int tamVar = solucao.getNumberOfVariables();
	    
	    	double[] objetivo = new double[tamObj];
	    
	    	double[] variables = new double[tamVar]; 
	    	
	    	//pego os objetivos da solucao e coloco em um array
	    	for(int i = 0; i< tamObj; i++)
	    		{ objetivo[i] = (double) solucao.getObjective(i); }
	    
	    	//pego as variaveis da solucao e coloco em um array
	    	for(int k = 0; k< tamVar; k++)
	    		{ variables[k] = (double) solucao.getVariableValue(k);}
	    
	    	Solucoes.add(p, variables);
	    	Objetivos.add(p, objetivo);
	    }
	    
	    user userObject = new user(
			    getName(),
			    "treino",
			    Solucoes,
			    Objetivos
			);
	  
	    
		if(treinou == false)
		{   
			ArrayList SwarmInicio = http(urlTreino, userObject);
		}
		else
		{
			//altera a populacao
			ArrayList SwarmInicio = http(urlClassifica, userObject);
		    
		    for(int p = 0; p < population.size(); p++)
		    {
		    	S solucao = population.get(p);
		    	for(int ob = 0; ob < SwarmInicio.size(); ob++)
		    	{
		    		ArrayList object = (ArrayList) SwarmInicio.get(ob);
		    		solucao.setObjective(ob, (double)object.get(p));
		    	}
		    	population.set(p, solucao);
		    }
		   
		}
    //*/
    return population;
  }

  protected Ranking<S> computeRanking(List<S> solutionList) {
    Ranking<S> ranking = new DominanceRanking<S>();
    ranking.computeRanking(solutionList);

    return ranking;
  }

  protected List<S> crowdingDistanceSelection(Ranking<S> ranking) {
    CrowdingDistance<S> crowdingDistance = new CrowdingDistance<S>();
    List<S> population = new ArrayList<>(populationSize);
    int rankingIndex = 0;
    while (populationIsNotFull(population)) {
      if (subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
        addRankedSolutionsToPopulation(ranking, rankingIndex, population);
        rankingIndex++;
      } else {
        crowdingDistance.computeDensityEstimator(ranking.getSubfront(rankingIndex));
        addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
      }
    }

    return population;
  }

  protected boolean populationIsNotFull(List<S> population) {
    return population.size() < populationSize;
  }

  protected boolean subfrontFillsIntoThePopulation(Ranking<S> ranking, int rank, List<S> population) {
    return ranking.getSubfront(rank).size() < (populationSize - population.size());
  }

  protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
    List<S> front;

    front = ranking.getSubfront(rank);

    for (S solution : front) {
      population.add(solution);
    }
  }

  protected void addLastRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
    List<S> currentRankedFront = ranking.getSubfront(rank);

    Collections.sort(currentRankedFront, new CrowdingDistanceComparator<S>());

    int i = 0;
    while (population.size() < populationSize) {
      population.add(currentRankedFront.get(i));
      i++;
    }
  }

  protected List<S> getNonDominatedSolutions(List<S> solutionList) {
    return SolutionListUtils.getNondominatedSolutions(solutionList);
  }

  @Override public String getName() {
    return "NSGAII45" ;
  }

  @Override public String getDescription() {
    return "Nondominated Sorting Genetic Algorithm version II. Version not using the AbstractGeneticAlgorithm template" ;
  }
  
  public static ArrayList http(String url, user userObject) {
	  	
	  	JSONObject json = new JSONObject();
	  	//json.put("valor", "chave");  
	  	ObjectMapper mapper = new ObjectMapper();
	  	String jsonInString = null;
	  	try {
				
				//Convert object to JSON string
				jsonInString = mapper.writeValueAsString(userObject);
				//System.out.println(jsonInString);
				
				//Convert object to JSON string and pretty print
				jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(userObject);
				//System.out.println(jsonInString);
				
				
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	  	
	  	retorno userA = new retorno();
	      try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
	          HttpPost request = new HttpPost(url);
	          StringEntity params = new StringEntity(jsonInString);
	          request.addHeader("content-type", "application/json");
	          request.setEntity(params);
	          HttpResponse result = httpClient.execute(request);
	          
	          String json1 = EntityUtils.toString(result.getEntity(), "UTF-8");
	          

			  userA = mapper.readValue(json1, retorno.class);
			  
			  
				
	          //System.out.println(json1);

	      } catch (IOException ex) {
	      	System.out.println(ex.getMessage());
	      }
	      return userA.getRetorno();
	  }

@Override
public List<DoubleSolution> getResultado() {
	// TODO Auto-generated method stub
	return null;
}
  
}
