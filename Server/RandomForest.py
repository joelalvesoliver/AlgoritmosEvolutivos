# -*- coding: utf-8 -*-
"""
Created on Fri Jul 27 13:54:53 2018

@author: joeli
"""
from sklearn.grid_search import GridSearchCV
#from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.externals import joblib

def Cria_ClassificadorRF():

    pipe_svc = Pipeline([('scl', StandardScaler()),
            ('clf',RandomForestClassifier(random_state=0))])

    
    return pipe_svc



def Treina_classificadorRF(classifier, X_train, y_train):
    
    classifier = classifier.fit(X_train, y_train)
    return classifier


def predict_classifierRF(classifier, X_test):
    y_pred = classifier.predict(X_test)
    return y_pred


def load_classifierRF(name):
    classifier = joblib.load(name)
    return classifier
    
    
def save_classifierRF(classifier,name):
    #name tem a extensão .pkl
    joblib.dump(classifier, name)