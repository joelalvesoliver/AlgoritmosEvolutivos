package org.uma.jmetal.algorithm.singleobjective.differentialevolution;

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
import org.uma.jmetal.algorithm.impl.AbstractDifferentialEvolution;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.retorno;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements a differential evolution algorithm.
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DifferentialEvolution extends AbstractDifferentialEvolution<DoubleSolution> {
  private int populationSize;
  private int maxEvaluations;
  private SolutionListEvaluator<DoubleSolution> evaluator;
  private Comparator<DoubleSolution> comparator;

  private int evaluations;
  private int execucao;
  private String classifier;
  
  private String urlTreino = "http://127.0.0.1:5000/treinamento";
  private String urlClassifica = "http://127.0.0.1:5000/classifica";
  private String urlSalva = "http://127.0.0.1:5000/save";
  private String urlInicializa = "http://127.0.0.1:5000/inicializa";
  private String urlClassificador = "http://127.0.0.1:5000/classificador";
  private String urlBase = "http://127.0.0.1:5000/guarda";
  private ArrayList Solucoes = new ArrayList();
  private ArrayList Objetivos = new ArrayList();
  private int contErro = 0;
  private int parada = 0;
  private String problema;
  private String metodo;
  private int nTreino = 0;
  /**
   * Constructor
   *
   * @param problem Problem to solve
   * @param maxEvaluations Maximum number of evaluations to perform
   * @param populationSize
   * @param crossoverOperator
   * @param selectionOperator
   * @param evaluator
   */
  public DifferentialEvolution(DoubleProblem problem, int maxEvaluations, int populationSize,
      DifferentialEvolutionCrossover crossoverOperator,
      DifferentialEvolutionSelection selectionOperator, SolutionListEvaluator<DoubleSolution> evaluator, int execucao, String Classifier, String metodo, int nTreino) {
    setProblem(problem); ;
    this.maxEvaluations = maxEvaluations;
    this.populationSize = populationSize;
    this.crossoverOperator = crossoverOperator;
    this.selectionOperator = selectionOperator;
    this.evaluator = evaluator;
    this.problema = problem.getName();
    this.execucao = execucao;
    this.classifier = Classifier;
    this.metodo = metodo;
    this.nTreino = nTreino;
    comparator = new ObjectiveComparator<DoubleSolution>(0);
  }
  
  public int getEvaluations() {
    return evaluations;
  }

  public void setEvaluations(int evaluations) {
    this.evaluations = evaluations;
  }

  @Override protected void initProgress() {
    evaluations = populationSize;
  }

  @Override protected void updateProgress() {
    evaluations += populationSize;
  }
  
  @Override protected boolean isStopping() {
	  System.out.println("O valor de parada: "+parada+"!");
	  return parada >= 100000;
  }
  @Override protected boolean isStoppingConditionReached() {
	  
	  if (evaluations >= maxEvaluations)
	  {
		  if(metodo.equals("online")|| metodo.equals("batch") || metodo.equals("base"))
		  {
			  int Inverso = 100000 - nTreino;
			  String nome = null;
			  if(metodo.equals("online"))
			  {
				  nome = getName()+"_"+ classifier +"_"+problema+"_ONLINE100_TREINO"+nTreino+"_TESTE"+Inverso+"_Populacao100_NumeroInteracoes"+maxEvaluations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
				    
			  }
			  else if(metodo.equals("batch"))
			  {
				  nome = getName()+"_"+ classifier +"_"+problema+"_BATCH50k_TREINO"+nTreino+"_TESTE"+Inverso+"_Populacao100_NumeroInteracoes"+maxEvaluations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
			  }
			  else
			  {
				  nome = getName()+"_"+problema+"_Populacao100_NumeroInteracoes"+maxEvaluations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
			  }
			   
			   //String 
			  
			  user userObject = new user(
					    nome,
					    "none",
					    Solucoes,
					    Objetivos
					);
			  
			  ArrayList SwarmInicio = http(urlSalva, userObject);
			  
		  }

	  }
	  //System.out.println("Valores treino "+nTreino+" e valor interacao "+evaluations);
	  if(metodo.equals("online") && evaluations >= nTreino)
		  contErro = 1;
		  
	  return evaluations >= maxEvaluations;
  }
  
  
  @Override
  protected double avaliation(List<DoubleSolution> population)
  {
	  return  population.get(0).getObjective(0);
  }
  @Override
  
  public List<DoubleSolution> Uniao(List<DoubleSolution> DE, List<DoubleSolution> DE1)
  {	  
	
	  List<DoubleSolution> retorno = new ArrayList<>();
	  
	  for (int k = 0; k<DE.size(); k++)
	  {
		  DoubleSolution solution = DE.get(k);
		  retorno.add(solution);
	  }
	  
	  if(DE1 != null)
	  {
		  for (int i = 0; i<DE1.size(); i++)
		  {
			  DoubleSolution solution = DE1.get(i);
			  retorno.add(solution);
		  }
	  }
	  
	  return retorno;
  }
  
  
  @Override protected List<DoubleSolution> createInitialPopulation() {
    List<DoubleSolution> population = new ArrayList<>(populationSize);
    for (int i = 0; i < populationSize; i++) {
      DoubleSolution newIndividual = getProblem().createSolution();
      population.add(newIndividual);
    }
    return population;
  }
  
  @Override 
  public List<DoubleSolution> createInitialDESurrogate(int variaveis, int solucoes)
  {

	  Objetivos.add(variaveis);
	  Solucoes.add(solucoes);
	    
	  user userObject = new user(
			    getName(),
			    "none",
			    Solucoes,
			    Objetivos
			);
	  
	  ArrayList DEInicio = http(urlInicializa, userObject);
	  
	  List <DoubleSolution> solutions = new ArrayList<>(DEInicio.size());
	  DoubleSolution newSolution;
	  ArrayList solution;
	  
	  for(int i = 0; i < DEInicio.size(); i++)
	  {
		  newSolution = problem.createSolution();
		  solution =  (ArrayList)DEInicio.get(i);
	      for(int k = 0; k < solution.size(); k++)
	      {
	    	  newSolution.setVariableValue(k, (double)solution.get(k));
	      }
	      solutions.add(newSolution);
	      
	  }
	  
	  Objetivos.clear();
	  Solucoes.clear();
	  
	  return solutions;
  }

  
  @Override
  public List<DoubleSolution> evaluateSurogateDE(List<DoubleSolution> swarm)
  {	 
	 List<DoubleSolution> swarmSurrogate = new ArrayList<>(swarm.size());
	 swarm = evaluator.evaluate(swarm, problem);
	 
	 for (int i = 0; i < swarm.size(); i++)
	 {
		 DoubleSolution solution = swarm.get(i);
		 double[] xvariaveis = new double[solution.getNumberOfVariables()];
		 for(int k = 0; k< solution.getNumberOfVariables(); k++)
		 {
			 xvariaveis[k] = solution.getVariableValue(k);
		 }
		 Solucoes.add(xvariaveis);
		 Objetivos.add(solution.getObjective(0));
	 }
	 
	 user userObject = new user(
			    getName(),
			    "classifica",
			    Solucoes,
			    Objetivos
			);
	 
	 ArrayList SwarmAvalia = http(urlClassifica, userObject);
	 
	 for (int ii = 0; ii < SwarmAvalia.size(); ii++)
	 {
		 double objetivo = (double) SwarmAvalia.get(ii);
		 DoubleSolution solution = swarm.get(ii);
		 solution.setObjective(0, objetivo);
		 swarmSurrogate.add(solution);
	 }
	 
	 parada += SwarmAvalia.size();
	 
	 if(parada >= 100000)
	 {
		 String nome = getName()+"_"+ classifier +"_"+problema+"_SURROGATE_THETA5_Populacao100_NumeroInteracoes"+maxEvaluations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
		 user userObject1 = new user(
				 	nome,
				    "treino",
				    Solucoes,
				    Objetivos
				);
		 
		 paradaSurrogate(userObject1);
	 }
	 
	 Objetivos.clear();
	 Solucoes.clear();
	 return swarmSurrogate;  
  }
  
  
  
  @Override protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
    //return evaluator.evaluate(population, getProblem());
	  List<DoubleSolution> DESurrogate = new ArrayList<>(population.size());
	  population = evaluator.evaluate(population, getProblem());
	  
	  for (int i = 0; i < population.size(); i++)
		 {
			 DoubleSolution solution = population.get(i);
			 double[] xvariaveis = new double[solution.getNumberOfVariables()];
			 for(int k = 0; k < solution.getNumberOfVariables(); k++)
			 {
				 xvariaveis[k] = solution.getVariableValue(k);
			 }
			 Solucoes.add(xvariaveis);
			 Objetivos.add(solution.getObjective(0));
		 }
	  
	  String url = null;
	  if(contErro == 1)
		  url = urlClassifica;
	  else
		  url = urlTreino;
	  
	 if(metodo.equals("base"))
		 url = urlBase;
		 
	 if(contErro == 1)	 
	 {  
		 user userObject = new user(
				    getName(),
				    "treino",
				    Solucoes,
				    Objetivos
				);

		ArrayList SwarmAvalia = http(url, userObject);
		 for (int ii = 0; ii < SwarmAvalia.size(); ii++)
		 {
			 double objetivo = (double) SwarmAvalia.get(ii);
			 DoubleSolution solution = population.get(ii);
			 solution.setObjective(0, objetivo);
			 DESurrogate.add(solution);
		 }
		 population = DESurrogate;
		 Objetivos.clear();
		 Solucoes.clear();
	 }
	 
	 if (Objetivos.size() > nTreino)
	  {
		  user userObject = new user(
				    getName(),
				    "treino",
				    Solucoes,
				    Objetivos
				);
		  
		  ArrayList SwarmAvalia = http(url, userObject);
		  if(metodo.equals("batch"))
			  contErro = 1;
		  
		  Objetivos.clear();
		  Solucoes.clear();
	  }
	 
	 
	  if((contErro == 0) && metodo.equals("online"))
	  {
		  user userObject = new user(
		  		    getName(),
		     	    "treino",
		             Solucoes,
		  		     Objetivos
		  	);
		  
		  ArrayList SwarmAvalia = http(url, userObject);
		  
		  Objetivos.clear();
		  Solucoes.clear();
	  }
	  
	  if(metodo.equals("surrogate"))
	  {
		     parada += population.size();
		     
		     user userObject = new user(
					    getName(),
					    "treino",
					    Solucoes,
					    Objetivos
					);

			ArrayList SwarmAvalia = http(url, userObject);
			
			 //condicao para parada do algoritmo
			 if(parada >= 100000)
			 {	
				 String nome = getName()+"_"+ classifier +"_"+problema+"_SURROGATE_THETA5_Populacao100_NumeroInteracoes"+maxEvaluations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
				 user userObject1 = new user(
						 	nome,
						    "treino",
						    Solucoes,
						    Objetivos
						);
				 
				 paradaSurrogate(userObject1);
			 
			 }
			 
			 Objetivos.clear();
			 Solucoes.clear();
	  }
	 

	 return population;
  }

  @Override protected List<DoubleSolution> selection(List<DoubleSolution> population) {
    return population;
  }

  @Override protected List<DoubleSolution> reproduction(List<DoubleSolution> matingPopulation) {
    List<DoubleSolution> offspringPopulation = new ArrayList<>();

    for (int i = 0; i < populationSize; i++) {
      selectionOperator.setIndex(i);
      List<DoubleSolution> parents = selectionOperator.execute(matingPopulation);

      crossoverOperator.setCurrentSolution(matingPopulation.get(i));
      List<DoubleSolution> children = crossoverOperator.execute(parents);

      offspringPopulation.add(children.get(0));
    }

    return offspringPopulation;
  }

  @Override protected List<DoubleSolution> replacement(List<DoubleSolution> population,
      List<DoubleSolution> offspringPopulation) {
    List<DoubleSolution> pop = new ArrayList<>();

    for (int i = 0; i < populationSize; i++) {
      if (comparator.compare(population.get(i), offspringPopulation.get(i)) < 0) {
        pop.add(population.get(i));
      } else {
        pop.add(offspringPopulation.get(i));
      }
    }

    Collections.sort(pop, comparator) ;
    return pop;
  }

  
  @Override
  public List<DoubleSolution> getLideres(List<DoubleSolution> popularion) {
	  List<DoubleSolution> lederes = new ArrayList<>();
	  List<DoubleSolution> Listaux = popularion; 
	  DoubleSolution particule;
	  DoubleSolution aux;
	  
	  int tam = 0;
	  while (tam < 100)
	  {
		particule = Listaux.get(0);
		for(int i=1;i<Listaux.size();i++) 
		{
			aux = Listaux.get(i);
			if(aux.getObjective(0) < particule.getObjective(0))
			{
				particule = aux;
			}		
		}
		lederes.add(particule);
		tam = lederes.size();
		Listaux.remove(particule);
		
	  }
	  
	  return lederes;
    
  }
  
  /**
   * Returns the best individual
   */
  @Override public DoubleSolution getResult() {
    Collections.sort(getPopulation(), comparator) ;

    return getPopulation().get(0);
  }

  @Override public String getName() {
    return "DE" ;
  }

  @Override public String getDescription() {
    return "Differential Evolution Algorithm" ;
  }
  
  public void paradaSurrogate(user userObject)
  {
	  //String nome = getName()+ "_RANDOMFOREST_BATCH50k_TREINO50_TESTE50_Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
	  //String nome = getName()+ "_RANDOMFOREST_ONLINE100_TREINO50_TESTE50_Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
	  //String nome = getName()+ "Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
	  
	  ArrayList SwarmInicio = http(urlSalva, userObject);
	  
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
  
}
