package org.uma.jmetal.algorithm.multiobjective.smpso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

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
import org.uma.jmetal.algorithm.impl.AbstractParticleSwarmOptimization;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.retorno;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.impl.ArrayPoint;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

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
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;

import exceptions.EvaluationException;
import exceptions.MisplacedTokensException;
import exceptions.TooManyDecimalPointsException;
import kktpm.KKTPMCalculator;
import parsing.KKTPM;
import parsing.OptimizationProblem;
import com.mathworks.toolbox.javabuilder.*;
import main.*;

/**
 * This class implements the SMPSO algorithm described in:
 * SMPSO: A new PSO-based metaheuristic for multi-objective optimization
 * MCDM 2009. DOI: http://dx.doi.org/10.1109/MCDM.2009.4938830
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SMPSO extends AbstractParticleSwarmOptimization<DoubleSolution, List<DoubleSolution>> {
  private DoubleProblem problem;

  private double c1Max;
  private double c1Min;
  private double c2Max;
  private double c2Min;
  private double r1Max;
  private double r1Min;
  private double r2Max;
  private double r2Min;
  private double weightMax;
  private double weightMin;
  private double changeVelocity1;
  private double changeVelocity2;
  private int parada = 0;
  private int swarmSize;
  private int maxIterations;
  private int iterations;

  private GenericSolutionAttribute<DoubleSolution, DoubleSolution> localBest;
  private double[][] speed;

  private JMetalRandom randomGenerator;

  private BoundedArchive<DoubleSolution> leaders;
  private Comparator<DoubleSolution> dominanceComparator;

  private MutationOperator<DoubleSolution> mutation;

  private double deltaMax[];
  private double deltaMin[];
  private SolutionListEvaluator<DoubleSolution> evaluator;
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
  private String problema;
  private String metodo;
  private int nTreino = 0;
  private boolean treinou = false;
  
  
  /**
   * Constructor
   */
  public SMPSO(DoubleProblem problem, int swarmSize, BoundedArchive<DoubleSolution> leaders,
               MutationOperator<DoubleSolution> mutationOperator, int maxIterations, double r1Min, double r1Max,
               double r2Min, double r2Max, double c1Min, double c1Max, double c2Min, double c2Max,
               double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
               SolutionListEvaluator<DoubleSolution> evaluator, int execucao, String classificador, String metodo, int nTreino) {
    this.problem = problem;
    this.swarmSize = swarmSize;
    this.leaders = leaders;
    this.mutation = mutationOperator;
    this.maxIterations = maxIterations;
    this.problema = problem.getName();
    this.r1Max = r1Max;
    this.r1Min = r1Min;
    this.r2Max = r2Max;
    this.r2Min = r2Min;
    this.c1Max = c1Max;
    this.c1Min = c1Min;
    this.c2Max = c2Max;
    this.c2Min = c2Min;
    this.weightMax = weightMax;
    this.weightMin = weightMin;
    this.changeVelocity1 = changeVelocity1;
    this.changeVelocity2 = changeVelocity2;
    this.execucao = execucao;
    this.classifier = classificador;
    this.metodo = metodo;
    this.nTreino = nTreino;
    randomGenerator = JMetalRandom.getInstance();
    this.evaluator = evaluator;

    dominanceComparator = new DominanceComparator<DoubleSolution>();
    localBest = new GenericSolutionAttribute<DoubleSolution, DoubleSolution>();
    speed = new double[swarmSize][problem.getNumberOfVariables()];

    deltaMax = new double[problem.getNumberOfVariables()];
    deltaMin = new double[problem.getNumberOfVariables()];
    for (int i = 0; i < problem.getNumberOfVariables(); i++) {
      deltaMax[i] = (problem.getUpperBound(i) - problem.getLowerBound(i)) / 2.0;
      deltaMin[i] = -deltaMax[i];
    }
  }

  protected void updateLeadersDensityEstimator() {
    leaders.computeDensityEstimator();
  }
  
  @Override
  protected void updateProgress() {
    iterations += 1;
    updateLeadersDensityEstimator();
  }

  
  @Override
  protected boolean isStopping() {
	  //System.out.println("O valor de parada: "+parada+"!");
	  return parada >= 100000;
  }
  
  @Override
  protected boolean isStoppingConditionReached() {
	  
	  /*if (iterations >= maxIterations)
	  {
		 if(metodo.equals("online") || metodo.equals("batch") || metodo.equals("base"))
		 {
			int Inverso = 100000 - nTreino;
			String nome = null; 
			if(metodo.equals("online"))
			{
				nome = getName()+"_"+ classifier +"_"+problema+"_ONLINE100_TREINO"+nTreino+"_TESTE"+Inverso+"_Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
			}
			else if(metodo.equals("batch"))
			{
				nome = getName()+"_"+ classifier +"_"+problema+"_BATCH50k_TREINO"+nTreino+"_TESTE"+Inverso+"_Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";  
			}
			else
			{
				nome = getName()+"_"+problema+"_Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
			}
			
			  user userObject = new user(
					    nome,
					    "none",
					    Solucoes,
					    Objetivos
					);
			  
			  ArrayList SwarmInicio = http(urlSalva, userObject);
			  
		 }
	  }
	  
	 //System.out.println("Valores treino "+nTreino+" e valor interacao "+iterations);
	  if(metodo.equals("online") && iterations > nTreino)
		  contErro = 1;
	  */
	  
	  if(iterations >= (maxIterations*0.2)+1)
		  treinou = true;
	  
	  if(iterations >= maxIterations)
	  {
		  List<DoubleSolution> populacao = createInitialSwarm1(swarm);
		  populacao = evaluator.evaluate(populacao, problem);
		  
		  int numObjetives = populacao.get(0).getNumberOfObjectives();
		  String nameProblem = problem.getName();
		  String referenceParetoFront = "C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\jMetal-master.zip_expanded\\jMetal-master\\jmetal-problem\\src\\test\\resources\\pareto_fronts\\"+nameProblem+"."+Integer.toString(numObjetives)+"D.pf";
		  double IGD = 0.0;
		  if (!referenceParetoFront.equals("")) {
		
			  InvertedGenerationalDistance indice;
		      try {
				indice = new InvertedGenerationalDistance(referenceParetoFront,2.0);
				IGD = indice.evaluate(populacao);
				//System.out.println(IGD);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		      
		  }
		  
		  if(execucao==19)
		  {
			  String ProblemNAme = "C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\IGDS\\IGD_"+nameProblem+"_"+Integer.toString(numObjetives)+"OBJ";
			  user userObject = new user(
						ProblemNAme,
						ProblemNAme,
						new ArrayList<>(),
						new ArrayList<>()
					);
				ArrayList SwarmInicio = http("http://127.0.0.1:5000/saveIGD", userObject);
		  }else
		  {
			  ArrayList array = new ArrayList<>(1);
			  array.add(IGD);
			  user userObject = new user(
						"",
						"",
						array,
						array
					);
				ArrayList SwarmInicio = http("http://127.0.0.1:5000/IGD", userObject);
		  }
	  }
	  
	  return iterations >= maxIterations;
    
  }
  @Override
  public List<DoubleSolution> Uniao(List<DoubleSolution> swarm, List<DoubleSolution> swarm1)
  {	  
	
	  List<DoubleSolution> retorno = new ArrayList<>();
	  
	  for (int k = 0; k<swarm.size(); k++)
	  {
		  DoubleSolution solution = swarm.get(k);
		  retorno.add(solution);
	  }
	  
	  if(swarm1 != null)
	  {
		  for (int i = 0; i<swarm1.size(); i++)
		  {
			  DoubleSolution solution = swarm1.get(i);
			  retorno.add(solution);
		  }
	  }
	  
	  return retorno;
  }
  
  
  
  @Override
  public List<DoubleSolution> createInitialSwarm() {
    List<DoubleSolution> swarm = new ArrayList<>(swarmSize);
    
    String nameProblem = problem.getName();
    
    DoubleProblem problem1 = (DoubleProblem) getProblem(nameProblem, 1, metodo);
    DoubleSolution newSolution;
    for (int i = 0; i < swarmSize; i++) {
      newSolution = problem1.createSolution();
      swarm.add(newSolution);
    }

    return swarm;
  }
  
  public List<DoubleSolution> createInitialSwarm1(List<DoubleSolution> swarm) {
	    List<DoubleSolution> swarm1 = new ArrayList<>(swarm.size());
	    
	    DoubleSolution newSolution;
	    for (int i = 0; i < swarm.size(); i++) 
	    {
	    	newSolution = problem.createSolution();
	    	DoubleSolution S = swarm.get(i);
	    	for(int k = 0; k < newSolution.getNumberOfVariables(); k++)
	    	{
	    		newSolution.setVariableValue(k, S.getVariableValue(k));
	    	}
		  
	    	swarm1.add(newSolution);
	    }

	    return swarm1;
	  }

  
  @Override 
  public List<DoubleSolution> createInitialSwarmSurrogate(int variaveis, int solucoes) 
  {
	  
	  Objetivos.add(variaveis);
	  Solucoes.add(solucoes);
	    
	  user userObject = new user(
			    getName(),
			    "none",
			    Solucoes,
			    Objetivos
			);
	  
	  ArrayList SwarmInicio = http(urlInicializa, userObject);
	  
	  List <DoubleSolution> swarm = new ArrayList<>(SwarmInicio.size());
	  DoubleSolution newSolution;
	  ArrayList solution;
	  
	  for(int i = 0; i < SwarmInicio.size(); i++)
	  {
		  newSolution = problem.createSolution();
		  solution =  (ArrayList)SwarmInicio.get(i);
	      for(int k = 0; k < solution.size(); k++)
	      {
	    	  newSolution.setVariableValue(k, (double)solution.get(k));
	      }
		  swarm.add(newSolution);
	      
	  }
	  
	  Objetivos.clear();
	  Solucoes.clear();
	  
	  return swarm;
  }
  
  
  @Override
  public List<DoubleSolution> evaluateSurogateM6(List<DoubleSolution> swarm) 
  {	
	  List<DoubleSolution> swarm1 = new ArrayList<>(swarmSize);
	  DoubleSolution newSolution;
	    for (int i = 0; i < swarmSize; i++) 
	    {
	    	newSolution = problem.createSolution();
	    	DoubleSolution S = swarm.get(i);
	    	for(int k = 0; k < newSolution.getNumberOfVariables(); k++)
	    	{
	    		newSolution.setVariableValue(k, S.getVariableValue(k));
	    	}
		  
	    	swarm1.add(newSolution);
	    }
	    swarm1 = evaluator.evaluate(swarm1, problem);
	    
	  return swarm1;
  }
  
  
  @Override
  public List<DoubleSolution> evaluateSurogateSwarm(List<DoubleSolution> swarm) 
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
		 String nome = getName()+"_"+ classifier +"_"+problema+"_SURROGATE_THETA5_Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
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
  
  
  @Override
  public List<DoubleSolution> evaluateSwarm(List<DoubleSolution> swarm) {
	  
	  List<DoubleSolution> swarmSurrogate = new ArrayList<>(swarm.size());
	  List<DoubleSolution> swarm1 = createInitialSwarm1(swarm);
	  
	  swarm1 = evaluator.evaluate(swarm1, problem);
	  
	  int numObjetives = swarm1.get(0).getNumberOfObjectives();
	  int numVariaveis = swarm1.get(0).getNumberOfVariables();
	  
	  for (int i = 0; i < swarm1.size(); i++)
		 {
			 DoubleSolution solution = swarm1.get(i);
			 double[] xvariaveis = new double[solution.getNumberOfVariables()];
			 //new double[solution.getNumberOfVariables()];
			 for(int k = 0; k < solution.getNumberOfVariables(); k++)
			 {
				 xvariaveis[k] = solution.getVariableValue(k);//solution.getVariableValue(k);
			 }
			 //popul[i] = xvariaveis;
			 double [] xobjetives = new double[solution.getNumberOfObjectives()];
			 for(int o = 0; o < solution.getNumberOfObjectives(); o++)
			 {
				 xobjetives[o] = solution.getObjective(o);
			 }
			 
			 Solucoes.add(xvariaveis);
			 Objetivos.add(xobjetives);
			 //popcons[i][0] = 0;
		 }
	  
	  
	  if(treinou == false)
	  {
		  String ProblemNAme = "C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\ARQUIVOS_POPULACAO\\pop_"+problem.getName()+"_"+Integer.toString(numObjetives)+"OBJ_Execucao"+Integer.toString(execucao)+"";
			user userObject = new user(
					ProblemNAme,
					ProblemNAme,
					Solucoes,
					Objetivos
				);
			ArrayList SwarmInicio = http("http://127.0.0.1:5000/save", userObject);
		  
		  String nameProblem = problem.getName();
		  String referenceParetoFront = "C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\jMetal-master.zip_expanded\\jMetal-master\\jmetal-problem\\src\\test\\resources\\pareto_fronts\\"+nameProblem+"."+Integer.toString(numObjetives)+"D.pf";
		  
		  MWCharArray popName = new MWCharArray();
		  MWCharArray popObjName = new MWCharArray();
		  MWCharArray popConsName = new MWCharArray();
		  MWCharArray fronteira = new MWCharArray();
		  
		  Object[] result = null;
		  KKTP kktp = null;
		  ArrayList lists = new ArrayList<>(swarm.size());
		  try
	      {
			  popName = new MWCharArray("C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\ARQUIVOS_POPULACAO\\pop_"+problem.getName()+"_"+Integer.toString(numObjetives)+"OBJ_Execucao"+Integer.toString(execucao)+".txt");
			  popObjName = new MWCharArray("C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\ARQUIVOS_POPULACAO\\pop_"+problem.getName()+"_"+Integer.toString(numObjetives)+"OBJ_Execucao"+Integer.toString(execucao)+"_Objetivos.txt");
			  
			  popConsName = new MWCharArray("C:\\Users\\Joel Alves\\Desktop\\Exp - Mestrado\\ARQUIVOS_POPULACAO\\pop_"+problem.getName()+"_"+Integer.toString(numObjetives)+"OBJ_Execucao"+Integer.toString(execucao)+"_Cons.txt");
			  
			  fronteira = new MWCharArray(referenceParetoFront);
			  
			  String funcao = problem.getName().toLowerCase();
			  MWCharArray func = new MWCharArray(funcao);
			  
			  String familia = "";
			  if(funcao.startsWith("dtlz"))
				  familia = "dtlz";
			  else if(funcao.startsWith("wfg"))
				  familia ="wfg";
			  
			  MWCharArray family = new MWCharArray(familia);
			  
			  
			  MWNumericArray objetivos = new MWNumericArray(numObjetives, MWClassID.INT16);
			  MWNumericArray variaveis = new MWNumericArray(numVariaveis, MWClassID.INT16);
			  
	         kktp = new KKTP();
	         result = kktp.main(1, func, family, objetivos, variaveis, popName, popObjName, popConsName,fronteira);
	         
	         
	         List<Object> X = Arrays.asList(result[0]);         
	         
	         
	         if (X.get(0) instanceof MWNumericArray) {
	
	             MWNumericArray mw= (MWNumericArray) X.get(0);
	             
	             for(int i = 1; i <= swarm.size(); i++){
	                 
	            	 
	            	 double[] x = new double[1];
	            	 x[0] =	(double) mw.get(i);
	            	 lists.add(x);
	            	 DoubleSolution solution = swarm.get(i-1);
	    			 solution.setObjective(0, x[0]);
	    			 swarmSurrogate.add(solution);
	    			 
	             }
	             //System.out.println("Numero de elementos" + mw.numberOfElements());
	             //System.out.println("Numero de swarm" + swarm.size());
	             //envia Lists e a população para treinar o classificador
	          }
	         swarm = swarmSurrogate;
	         //System.out.println(result[0]);
	         
	         
	         
	      }catch (Exception e)
	      {
	          System.out.println("Exception: " + e.toString());
	       }
	       finally
	       {
	          MWArray.disposeArray(popName);
	          MWArray.disposeArray(popObjName);
	          MWArray.disposeArray(popConsName);
	          
	          MWArray.disposeArray(result);
	          kktp.dispose();
	       }
		  
		  userObject = new user(
				    getName(),
				    "treino",
				    Solucoes,
				    lists
				);
		  
		   SwarmInicio = http(urlTreino, userObject);
		  //envia a populacao para treinar o modelo
	  }
	  else
	  {
		  Objetivos.clear();
		  double[] ob = {1};
		  Objetivos.add(ob);
		  user userObject = new user(
			    getName(),
			    "treino",
			    Solucoes,
			    Objetivos );
		  
		  ArrayList SwarmInicio = http(urlClassifica, userObject);
		  
		  for(int p = 0; p < swarm.size(); p++)
		  {
		    	DoubleSolution solucao = swarm.get(p);
		    	
		    	ArrayList object = (ArrayList) SwarmInicio.get(0);
		    	solucao.setObjective(0, (double)object.get(p));
		    	
		    	swarm.set(p, solucao);
		  }
	  }
	  /*String url = null;
	  if(contErro == 1)
		  url = urlClassifica;
	  else
		  url = urlTreino;
	  
	  if(metodo.equals("base"))
		  url = urlBase;
	  
	 if(contErro == 1)	 
	 {  
		 url = urlClassifica;
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
			 DoubleSolution solution = swarm.get(ii);
			 solution.setObjective(0, objetivo);
			 swarmSurrogate.add(solution);
		 }
		 swarm = swarmSurrogate;
		 Objetivos.clear();
		 Solucoes.clear();
	 }
	 
	  if (Objetivos.size()>nTreino)
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
		  parada += swarm.size();
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
			  String nome = getName()+"_"+ classifier +"_"+problema+"_SURROGATE_THETA5_Populacao100_NumeroInteracoes"+maxIterations+"_NumeroObjetivos01_Execucao_0"+execucao+"";
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
	  }*/
	  
	  Objetivos.clear();
	  Solucoes.clear();
	 
	  return swarm;
  }

 
  
  @Override
  protected double avaliation(List<DoubleSolution> swarm)
  {
	  return  swarm.get(0).getObjective(0);
  }
  
  @Override
  protected void initializeLeader(List<DoubleSolution> swarm) {
    for (DoubleSolution particle : swarm) {
      leaders.add(particle);
    }
  }

  @Override
  protected void initializeVelocity(List<DoubleSolution> swarm) {
    for (int i = 0; i < swarm.size(); i++) {
      for (int j = 0; j < problem.getNumberOfVariables(); j++) {
        speed[i][j] = 0.0;
      }
    }
  }

  @Override
  protected void initializeParticlesMemory(List<DoubleSolution> swarm) {
    for (DoubleSolution particle : swarm) {
      localBest.setAttribute(particle, (DoubleSolution) particle.copy());
    }
  }

  @Override
  protected void updateVelocity(List<DoubleSolution> swarm) {
    double r1, r2, c1, c2;
    double wmax, wmin;
    DoubleSolution bestGlobal;

    for (int i = 0; i < swarm.size(); i++) {
      DoubleSolution particle = (DoubleSolution) swarm.get(i).copy();
      DoubleSolution bestParticle = (DoubleSolution) localBest.getAttribute(swarm.get(i)).copy();

      bestGlobal = selectGlobalBest();

      r1 = randomGenerator.nextDouble(r1Min, r1Max);
      r2 = randomGenerator.nextDouble(r2Min, r2Max);
      c1 = randomGenerator.nextDouble(c1Min, c1Max);
      c2 = randomGenerator.nextDouble(c2Min, c2Max);
      wmax = weightMax;
      wmin = weightMin;

      for (int var = 0; var < particle.getNumberOfVariables(); var++) {
        speed[i][var] = velocityConstriction(constrictionCoefficient(c1, c2) * (
                        inertiaWeight(iterations, maxIterations, wmax, wmin) * speed[i][var] +
                                c1 * r1 * (bestParticle.getVariableValue(var) - particle.getVariableValue(var)) +
                                c2 * r2 * (bestGlobal.getVariableValue(var) - particle.getVariableValue(var))),
                deltaMax, deltaMin, var);
      }
    }
  }

  @Override
  protected void updatePosition(List<DoubleSolution> swarm) {
    for (int i = 0; i < swarmSize; i++) {
      DoubleSolution particle = swarm.get(i);
      for (int j = 0; j < particle.getNumberOfVariables(); j++) {
        particle.setVariableValue(j, particle.getVariableValue(j) + speed[i][j]);

        if (particle.getVariableValue(j) < problem.getLowerBound(j)) {
          particle.setVariableValue(j, problem.getLowerBound(j));
          speed[i][j] = speed[i][j] * changeVelocity1;
        }
        if (particle.getVariableValue(j) > problem.getUpperBound(j)) {
          particle.setVariableValue(j, problem.getUpperBound(j));
          speed[i][j] = speed[i][j] * changeVelocity2;
        }
      }
    }
  }

  
  @Override
  protected List<DoubleSolution> perturbationMutax(List<DoubleSolution> swarm1) {
    for (int i = 0; i < swarm1.size(); i++) {
      if ((i % 1) == 0) {
        mutation.execute(swarm1.get(i));
      }
    }
    return swarm1;
  }
  
  @Override
  protected void perturbation(List<DoubleSolution> swarm) {
    for (int i = 0; i < swarm.size(); i++) {
      if ((i % 6) == 0) {
        mutation.execute(swarm.get(i));
      }
    }
  }

  @Override
  public void updateLeaders(List<DoubleSolution> swarm) {
    for (DoubleSolution particle : swarm) {
      leaders.add((DoubleSolution) particle.copy());
    }
  }

  @Override
  public void updateParticlesMemory(List<DoubleSolution> swarm) {
    for (int i = 0; i < swarm.size(); i++) {
      int flag = dominanceComparator.compare(swarm.get(i), localBest.getAttribute(swarm.get(i)));
      if (flag != 1) {
        DoubleSolution particle = (DoubleSolution) swarm.get(i).copy();
        localBest.setAttribute(swarm.get(i), particle);
      }
    }
  }

  @Override
  public List<DoubleSolution> getResult() {
    return leaders.getSolutionList();
  }
  @Override
  public List<DoubleSolution> getResultado() {
    return swarm;//leaders.getSolutionList();
  }

      
  @Override
  public List<DoubleSolution> getLideres(List<DoubleSolution> swarm) {
	  List<DoubleSolution> lederes = new ArrayList<>();
	  List<DoubleSolution> Listaux = swarm; 
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
  
  public DoubleSolution selectGlobalBest() {
    DoubleSolution one, two;
    DoubleSolution bestGlobal;
    int pos1 = randomGenerator.nextInt(0, leaders.getSolutionList().size() - 1);
    int pos2 = randomGenerator.nextInt(0, leaders.getSolutionList().size() - 1);
    one = leaders.getSolutionList().get(pos1);
    two = leaders.getSolutionList().get(pos2);

    if (leaders.getComparator().compare(one, two) < 1) {
      bestGlobal = (DoubleSolution) one.copy();
    } else {
      bestGlobal = (DoubleSolution) two.copy();
    }

    return bestGlobal;
  }

  private double velocityConstriction(double v, double[] deltaMax, double[] deltaMin,
                                      int variableIndex) {
    double result;

    double dmax = deltaMax[variableIndex];
    double dmin = deltaMin[variableIndex];

    result = v;

    if (v > dmax) {
      result = dmax;
    }

    if (v < dmin) {
      result = dmin;
    }

    return result;
  }

  protected double constrictionCoefficient(double c1, double c2) {
    double rho = c1 + c2;
    if (rho <= 4) {
      return 1.0;
    } else {
      return 2 / (2 - rho - Math.sqrt(Math.pow(rho, 2.0) - 4.0 * rho));
    }
  }

  private double inertiaWeight(int iter, int miter, double wma, double wmin) {
    return wma;
  }

  @Override
  public String getName() {
    return "SMPSO";
  }

  @Override
  public String getDescription() {
    return "Speed contrained Multiobjective PSO";
  }

  /* Getters */
  public int getSwarmSize() {
    return swarmSize;
  }

  public int getMaxIterations() {
    return maxIterations;
  }

  public int getIterations() {
    return iterations;
  }

  /* Setters */
  public void setIterations(int iterations) {
    this.iterations = iterations;
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
  
  public static Problem<DoubleSolution> getProblem(String prob, int nObj, String metodo)
  {
	  int k = -1;
	  /*if(nObj == 3)
		  k = 4;
	  else if(nObj == 10)
		  k = 9;
	  */
	  if(prob.startsWith("WFG"))
	  {
		  if(metodo.equals("3"))
			  k = 4;
		  else if(metodo.equals("10"))
			  k = 9;
	  }
	  if(prob.startsWith("DTLZ"))
	  {
		  if(metodo.equals("10"))
			  k = 19;
		  else
			  k = 12;
	  }
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
		  nomeP = new WFG2(k,10,nObj);
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
  
  

@Override
protected void initProgress() throws Throwable {
	// TODO Auto-generated method stub
	
}
  
  
}
