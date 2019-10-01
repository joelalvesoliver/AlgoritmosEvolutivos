#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Aug 25 11:00:25 2018

@author: joel
"""


from sklearn.externals import joblib
from sklearn.svm import SVR
from sklearn.ensemble import RandomForestRegressor
from sklearn.tree import DecisionTreeRegressor
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import r2_score
from sklearn.metrics import mean_squared_error
#from sklearn.cross_validation import cross_val_score
from sklearn.model_selection import GridSearchCV



import numpy as np
from numpy.core.umath_tests import inner1d
from sklearn.metrics import accuracy_score


#classifierSVM = SVR(kernel='rbf', C=1e3, gamma=0.1, tol=0.01)
#classifierMLP = MLPRegressor(hidden_layer_sizes=(10,),max_iter=1000)
# #                                      activation='relu',
# #                                      solver='adam',
#  #                                     learning_rate='adaptive',
#   #                                    max_iter=10000,
#    #                                   learning_rate_init=0.01,
#     #                                  alpha=0.01)
classifierRF = RandomForestRegressor(n_estimators=1000, max_depth=None,min_samples_split=2, random_state=0,criterion='mse');
#classifierTree = DecisionTreeRegressor(max_depth=1000,min_samples_split=2, random_state=0,criterion='mse')



ErroTreino = []; predict = 0

Target = np.load('Saida.npy')
Entradas = np.load('Entrada.npy')


X_train = Entradas[0:25000,:]
y_train = Target[0:25000]

X_test = Entradas[25000:25000+100000,:]
y_test = Target[25000:25000+100000]

#for i in range(100,600000,100):
#    
#    if(predict!=0):
#        predictedTrain = classifierRF.predict(X_train[i-100:i,:])
#        ErroTreino.append(r2_score(y_train[i-100:i], predictedTrain))
#        #ErroTreino.append(mean_squared_error(y_train[i-100:i], predictedTrain))
#        
#    classifierRF.fit(X_train[i-100:i,:], y_train[i-100:i])
#    predict = 1
#    print(f'Treinando: {i}')

classifierRF.fit(X_train, y_train)
predictedTrain = classifierRF.predict(X_train)
predicted = classifierRF.predict(X_test)

#ErroTreino = np.asarray(ErroTreino)

#accuracyTrain = np.mean(ErroTreino)
#std = np.std(ErroTreino)

#accuracy = mean_squared_error(y_test, predicted)
r2Train = r2_score(y_train, predictedTrain)
r2 = r2_score(y_test, predicted)

arquivo = open("classifierRF25kR2.txt",'w')
arquivo.writelines("ErroMedioTreino, ErroMedioTeste \n")
#arquivo.writelines(str(r2Train) + " STD: "+str(std)+" , " + str(r2) +"\n")
arquivo.writelines(str(r2Train) + ", " + str(r2) +"\n")
arquivo.close()

