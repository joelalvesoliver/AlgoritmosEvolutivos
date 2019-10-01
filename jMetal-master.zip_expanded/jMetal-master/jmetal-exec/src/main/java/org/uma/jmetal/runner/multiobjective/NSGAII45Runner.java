package org.uma.jmetal.runner.multiobjective;

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
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII45;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.retorno;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.*;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.problem.multiobjective.dtlz.*;
import org.uma.jmetal.problem.multiobjective.wfg.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to configure and run the implementation of the NSGA-II algorithm included
 * in {@link NSGAII45}
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAII45Runner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   * @throws JMetalException
 * @throws IOException 
   */
  public static void main(String[] args) throws JMetalException, IOException {
    
	for(int p = 1; p <= 9; p++)
	{
		Problem<DoubleSolution> problem;
	    Algorithm<List<DoubleSolution>> algorithm;
	    CrossoverOperator<DoubleSolution> crossover;
	    MutationOperator<DoubleSolution> mutation;
	    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
	    String referenceParetoFront = "" ;
	    InvertedGenerationalDistance indice;
	    
	    int execucao = 0;
		int indiceClassificador = 3;
		String classifier = null;
		String metodo = "batch";
		classifier = classificador(indiceClassificador);
		ArrayList igds = new ArrayList<>();
		int object = -1;
		String algoritmo = null; 
		
		String nameProblem = null;
		
		nameProblem = getNomeProblem(p);
		
		int maxEval = 10000;
		int populationSize = 250;
		String surrogate = "Surrogate_";
		
		for(int i = 0; i < 20; i++)
		{
		    String problemName ;
		    if (args.length == 1) {
		      problemName = args[0];
		    } else if (args.length == 2) {
		      problemName = args[0] ;
		      referenceParetoFront = args[1] ;
		    } else {
		      //problemName = "org.uma.jmetal.problem.multiobjective.dtlz."+ nameProblem;
		      problemName = "org.uma.jmetal.problem.multiobjective.wfg."+ nameProblem;
		      referenceParetoFront = "";
		    }
		
		    //problem = ProblemUtils.<DoubleSolution> loadProblem(problemName);
		    //problem = problem.createSolution();
		    problem = getProblem(nameProblem, 3);
		    
		    //problem = new DTLZ7(19, 10);
		    
		    object = problem.getNumberOfObjectives();
		    
		    referenceParetoFront = "/home/joe/MESTRADO_LINUX/eclipse-workspace/jMetal-master.zip_expanded/jMetal-master/jmetal-problem/src/test/resources/pareto_fronts/"+nameProblem+"."+Integer.toString(object)+"D.pf";   
		    //referenceParetoFront = "/home/joe/MESTRADO_LINUX/eclipse-workspace/jMetal-master.zip_expanded/jMetal-master/jmetal-problem/src/test/resources/pareto_fronts/DTLZ2.10D.pf";   
		    if(object == 10)
		    	populationSize = 764;
		    else if(object == 3)
		    	populationSize = 91;
		    
		    ArrayList array = new ArrayList<>(1);
		    array.add(object);
		    
		    user userObject = new user(
					classifier,
				    classifier,
				    new ArrayList<>(),
				    array
				);
			ArrayList SwarmInicio = http("http://127.0.0.1:5000/classificador", userObject);
			
		    
		    double crossoverProbability = 0.9 ;
		    double crossoverDistributionIndex = 20.0 ;
		    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex) ;
		
		    double mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		    double mutationDistributionIndex = 20.0 ;
		    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex) ;
		
		    selection = new BinaryTournamentSelection<DoubleSolution>(new RankingAndCrowdingDistanceComparator<DoubleSolution>());
		
		    algorithm = new NSGAII45<DoubleSolution>(problem, maxEval,populationSize, crossover, mutation,
		            selection, new SequentialSolutionListEvaluator<DoubleSolution>()) ;
		    
		    algoritmo = algorithm.getName();
		    
		    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
		            .execute() ;
		
		    List<DoubleSolution> population = algorithm.getResult() ;
		    //long computingTime = algorithmRunner.getComputingTime() ;
		    
		    
		    
		    new SolutionListOutput(population)
	        .setSeparator("\t")
	        .setVarFileOutputContext(new DefaultFileOutputContext(surrogate +"VAR"+Integer.toString(maxEval)+"Eval"+nameProblem+"_"+Integer.toString(object)+"OBJ_Population_"+Integer.toString(populationSize)+"_EXC"+i+".tsv"))
	        .setFunFileOutputContext(new DefaultFileOutputContext(surrogate +"FUN"+Integer.toString(maxEval)+"Eval"+nameProblem+"_"+Integer.toString(object)+"OBJ_Population_"+Integer.toString(populationSize)+"_EXC"+i+".tsv"))
	        .print();
		
		    //JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
		
		    printFinalSolutionSet(population);
		    if (!referenceParetoFront.equals("")) {
		      //printQualityIndicators(population, referenceParetoFront) ;
		      indice = new InvertedGenerationalDistance(referenceParetoFront,2.0);
		      double IGD = indice.evaluate(population);
		      igds.add(IGD);
		      //System.out.println(IGD);
		    }
		}
		
		String ProblemNAme = "/home/joe/MESTRADO_LINUX/EXPERIMENTOS_NSGA2/"+surrogate+algoritmo+"_"+nameProblem+"_"+Integer.toString(object)+"_Objectivos"+Integer.toString(maxEval)+"Eval_Population_"+Integer.toString(populationSize);
		
		user userObject = new user(
				ProblemNAme,
				ProblemNAme,
				new ArrayList<>(),
			    igds
			);
		ArrayList SwarmInicio = http("http://127.0.0.1:5000/save", userObject);
		
	}
	//try(FileOutputStream f = new FileOutputStream("/home/joe/MESTRADO_LINUX/"+algoritmo+"_"+Integer.toString(object)+"_Objectivos.txt");
	//	    ObjectOutput s = new ObjectOutputStream(f)) {
	//	    s.writeObject(igds);
		   
	//	}
    
  }
  
  public static Problem<DoubleSolution> getProblem(String prob, int nObj)
  {
	  int k = -1;
	  if(nObj == 3)
		  k = 4;
	  else if(nObj == 10)
		  k = 9;
	  Problem<DoubleSolution> nomeP = null;
	  switch(prob)
	  {
	  case "WFG1":
		  nomeP = new WFG1(k,10,nObj);
		  break;
	  case "WFG2":
		  nomeP = new WFG2(k,10,nObj);
		  break;
	  case "WFG3":
		  nomeP = new WFG3(k,10,nObj);
		  break;
	  case "WFG4":
		  nomeP = new WFG4(k,10,nObj);
		  break;
	  case "WFG5":
		  nomeP = new WFG5(k,10,nObj);
		  break;
	  case "WFG6":
		  nomeP = new WFG6(k,10,nObj);
		  break;
	  case "WFG7":
		  nomeP = new WFG7(k,10,nObj);
		  break;
	  case "WFG8":
		  nomeP = new WFG8(k,10,nObj);
		  break;
	  case "WFG9":
		  nomeP = new WFG9(k,10,nObj);
		  break;
	  default:
		  nomeP = null;
		  break;
	  }
	  return nomeP;
  }
  
  public static String getNomeProblem(int nome)
  {
	  String nomeP = null;
	  switch(nome)
	  {
	  case 1:
		  nomeP = "WFG1";
		  break;
	  case 2:
		  nomeP = "WFG2";
		  break;
	  case 3:
		  nomeP = "WFG3";
		  break;
	  case 4:
		  nomeP = "WFG4";
		  break;
	  case 5:
		  nomeP = "WFG5";
		  break;
	  case 6:
		  nomeP = "WFG6";
		  break;
	  case 7:
		  nomeP = "WFG7";
		  break;
	  case 8:
		  nomeP = "WFG8";
		  break;
	  case 9:
		  nomeP = "WFG9";
		  break;
	  default:
		  nomeP = null;
		  break;
	  }
	  
	return nomeP;  
  }
  
  public static String classificador(int index)
  {
	  String classificador = null;
	  switch (index) {
	case 1:
		classificador = "SVM";
		break;
	case 2:
		classificador = "RAMDOMFOREST";
		break;
	case 3:
		classificador = "TREE";
		break;
	default:
		classificador = null;
		break;
	}
	  return classificador;
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
