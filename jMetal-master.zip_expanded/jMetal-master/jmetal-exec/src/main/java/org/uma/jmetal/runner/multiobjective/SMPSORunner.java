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
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.retorno;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;
import org.uma.jmetal.algorithm.multiobjective.smpso.SMPSOBuilder;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.dtlz.*;
import org.uma.jmetal.problem.multiobjective.wfg.WFG1;
import org.uma.jmetal.problem.multiobjective.wfg.WFG2;
import org.uma.jmetal.problem.multiobjective.wfg.WFG3;
import org.uma.jmetal.problem.multiobjective.wfg.WFG4;
import org.uma.jmetal.problem.multiobjective.wfg.WFG5;
import org.uma.jmetal.problem.multiobjective.wfg.WFG6;
import org.uma.jmetal.problem.multiobjective.wfg.WFG7;
import org.uma.jmetal.problem.multiobjective.wfg.WFG8;
import org.uma.jmetal.problem.multiobjective.wfg.WFG9;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for configuring and running the SMPSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SMPSORunner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments. The first (optional) argument specifies
   *             the problem to solve.
   * @throws org.uma.jmetal.util.JMetalException
   * @throws java.io.IOException
   * @throws SecurityException
   * Invoking command:
  java org.uma.jmetal.runner.multiobjective.SMPSORunner problemName [referenceFront]
   */
  public static void main(String[] args) throws Exception {
	  
	for (int p = 7; p <= 7; p++)
	{
		DoubleProblem problem;
	    Algorithm<List<DoubleSolution>> algorithm;
	    MutationOperator<DoubleSolution> mutation;
	
	    //String referenceParetoFront = "" ;
	    
	    String classifier = null;
	    int indiceClassificador = 2;
		String metodo = "";
		classifier = classificador(indiceClassificador);
		
		String problemName ;
		
		problemName = getNomeProblemDTLZ(p);
		//problemName = getNomeProblem(p);
		int execucao = 0;
	
		for(int i = 0; i < 20; i++)
		{
			execucao = i;
		    
		    ArrayList array = new ArrayList<>(1);
		    array.add(1);
		    
		    user userObject = new user(
					classifier,
				    classifier,
				    new ArrayList<>(),
				    array
				);
			ArrayList SwarmInicio = http("http://127.0.0.1:5000/classificador", userObject);
			
			
		    //referenceParetoFront = "C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\jMetal-master.zip_expanded\\jMetal-master\\jmetal-problem\\src\\test\\resources\\pareto_fronts\\DTLZ1.3D.pf";
		    
		    
		    problem = (DoubleProblem) getProblemDTLZ(problemName, 3);
		    //System.out.println(problem.getName());
		    //problem = (DoubleProblem) getProblem(problemName, 3);
		    System.out.println(problem.getName());
		    metodo = Integer.toString(problem.getNumberOfObjectives());
		    
		    BoundedArchive<DoubleSolution> archive = new CrowdingDistanceArchive<DoubleSolution>(100) ;
		
		    double mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		    double mutationDistributionIndex = 20.0 ;
		    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex) ;
		
		    algorithm = new SMPSOBuilder(problem, archive)
		        .setMutation(mutation)
		        .setExecucao(execucao)
		        .setMetodo(metodo)
		        .setMaxIterations(100)
		        .setSwarmSize(100)
		        .setSolutionListEvaluator(new SequentialSolutionListEvaluator<DoubleSolution>())
		        .build();
		
		    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
		        .execute();
		
		    //List<DoubleSolution> population = algorithm.getResultado();
		    //long computingTime = algorithmRunner.getComputingTime();
		
		    //JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
		
		    //printFinalSolutionSet(population);
		   // if (!referenceParetoFront.equals("")) {
		   //   printQualityIndicators(population, referenceParetoFront) ;
		   // }
		  }
	}
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
  
  
  public static String getNomeProblemDTLZ(int nome)
  {
	  String nomeP = null;
	  switch(nome)
	  {
	  case 1:
		  nomeP = "DTLZ1";
		  break;
	  case 2:
		  nomeP = "DTLZ2";
		  break;
	  case 3:
		  nomeP = "DTLZ3";
		  break;
	  case 4:
		  nomeP = "DTLZ4";
		  break;
	  case 5:
		  nomeP = "DTLZ5";
		  break;
	  case 6:
		  nomeP = "DTLZ6";
		  break;
	  case 7:
		  nomeP = "DTLZ7";
		  break;
	  default:
		  nomeP = null;
		  break;
	  }
	  
	return nomeP;  
  }
  
  public static Problem<DoubleSolution> getProblemDTLZ(String prob, int nObj)
  {
	  int k = -1;
	  if(nObj == 3)
		  k = 12;
	  else if(nObj == 10)
		  k = 19;
	  Problem<DoubleSolution> nomeP = null;
	  switch(prob)
	  {
	  case "DTLZ1":
		  nomeP = new DTLZ1(k,nObj);
		  break;
	  case "DTLZ2":
		  nomeP = new DTLZ2(k,nObj);
		  break;
	  case "DTLZ3":
		  nomeP = new DTLZ3(k,nObj);
		  break;
	  case "DTLZ4":
		  nomeP = new DTLZ4(k,nObj);
		  break;
	  case "DTLZ5":
		  nomeP = new DTLZ5(k,nObj);
		  break;
	  case "DTLZ6":
		  nomeP = new DTLZ6(k,nObj);
		  break;
	  case "DTLZ7":
		  nomeP = new DTLZ7(k,nObj);
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
