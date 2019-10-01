#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Aug 14 21:24:26 2018

@author: joel
"""

from flask import Flask, request, jsonify
from servidor.Serialize.StructRetorno import Retorno
from servidor.Serialize.Seriabilize import GenericJsonEncoder 
from servidor.Serialize.RetornoGoogleCloud import ReturnGoogleCloud
from unicodedata import normalize

from sklearn.externals import joblib
from sklearn.svm import SVR
from sklearn.ensemble import RandomForestRegressor
from sklearn.tree import DecisionTreeRegressor
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import mean_squared_error
#from sklearn.cross_validation import cross_val_score
from sklearn.model_selection import GridSearchCV



import numpy as np
from numpy.core.umath_tests import inner1d
from pyDOE import LKS

import json

app = Flask(__name__)

#variaveis globais
dados = []
txErroSVM = [];
txErroMLP = [];
txErroRF = [];
txErroTREE =[];
execult = [];

comparaSVM=[];
comparaMLP=[];
comparaRF=[];
comparaTRee=[];

real = []
predito = []

ler = 0;
tamanho = 0;
chamada = 0;
#
#classifierSVM = SVR(kernel='rbf', C=1e3, gamma=0.1, tol=0.01)
#classifierMLP = MLPRegressor(hidden_layer_sizes=(10,),max_iter=1000)
# #                                      activation='relu',
# #                                      solver='adam',
#  #                                     learning_rate='adaptive',
#   #                                    max_iter=10000,
#    #                                   learning_rate_init=0.01,
#     #                                  alpha=0.01)
#classifierRF = RandomForestRegressor(n_estimators=1000, max_depth=None,min_samples_split=2, random_state=0,criterion='mse');
classifierTree = DecisionTreeRegressor(max_depth=1000,min_samples_split=2, random_state=0,criterion='mse')

classifier = classifierTree;



def Leitura(name):
	arquivo = open(name,'r')
	linhas = arquivo.readlines()
	retorno = []

	for linha in linhas:
		retorno.append(eval(linha.replace("[","").replace("]","").replce(" ",",")))
	arquivo.close()

	return retorno


def Save(lista, name):
    arquivo = open(name,'w')
    i = 0
    while i < len(lista):    
        arquivo.writelines((lista[i]))
        i += 1
    arquivo.close()

def EscreveArquivo(indice, lista, name):
    arquivo = open(name,'w')
    i = 0
    while i < len(lista):    
        arquivo.write(str(indice[i]) +" "+ str(lista[i])+'\n')
        i += 1
    arquivo.close()

classifier
def EscreveArquivoC(indice, compara ,lista, name):
    arquivo = open(name,'w')
    i = 0
    arquivo.write("Indice" +" "+ "Predição"+" "+"Rotulo"+" "+"Erro"+'\n')
    while i < len(lista):
        arquivo.write(str(indice[i]) +" "+str(compara[i])+" "+ str(lista[i])+'\n')
        i += 1
    arquivo.close()



@app.route("/treinamento", methods=['GET', 'POST'])
def treino():

    message = request.get_json(silent=True)
    
    #algoritmo = message["algoritmo"] 
    
    retorno = np.array([0.08347,0.942,0.09403])
    
    variaveisIniciais = LKS.lhs(2, samples=100)
    nSolution = np.asarray(message["solucoes"])
    nObj = np.asarray(message["objetivos"])
    processar = message["save"]
    erro =message["erro"]
    
    if chamada == 0:classifier
        Entrada = np.asarray(nSolution)
        Saida = np.asarray(nObj)
        chamada = 1
        
    if processar == "save":
        print("Salvou")
        
        #Target = np.load('Saida.npy')
        #Entradas = np.load('Entrada.npy')
        
        
        #EscreveArquivoC(execult, txErroSVM,comparaSVM,'txErroSVMRregreBATCH100.txt')
        #EscreveArquivoC(execult, txErroMLP, comparaMLP,'txErroMLPRregreBATCH25k.txt')
        EscreveArquivo(execult, real,'Real.txt')
        EscreveArquivo(execult, predito,'Predito.txt')
        #joblib.dump(classifierMLP,"MLPBatch100.pkl")
        #joblib.dump(classifierRF,"RFBatch100.pkl")
        #joblib.dump(classifierSVM,"SVMBatch100k.pkl")
        joblib.dump(classifierTree, "TREEBatch25K.pkl")
    
    else:
        
        #print(message)
        if erro=="calcule": 
        
            #if erro=="calcule"
            #tamanho = tamanho + len(nSolution)
            #execult.append(tamanho)
            #y_predictSVM = classifierSVM.predict(nSolution) 
            #y_predictRF = classifierRF.predict(nSolution)
            #y_predictMLP = classifierMLP.predict(nSolution)
            y_predictTree = classifierTree.predict(nSolution)
            """O tipo de retorno depois de fazer o predict deve ser float64, que é o tipo vindo do jMetal
            """
            retorno = y_predictTree
            cont = 0;
            for element in retorno:
                real.append(nObj[cont])
                predito.append(retorno[cont])
                execult.append(tamanho)
                tamanho += 1
                cont+=1
            
            #comparaSVM.append([nObj, y_predictSVM]);
            #comparaMLP.append([nObj, y_predictMLP]);
            #comparaRF.append([nObj, y_predictRF]);
            #comparaTRee.append([nObj, y_predictTree]);
            
            #txErroSVM.append(mean_squared_error(np.asarray(nObj),np.asarray(y_predictSVM)))
            #txErroSVM.append(1.0 - classifierSVM.score(np.asarray(nSolution), np.asarray(nObj)))
            #txErroRF.append(mean_squared_error(np.asarray(nObj), np.asarray(y_predictRF)))
            #txErroRF.append(1.0 - classifierRF.score(np.asarray(nSolution), np.asarray(nObj)))
            #txErroMLP.append(mean_squared_error(np.asarray(nObj),y_predictMLP))
            #txErroMLP.append(1.0 - classifierMLP.score(np.asarray(nSolution), np.asarray(nObj)))
            #txErroTREE.append(mean_squared_error(np.asarray(nObj),y_predictTree))
            #txErroTREE.append(1.0 - classifierTree.score(np.asarray(nSolution), np.asarray(nObj)))
            
            
            #classifierSVM = classifierSVM.fit(np.asarray(nSolution), np.asarray(nObj))
            #classifierRF = classifierRF.fit(np.asarray(nSolution),np.asarray(nObj))
            #classifierMLP = classifierMLP.fit(np.asarray(nSolution),np.asarray(nObj))
            #classifierTree = classifierTree.fit(np.asarray(nSolution),np.asarray(nObj));
            
        if erro != "calcule":
            #print("Treinando")
            #classifierSVM = classifierSVM.fit(np.asarray(nSolution), np.asarray(nObj))
            #print("Treinou SVM")
            #classifierRF = classifierRF.fit(np.asarray(nSolution),np.asarray(nObj))
            #print("Treinou RF")
            #classifierMLP = classifierMLP.fit(np.asarray(nSolution),np.asarray(nObj))
            #print("Treinou MLP")
            classifierTree = classifierTree.fit(nSolution,np.asarray(nObj));

        	
			
    return json.dumps({"retorno": retorno.tolist()})



#Inicializa usando LHS
@app.route("/inicializa", methods=['GET', 'POST'])
def Inicializa():
    
    message = request.get_json(silent=True)
    
    nSolution = np.asarray(message["solucoes"]) #passa o numero de exemplos na posicao 0
    nObj = np.asarray(message["objetivos"]) #passa o numero de variaveis na posicao 0
    
    retorno = lhs(nObj[0], samples=nSolution[0])
    retorno = np.asarray(retorno)



    return json.dumps({"retorno": retorno.tolis()}