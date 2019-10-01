package org.uma.jmetal.runner.singleobjective;

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
//import org.uma.jmetal.problem.singleobjective.Rosenbrock;
import org.uma.jmetal.problem.singleobjective.*;
import org.uma.jmetal.problem.singleobjective.cec2005competitioncode.*;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.impl.MersenneTwisterGenerator;

import org.uma.jmetal.problem.multiobjective.dtlz.*;
import org.uma.jmetal.problem.multiobjective.wfg.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for configuring and running the SMPSO algorithm to solve a single-objective problem
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
    
	// for para execultar 20 vezes
	
	int execucao = 0;
	int indiceClassificador = 3;
	String classifier = null;
	String metodo = "online";
	classifier = classificador(indiceClassificador);
	DoubleProblem problem = null;
	
	//indiceClassificador+=1;
	//chama a funcao classificador para escolher o classificador, incrementa o indice classificador
	//seta no servidor o classificador
	user userObject = new user(
			classifier,
		    classifier,
		    new ArrayList<>(),
		    new ArrayList<>()
		);
	ArrayList SwarmInicio = http("http://127.0.0.1:5000/classificador", userObject);

	int indexP= 1;
	int execuções = 1; // MUDAR PARA MUDAR O NUMERO DE EXECUCOES
	for(int i = 0; i < execuções; i++)
	{
		
		execucao = i % 20;
		if(execucao == 0)
		{
			
			switch (indexP) {
			/*case 1:
				problem = new Ellipsoid(20);
				//problem = new CEC2005Problem(8,20);
				break;
			case 2:
				problem = new Ackley(20);
				break;
				
			case 3:
				problem = new Griewank(20);
				break;
			*/	
			case 1:
				problem = new DTLZ1(12,3);
				break;
				
			default:
				problem = null;
				break;
			}
			indexP +=1;
		}
		
		
	    Algorithm<List<DoubleSolution>> algorithm;
	    MutationOperator<DoubleSolution> mutation;

	    //problem = new Rosenbrock(20) ;
	    //problem = new Sphere(20);
	    
	    BoundedArchive<DoubleSolution> archive = new CrowdingDistanceArchive<DoubleSolution>(100) ;

	    double mutationProbability = 1.0 / problem.getNumberOfVariables() ;
	    double mutationDistributionIndex = 20.0 ;
	    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex) ;

	    algorithm = new SMPSOBuilder(problem, archive)
	        .setMutation(mutation)
	        .setMaxIterations(1000)
	        .setSwarmSize(100)
	        //.setRandomGenerator(new MersenneTwisterGenerator())
	        .setSolutionListEvaluator(new SequentialSolutionListEvaluator<DoubleSolution>())
	        .setExecucao(execucao)
	        .setClassifier(classifier)
	        .setMetodo(metodo)
	        .setNtreino(5000) //MUDAR PARA SETAR O TAMANHO DA BASE DE TREINO E TESTE
	        .build();

	    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
	        .execute();

	    List<DoubleSolution> population = algorithm.getResult();
	    long computingTime = algorithmRunner.getComputingTime();

	    new SolutionListOutput(population)
	            .setSeparator("\t")
	            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
	            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
	            .print();

	    //JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
	    //JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    //JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

	    //JMetalLogger.logger.info("Fitness: " + population.get(0).getObjective(0)) ;
	}
	
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
