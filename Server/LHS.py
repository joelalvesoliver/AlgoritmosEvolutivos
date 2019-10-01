#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Sep  1 09:48:39 2018

@author: joel
"""
from pyDOE import *
import matplotlib.pyplot as plt
import numpy

k = lhs(2, samples=100)

x = []; y =[]

for ks in k:
    #print([len(ks), ks])
    x.append(ks[0])
    y.append(ks[1])
    
fig = plt.figure()
ax = fig.gca()
plt.scatter(x,y)
plt.grid()
plt.show()