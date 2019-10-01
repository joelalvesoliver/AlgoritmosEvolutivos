#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Jan  3 21:13:31 2019

@author: joel
"""

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

def MediaStd(caminho,passoA):
    import os
    import numpy as np
    #lista = ["0","1","2","3","4","5","6","7","8","9"]
    lista = ["00"]
    files = []
    leu = False;
    for num in lista:
        #arq = [os.path.join(caminho, nome) for nome in os.listdir(caminho) if nome.startswith( num +"Dados")]
        arq = [os.path.join(caminho, nome) for nome in os.listdir(caminho)]
        
        for nome in arq:
            if not leu:
                files.append(nome)
                #leu = True
        #leu =False
        
    Medias = []
    Execucao = []
    RETORNO = []
    menor = 10000
    i = 0
    passo = passoA;
    for arq in files:
        print(arq)
        retorno = np.array(Leitura(arq))
        while passo < 2000:
            try:
                menor = np.amin(abs(retorno[i:passo]))
            except ValueError:  #raised if `y` is empty.
                print("")
            #print(menor)
            Medias.append(menor)
            Execucao.append(passo)
            i = passo
            passo += passoA;
            
        RETORNO.append([Medias,Execucao])
        Medias = []
        Execucao = []
        i = 0
        menor = 10000
        passo = passoA;
    return RETORNO


def grafico(Lista, caminho, nome):
    import matplotlib.pyplot as plt
    #import numpy as np
    X = Lista[0][1]
    Y = Lista[0][0]
    
#    plt.subplot(1, 2, 1)
#    plt.title('Base - M1')
#    plt.ylabel('Fitness')
#    plt.xlabel('Execuções')
#    #axs[0].gca().set_color_cycle(['red', 'green', 'blue', 'yellow'])
#    
#    plt.plot(X, Y, label="Base")
#    plt.plot(Lista[7][1], Lista[7][0],label="M1RF")
#    plt.plot(Lista[8][1], Lista[8][0],label="M1SVM")
#    plt.plot(Lista[9][1], Lista[9][0],label="M1AD")
#    plt.legend(loc='upper right')
#    
#    
#    plt.subplot(1, 2, 2)
#    plt.title('M1')
#    plt.ylabel('Fitness')
#    plt.xlabel('Execuções')
#    #axs[0].gca().set_color_cycle(['red', 'green', 'blue', 'yellow'])
#    
#    #plt.plot(X, Y, label="Base")
#    plt.plot(Lista[7][1], Lista[7][0],label="M1RF")
#    plt.plot(Lista[8][1], Lista[8][0],label="M1SVM")
#    plt.plot(Lista[9][1], Lista[9][0],label="M1AD")
#    plt.legend(loc='upper right')
    
#    #fig, axs = plt.subplots(1, 3, figsize=(15, 5), sharey=True)
    #plt.subplot(1, 3, 1)
    plt.title('RF')
    plt.ylabel('Fitness')
    plt.xlabel('Execuções')
    #axs[0].gca().set_color_cycle(['red', 'green', 'blue', 'yellow'])

    plt.plot(X, Y, label="Treino10% - Teste90%")
    plt.plot(Lista[1][1], Lista[1][0],label="Treino20% - Teste80%")
    plt.plot(Lista[4][1], Lista[4][0],label="Treino30% - Teste70%")
    plt.plot(Lista[3][1], Lista[3][0],label="Treino40% - Teste60%")
    plt.plot(Lista[2][1], Lista[2][0],label="Treino50% - Teste50%")
    plt.legend(loc='upper right')
#
#    plt.subplot(1, 3, 2)
#    plt.title('SVM')
#    plt.ylabel('Fitness')
#    plt.xlabel('Execuções')
#    plt.plot(X, Y, label="Base")
#    plt.plot(Lista[2][1], Lista[2][0],label="BATCHSVM")
#    plt.plot(Lista[6][1], Lista[6][0],label="ONLINESVM")
#    plt.plot(Lista[8][1], Lista[8][0],label="M1SVM")
#    plt.legend(loc='upper right')
#    
#    plt.subplot(1, 3, 3)
#    plt.title('AD')
#    plt.ylabel('Fitness')
#    plt.xlabel('Execuções')
#    plt.plot(X, Y, label="Base")
#    plt.plot(Lista[3][1], Lista[3][0],label="BATCHAD")
#    plt.plot(Lista[7][1], Lista[7][0],label="ONLINEAD")
#    plt.plot(Lista[9][1], Lista[9][0],label="M1AD")
#    plt.legend(loc='upper right')

    #axs[0].legend(['Base', 'BATCHRF', 'ONLINERF', 'M1RF'], loc='upper right')

    #fig.suptitle('Categorical Plotting')
    plt.savefig(caminho + nome) 
    plt.show()
    
CAMINHO = "/home/joel/MESTRADO/SERVER_PYTHON/servidor/Experimentos Artigo/Experimentos Batch SMPSO/SMPSOBatch"
A = MediaStd(CAMINHO,500)
grafico(A, CAMINHO, "DERastriginT.png")
