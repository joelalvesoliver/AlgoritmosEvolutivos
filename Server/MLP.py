# -*- coding: utf-8 -*-
"""
Created on Fri Jul 27 13:47:32 2018

@author: joeli
"""
from sklearn.neural_network import MLPClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.externals import joblib

def Cria_ClassificadorMLP():

    classifier = Pipeline([('scl', StandardScaler()), 
            ('clf',MLPClassifier(random_state=1, max_iter=1000, solver='lbfgs',
                                 activation='relu', alpha=0.1,
                                 hidden_layer_sizes=(2)))])
    return classifier



def Treina_classificadorMLP(classifier, X_train, y_train):
    
    classifier = classifier.fit(X_train, y_train)
    
    return classifier


def predict_classifierMLP(classifier, X_test):
    y_pred = classifier.predict(X_test)
    return y_pred


def load_classifierMLP(name):
    classifier = joblib.load(name)
    return classifier
    
    
def save_classifierMLP(classifier,name):
    #name tem a extensão .pkl
    joblib.dump(classifier, name)