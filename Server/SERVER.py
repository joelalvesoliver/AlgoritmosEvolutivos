#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Aug 14 21:24:26 2018

@author: joel
"""

from flask import Flask, request, jsonify

from sklearn.externals import joblib
from sklearn.svm import SVR
from sklearn.ensemble import RandomForestRegressor
from sklearn.tree import DecisionTreeRegressor
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import mean_squared_error
#from sklearn.cross_validation import cross_val_score
from sklearn.model_selection import GridSearchCV



import numpy as np
#from pyDOE import lhs

import json

app = Flask(__name__)

#variaveis globais


"""
######################## INICIALIZA OS CLASSIFICADORES ##################################################################
"""
#classifierSVM = SVR(kernel='rbf', C=1e3, gamma=0.1, tol=0.01)
#classifierMLP = MLPRegressor(hidden_layer_sizes=(10,),max_iter=1000)
# #                                      activation='relu',
# #                                      solver='adam',
#  #                                     learning_rate='adaptive',
#   #                                    max_iter=10000,
#    #                                   learning_rate_init=0.01,
#     #                                  alpha=0.01)
#classifierRF = RandomForestRegressor(n_estimators=200, max_depth=None,min_samples_split=2, random_state=0,criterion='mse');
#classifierTree = DecisionTreeRegressor(max_depth=500,min_samples_split=2, random_state=0,criterion='mse')


"""
########################## VARIAVEIS GLOBAIS ############################################################################
"""
execult = [];
tamanho = 0;
txErro = [];
real = [];
treinou = False
#classifierInit = list();

classifier = list();
igd = list();

"""
######################################## FUNÇÕES PARA SALVAR OS VALORES ##################################################
"""

def Leitura(name):
	arquivo = open(name,'r')
	linhas = arquivo.readlines()
	retorno = []

	for linha in linhas:
		retorno.append(eval(linha.replace("[","").replace("]","").replce(" ",",")))
	arquivo.close()

	return retorno


def Save(lista, name):
    import numpy as np
#    arquivo = open(name,'w')
#    i = 0
#    while i < len(lista):    
#        arquivo.writelines((lista[i]))
#        i += 1
#    arquivo.close()
    np.savetxt(name, lista)

def LeituraIGD(name):
    arquivo = open(name,'r')
    linhas = arquivo.readlines()
    retorno = []
    
    for linha in linhas:
        retorno.append(float(linha))
    
    return retorno

def SaveIGD(lista, name):
    import numpy as np
    arquivo = open(name,'w')
    i = 0
    media = 0;
    while i < len(lista):
        media += lista[i]
        arquivo.write(str(lista[i])+'\n')
        i += 1
    media = media/len(lista)
    std = np.std(np.asarray(lista));
    arquivo.write(str(media)+ "+/-" + str(std) + '\n')
    arquivo.close()


def EscreveArquivo(indice, lista, name):
    arquivo = open(name,'w')
    i = 0
    while i < len(lista):    
        arquivo.write(str(indice[i]) +" "+ str(lista[i])+'\n')
        i += 1
    arquivo.close()


def EscreveArquivoC(indice, compara ,lista, name):
    arquivo = open(name,'w')
    i = 0
    arquivo.write("Indice" +" "+ "Predição"+" "+"Rotulo"+" "+"Erro"+'\n')
    while i < len(lista):
        arquivo.write(str(indice[i]) +" "+str(compara[i])+" "+ str(lista[i])+'\n')
        i += 1
    arquivo.close()




@app.route("/classificador", methods=['GET', 'POST'])
def classificador():
    global classifier
    global txErro
    global real 
    #global classifierInit
    
    classifierInit = list();
    message = request.get_json(silent=True)
    #nSolution = np.asarray(message["solucoes"]) #passa o numero de exemplos na posicao 0
    #nObj = np.asarray(message["objetivos"]) #passa o numero de variaveis na posicao 0
    processar = message["processar"]
    nObj = np.asarray(message["objetivos"])
    
    print(processar)
    if processar == "SVM":
        classifierInit = SVR(kernel='rbf', C=1e3, gamma=0.1, tol=0.01)
    elif processar == "TREE":
        for i in range(nObj[0]):
            classifierInit.append(DecisionTreeRegressor(max_depth=500,min_samples_split=2, random_state=0,criterion='mse'))
            
    elif processar == "RAMDOMFOREST":
        for i in range(nObj[0]):
            classifierInit.append(RandomForestRegressor(n_estimators=200, max_depth=None,min_samples_split=2, random_state=0,criterion='mse'))  
    elif processar == "MLP":
        classifierInit = MLPRegressor(hidden_layer_sizes=(10,),max_iter=1000)

    classifier = classifierInit
    
    return json.dumps({"retorno": []})

"""
##################################################### TREINAMENTO ########################################################
"""
@app.route("/treinamento", methods=['GET', 'POST'])
def treino():
    global classifier
    global txErro
    global real
    global tamanho
    global execult
    global treinou
    
    lista = []
    message = request.get_json(silent=True)
    nSolution = np.asarray(message["solucoes"])
    nObj = np.asarray(message["objetivos"])
    #print(f'O tamanho do treino: {len(nObj)}')
    #processar = message["processar"]
    #erro = message["erro"]  
    
    nObj = nObj.transpose()
    
    for i in range(len(nObj)):
        classifier[i] = classifier[i].fit(nSolution, nObj[i]);
	    
    #treinou = True
    
    #versão Batch
    #classifier = classifier.fit(nSolution, nObj);
    #y_predict = classifier.predict(nSolution)
    
    #i = 0
    #for valor in y_predict:
    #    real.append(nObj[i])
    #    txErro.append(y_predict[i])
    #    i+=1
    
    return json.dumps({"retorno": lista})




"""
######################################################### SALVAR #########################################################
"""
@app.route("/save", methods=['GET', 'POST'])
def save():
    global classifier
    global txErro
    global real
    global tamanho
    global execult
    global treinou
    global classifierInit
    
    message = request.get_json(silent=True)
    nome = message["algoritmo"]
    nSolution = np.asarray(message["solucoes"])
    nObj = np.asarray(message["objetivos"])
    #salva o erro e o classificador
    #joblib.dump(classifier, "TREE.pkl")
    if real:    
        SaveIGD(nSolution.tolist(), nome+".txt")
    
    
    Save(nSolution.tolist(), nome+".txt")
    Save(nObj.tolist(), nome+"_Objetivos.txt")
    Save(np.zeros((len(nObj), 1)).tolist(), nome+"_Cons.txt")
#    if txErro:
#        Save(txErro,"DadosPreditos_"+nome+".txt")
#    
#    real = []
#    txErro = []
#    treinou = False
#    classifier = classifierInit
    
    return json.dumps({"retorno": []})

"""
#################################### CLASSIFICA AS SOLUCOES ###############################################################
"""

@app.route("/classifica", methods=['GET', 'POST'])
def classifica():
    global classifier
    global txErro
    global real 
    
    message = request.get_json(silent=True)
    nSolution = np.asarray(message["solucoes"]) #passa o numero de exemplos na posicao 0
    nObj = np.asarray(message["objetivos"]) #passa o numero de variaveis na posicao 0
    
    #classifier = joblib.load("TREE30k.pkl") 
    y_predict = list()

    nObj = nObj.transpose()
    for i in range(len(nObj)):
        y_predict.append(classifier[i].predict(nSolution))

    y_predict = np.asarray(y_predict);
#    i = 0
#    for valor in y_predict:
#        real.append(nObj[i])
#        txErro.append(y_predict[i])
#        i+=1


    return json.dumps({"retorno": y_predict.tolist()})


"""
############################## INICIALIZA AS VARIAVEIS COM A FUNCAO LHS ####################################################
"""
@app.route("/inicializa", methods=['GET', 'POST'])
def Inicializa():
    
    message = request.get_json(silent=True)
    
    nSolution = np.asarray(message["solucoes"]) #passa o numero de exemplos na posicao 0
    nObj = np.asarray(message["objetivos"]) #passa o numero de variaveis na posicao 0
    print(f'O tamanho do treino: {nObj, nSolution}')
    retorno = lhs(nObj[0], samples=nSolution[0])
    retorno = np.asarray(retorno)


    return json.dumps({"retorno": retorno.tolist()})



@app.route("/saveIGD", methods=['GET', 'POST'])
def saveIGD():
    global igd
    message = request.get_json(silent=True)
    nome = message["algoritmo"]    
    
    SaveIGD(igd, nome+".txt")
    
    igd = list()
    
    return json.dumps({"retorno": []})

@app.route("/IGD", methods=['GET', 'POST'])
def IGD():
    global igd
    
    message = request.get_json(silent=True)
    nObj = np.asarray(message["objetivos"])
    
    igd.append(nObj[0])
    
    return json.dumps({"retorno": []})

@app.route("/guarda", methods=['GET', 'POST'])
def Base():
    global real

    
    message = request.get_json(silent=True)
    
    nSolution = np.asarray(message["solucoes"]) #passa o numero de exemplos na posicao 0
    nObj = np.asarray(message["objetivos"]) #passa o numero de variaveis na posicao 0
    
    i = 0;
    for valor in nObj:
        real.append(nObj[i])
        i+=1
    

    return json.dumps({"retorno": []})


