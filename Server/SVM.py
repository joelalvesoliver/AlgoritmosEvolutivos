# -*- coding: utf-8 -*-
"""
Created on Fri Jul 27 11:43:06 2018

@author: joeli
"""

from sklearn.svm import SVC
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
#from sklearn.cross_validation import train_test_split
from sklearn.externals import joblib
#import numpy as np


def Cria_ClassificadorSVM():

    classifier = Pipeline([('scl', StandardScaler()),
        ('clf', SVC(random_state=1, kernel='rbf', C=1000000, 
                    degree=2, gamma='auto',
                    decision_function_shape='ovo'))])
    return classifier



def Treina_classificadorSVM(classifier, X_train, y_train):
    
    classifier = classifier.fit(X_train, y_train)
    
    return classifier


def predict_classifierSVM(classifier, X_test):
    y_pred = classifier.predict(X_test)
    return y_pred


def load_classifierSVM(name):
    classifier = joblib.load(name)
    return classifier
    
    
def save_classifierSVM(classifier,name):
    #name tem a extensão .pkl
    joblib.dump(classifier, name)