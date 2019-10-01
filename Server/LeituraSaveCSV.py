#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Jul 30 20:26:41 2019

@author: joe
"""

import csv
import os


def Leitura(name):
    arquivo = open(name,'r')
    linhas = arquivo.readlines()
    retorno = []
    for linha in linhas:
        retorno.append(linha)
        
    arquivo.close()
    
    return retorno


def MediaStd(caminho, string):
    import os
    import numpy as np
    lista = ["1"]
    files = []
    for num in lista:
        arq = [os.path.join(caminho, nome) for nome in os.listdir(caminho) if nome.__contains__(string)]
        for nome in arq:
            files.append(nome)
            
    RETORNO = []
    i = 0
    for arq in files:
        print(arq)
        RETORNO.insert(i,Leitura(arq))
        i+=1
          
           
    return RETORNO


def Salvar(vetor, caminhoarquivo, prob):
    
    fout = open(caminhoarquivo +'.csv', 'w')

    if(prob == "wfg"):
        writer = csv.DictWriter(fout, delimiter=',', quotechar='"', quoting=csv.QUOTE_ALL,fieldnames=['WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8', 'WFG9'])
    else:
        writer = csv.DictWriter(fout, delimiter=',', quotechar='"', quoting=csv.QUOTE_ALL,fieldnames=['DTLZ1','DTLZ2','DTLZ3','DTLZ4','DTLZ5','DTLZ6','DTLZ7'])
    with open(caminhoarquivo +'.csv', 'w') as csvfile:
        if(prob == "wfg"):
            writer = csv.DictWriter(csvfile, fieldnames = ['WFG1','WFG2','WFG3','WFG4','WFG5','WFG6','WFG7','WFG8', 'WFG9'], delimiter = ',')
        else:
            writer = csv.DictWriter(csvfile, fieldnames = ['DTLZ1','DTLZ2','DTLZ3','DTLZ4','DTLZ5','DTLZ6','DTLZ7'], delimiter = ',')
        
        writer.writeheader()
        
        linha = 0;
        for row in vetor[0]:
            if(prob == "wfg"):
                dh = dict(WFG1=vetor[0][linha], WFG2=vetor[1][linha], WFG3=vetor[2][linha],WFG4=vetor[3][linha],WFG5=vetor[4][linha],WFG6=vetor[5][linha],WFG7=vetor[6][linha],WFG8=vetor[7][linha],WFG9=vetor[8][linha])
            else:
                dh = dict(DTLZ1=vetor[0][linha], DTLZ2=vetor[1][linha], DTLZ3=vetor[2][linha],DTLZ4=vetor[3][linha],DTLZ5=vetor[4][linha],DTLZ6=vetor[5][linha],DTLZ7=vetor[6][linha])

            writer.writerow(dh)
            linha+=1

    fout.close()