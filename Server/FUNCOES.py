#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Nov 27 20:21:18 2018

@author: joel
"""

import csv
import os


def Leitura(name):
    arquivo = open(name,'r')
    linhas = arquivo.readlines()
    retorno = []
    cont = 0
    for linha in linhas:
        
        retorno.append(float(linha))
        cont+=1
        if cont >= 99900:
            break
    arquivo.close()
    
    return retorno


def MediaStd(caminho):
    import os
    import numpy as np
    lista = ["0","1","2","3","4","5","6","7","8","9"]
    files = []
    for num in lista:
        arq = [os.path.join(caminho, nome) for nome in os.listdir(caminho) if nome.startswith( num +"Dados")]
        for nome in arq:
            files.append(nome)
            
    RETORNO = []
    Medias = []
    i = 0
    for arq in files:
        print(arq)
        retorno = np.array(Leitura(arq))
        Medias.append(np.amin(retorno))
        if len(Medias) == 20:
           Medias.append([np.mean(Medias),np.std(Medias)])
           RETORNO.insert(i,Medias)
           i+=1
           Medias = []
           
    return RETORNO


def Salvar(vetor, caminhoarquivo):
    
    fout = open(caminhoarquivo +'.csv', 'w')

    writer = csv.DictWriter(fout, delimiter=',', quotechar='"', quoting=csv.QUOTE_ALL,fieldnames=['BASE','BATCHRF','BATCHSVM','BATCHTREE','ONLINERF','ONLINESVM','ONLINETREE','M1RF','M1SVM','M1TREE'])

    with open(caminhoarquivo +'.csv', 'w') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames = ['BASE','BATCHRF','BATCHSVM','BATCHTREE','ONLINERF','ONLINESVM','ONLINETREE','M1RF','M1SVM','M1TREE'], delimiter = ',')
        writer.writeheader()
        
        linha = 0;
        for row in vetor[0]:
            dh = dict(BASE=vetor[0][linha], BATCHRF=vetor[1][linha], BATCHSVM=vetor[2][linha],BATCHTREE=vetor[3][linha],ONLINERF=vetor[4][linha],ONLINESVM=vetor[5][linha],ONLINETREE=vetor[6][linha],M1RF=vetor[7][linha],M1SVM=vetor[8][linha],M1TREE=vetor[9][linha])
            writer.writerow(dh)
            linha+=1

    fout.close()