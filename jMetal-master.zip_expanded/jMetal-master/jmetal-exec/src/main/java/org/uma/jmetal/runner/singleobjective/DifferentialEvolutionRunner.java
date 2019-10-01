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
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.singleobjective.Ackley;
import org.uma.jmetal.problem.singleobjective.Ellipsoid;
import org.uma.jmetal.problem.singleobjective.Griewank;
import org.uma.jmetal.problem.singleobjective.Rastrigin;
import org.uma.jmetal.problem.singleobjective.Rosenbrock;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to configure and run a differential evolution algorithm. The algorithm can be configured
 * to use threads. The number of cores is specified as an optional parameter. The target problem is Sphere.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class DifferentialEvolutionRunner {
  private static final int DEFAULT_NUMBER_OF_CORES = 1 ;

  /**
   *  Usage: java org.uma.jmetal.runner.singleobjective.DifferentialEvolutionRunner [cores]
   */
  public static void main(String[] args) throws Exception {

	int execucao = 0;
	int indiceClassificador = 3;
	String classifier = null;
	classifier = classificador(indiceClassificador);
	String metodo = "online";
	DoubleProblem problem = new Ellipsoid(20);

	Algorithm<DoubleSolution> algorithm;
    DifferentialEvolutionSelection selection;
    DifferentialEvolutionCrossover crossover;
    SolutionListEvaluator<DoubleSolution> evaluator ;

    user userObject = new user(
			classifier,
		    classifier,
		    new ArrayList<>(),
		    new ArrayList<>()
		);
	ArrayList SwarmInicio = http("http://127.0.0.1:5000/classificador", userObject);
	
	
	int indexP= 1;
	for(int i = 0; i < 20; i++)
	{
		execucao = i % 20;
		if(execucao == 0)
		{
			switch (indexP) {
			//case 1:
			//	problem = new Ellipsoid(20);
			//	break;
			//case 2:
			//	problem = new Ackley(20);
			//	break;
			case 1:
				problem = new Rastrigin(20);
				break;
			//case 4: 
			//	problem = new Rosenbrock(20);
			//	break;
			//case 5:
			//	problem = new Griewank(20);
			//	break;
				
			default:
				problem = null;
				break;
			}
			indexP +=1;
		}

    	int numberOfCores ;
    	if (args.length == 1) {
    	numberOfCores = Integer.valueOf(args[0]) ;
    	} else {
    		numberOfCores = DEFAULT_NUMBER_OF_CORES ;
    	}

    	if (numberOfCores == 1) {
    		evaluator = new SequentialSolutionListEvaluator<DoubleSolution>() ;
    	} else {
    		evaluator = new MultithreadedSolutionListEvaluator<DoubleSolution>(numberOfCores, problem) ;
    	}

    	crossover = new DifferentialEvolutionCrossover(0.5, 0.5, "rand/1/bin") ;
    	selection = new DifferentialEvolutionSelection();

    	algorithm = new DifferentialEvolutionBuilder(problem)
    			.setCrossover(crossover)
    			.setSelection(selection)
    			.setSolutionListEvaluator(evaluator)
    			.setMaxEvaluations(100000)
    			.setPopulationSize(100)
    			.setExecucao(execucao)
    			.setClassificador(classifier)
    			.setMetodo(metodo) // quando for aplicar outra metodo muda esse parametro
    			.setNtreino(10000)
    			.build() ;

    	AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
    			.execute() ;

    	//DoubleSolution solution = algorithm.getResult() ;
    	//long computingTime = algorithmRunner.getComputingTime() ;

    	//List<DoubleSolution> population = new ArrayList<>(1) ;
    	//population.add(solution) ;
    	//new SolutionListOutput(population)
        //	.setSeparator("\t")
        //	.setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        //	.setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        //	.print();

    //JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    //JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    //JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

    //JMetalLogger.logger.info("Fitness: " + solution.getObjective(0)) ;

    	evaluator.shutdown();
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
