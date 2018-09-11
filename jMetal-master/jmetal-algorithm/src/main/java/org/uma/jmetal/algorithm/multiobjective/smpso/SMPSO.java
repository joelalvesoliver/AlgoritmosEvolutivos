package org.uma.jmetal.algorithm.multiobjective.smpso;

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
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.user;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.retorno;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
  
  private String urlTreino = "http://127.0.0.1:5000/treinamento";
  private String urlClassifica = "http://127.0.0.1:5000/classifica";
  private String urlSalva = "http://127.0.0.1:5000/save";
  private String urlInicializa = "http://127.0.0.1:5000/inicializa";
  private ArrayList Solucoes = new ArrayList();
  private ArrayList Objetivos = new ArrayList();
  //private int contErro = 0; 


  
  
  /**
   * Constructor
   */
  public SMPSO(DoubleProblem problem, int swarmSize, BoundedArchive<DoubleSolution> leaders,
               MutationOperator<DoubleSolution> mutationOperator, int maxIterations, double r1Min, double r1Max,
               double r2Min, double r2Max, double c1Min, double c1Max, double c2Min, double c2Max,
               double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
               SolutionListEvaluator<DoubleSolution> evaluator) {
    this.problem = problem;
    this.swarmSize = swarmSize;
    this.leaders = leaders;
    this.mutation = mutationOperator;
    this.maxIterations = maxIterations;

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
  protected void initProgress() {
    iterations = 1;
    updateLeadersDensityEstimator();
  }

  @Override
  protected void updateProgress() {
    iterations += 1;
    updateLeadersDensityEstimator();
  }

  @Override
  protected boolean isStoppingConditionReached() {
    
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
    
    DoubleSolution newSolution;
    for (int i = 0; i < swarmSize; i++) {
      newSolution = problem.createSolution();
      swarm.add(newSolution);
    }

    return swarm;
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
  public List<DoubleSolution> evaluateSurogateSwarm(List<DoubleSolution> swarm)
  {	 
	 List<DoubleSolution> swarmSurrogate = new ArrayList<>(swarm.size());
	 
	 
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
	 
	 ArrayList SwarmAvalia = http(urlTreino, userObject);
	 
	 for (int ii = 0; ii < SwarmAvalia.size(); ii++)
	 {
		 double objetivo = (double) SwarmAvalia.get(ii);
		 DoubleSolution solution = swarm.get(ii);
		 solution.setObjective(0, objetivo);
		 swarmSurrogate.add(solution);
	 }
	 
	 Objetivos.clear();
	 Solucoes.clear();
	 return swarmSurrogate;  
  }
  
  
  @Override
  public List<DoubleSolution> evaluateSwarm(List<DoubleSolution> swarm) {
	  
	  swarm = evaluator.evaluate(swarm, problem);
	  
	  for (int i = 0; i < swarm.size(); i++)
		 {
			 DoubleSolution solution = swarm.get(i);
			 double[] xvariaveis = new double[solution.getNumberOfVariables()];
			 for(int k = 0; k < solution.getNumberOfVariables(); k++)
			 {
				 xvariaveis[k] = solution.getVariableValue(k);
			 }
			 Solucoes.add(xvariaveis);
			 Objetivos.add(solution.getObjective(0));
		 }
	  
	  user userObject = new user(
			    getName(),
			    "treino",
			    Solucoes,
			    Objetivos
			);
	 
	 ArrayList SwarmAvalia = http(urlTreino, userObject);
	 
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
