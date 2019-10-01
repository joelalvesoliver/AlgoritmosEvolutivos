package org.uma.jmetal.algorithm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import org.uma.jmetal.algorithm.retorno;
import org.uma.jmetal.algorithm.user;
import org.uma.jmetal.solution.DoubleSolution;

/**
 * Abstract class representing a PSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractParticleSwarmOptimization<S, Result> implements Algorithm <Result> {
  protected List<S> swarm;
  private List<S> Pt;
  private List<S> Pk;
  private List<S> Pk1;
  private List<S> swarm1;
  public List<S> getSwarm() {
    return swarm;
  }
  
  private int execucao = 0;
  public void setSwarm(List<S> swarm) {
    this.swarm = swarm;
  }

  protected abstract void initProgress() throws Throwable ;
  protected abstract void updateProgress() ;
  protected abstract boolean isStopping();
  protected abstract boolean isStoppingConditionReached() ;
  protected abstract List<S> createInitialSwarm();
  protected abstract List<S> createInitialSwarm1(List<S> swarm);
  protected abstract List<S> createInitialSwarmSurrogate(int variaveis, int solucoes);
  protected abstract List<S> evaluateSurogateM6(List<S> swarm);
  protected abstract List<S> evaluateSurogateSwarm(List<S> swarm);
  protected abstract double avaliation(List<S> swarm);
  protected abstract List<S> evaluateSwarm(List<S> swarm) ;
  protected abstract void initializeLeader(List<S> swarm) ;
  protected abstract void initializeParticlesMemory(List<S> swarm) ;
  protected abstract void initializeVelocity(List<S> swarm) ;
  protected abstract void updateVelocity(List<S> swarm) ;
  protected abstract void updatePosition(List<S> swarm) ;
  protected abstract void perturbation(List<S> swarm) ;
  protected abstract void updateLeaders(List<S> swarm);
  protected abstract void updateParticlesMemory(List<S> swarm);
  protected abstract List<S> Uniao(List<S> swarm, List<S> swarm1);
  protected abstract  List<S> getLideres(List<S> swarm);
  protected abstract List<S> perturbationMutax(List<S> swarm);

  
  
  private String urlTreino = "http://127.0.0.1:5000/treinamento";
  private String urlClassifica = "http://127.0.0.1:5000/classifica";
  private String urlSalva = "http://127.0.0.1:5000/save";
  private String urlInicializa = "http://127.0.0.1:5000/inicializa";

  private ArrayList Solucoes = new ArrayList();
  private ArrayList Objetivos = new ArrayList();

  public abstract List<DoubleSolution> getResultado();
  @Override
  public abstract Result getResult() ;

  @Override
  public void run() {
	
    swarm = createInitialSwarm() ;
    
    swarm = evaluateSwarm(swarm);
    initializeVelocity(swarm);
    initializeParticlesMemory(swarm) ;
    initializeLeader(swarm);
    try {
		initProgress();
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    while (!isStoppingConditionReached()) {
      updateVelocity(swarm);
      updatePosition(swarm);
      perturbation(swarm);
      swarm = evaluateSwarm(swarm);
      updateLeaders(swarm);
      updateParticlesMemory(swarm);
      updateProgress();
    }
    
    
	  //M1();
  }
  
  public List<S> EMO(List<S> swarm1) throws Throwable
  {
	  List<S> swarm = swarm1;
	  
	  swarm = evaluateSurogateSwarm(swarm);

	  initializeVelocity(swarm);	  
	  initializeParticlesMemory(swarm) ;
	  initializeLeader(swarm) ;
	  initProgress();
	  
	  while (!isStoppingConditionReached()) {
	      updateVelocity(swarm);
	      updatePosition(swarm);
	      perturbation(swarm);
	      
	      swarm = evaluateSurogateSwarm(swarm) ;
	      
	      updateLeaders(swarm);
	      updateParticlesMemory(swarm);
	      updateProgress();
	    }
	  
	  return swarm;
  }
  
  
  
  
  public void M1() throws Throwable
  {	
	  int teta = 5;
	  int t = 0;
	  
	  int k = t%teta;
	  // inicializa as populacoes 
	  Pk = createInitialSwarmSurrogate(20,100);
	   
	  
	  double eval = 1000;
	  
	  while (!isStopping())
	  {
		  //treina o surrogate
		  if (t % teta == 0)
		  {
			  
			  Pk1 = Uniao(Pk, Pt);
			  
			  // Avalia com a Funcao Real
			  // Treina
			  evaluateSwarm(Pk1);
			  if(isStopping())
				  break;
			  
			  //atualiza o eval eval = eval 
			  
			  if (k==0) 
			  {
				  Pt = createInitialSwarmSurrogate(20,100);
				  Pt = evaluateSwarm(Pt);
				  if(isStopping())
					  break;
			  } 
			  else 
			  {
				  updateLeaders(Pk1);
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
  
  
  // metodo uniao deve ser na classe SMPSO
  /*public List<S> Uniao(List<S> Psurrogate, List<S>Pemo)
  {		
	  List<S> retorno = Psurrogate;
	  
	  if(Pemo != null)
	  {
		  for(int i = 0; i< Pemo.size(); i++)
		  {	
			  retorno.add(Pemo.get(i));
		  }
	  }
	  return retorno; 
  }*/
  
  
  
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
