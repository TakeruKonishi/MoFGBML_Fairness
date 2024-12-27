# -*- coding: utf-8 -*-
"""
Created on Wed Dec 22 09:32:18 2021

@author: kawano
"""

import pandas as pd
import statistics
import numpy as np
from matplotlib import pyplot as plt
import os



def SummaryGmeanPopResults(Dataset, RR, CC, folder):
    filesName = [folder + Dataset + "_trial" + str(rr) + str(cc) + sep + "results.csv" for rr in range(RR) for cc in range(CC)]
    dfList = list(map(lambda x: pd.read_csv(x), filesName))
    
    maxPopList = pd.concat([df[df['Gmean_Dtra'] == df['Gmean_Dtra'].max()].mean() for df in dfList], axis = 1)
    
    return maxPopList.mean(axis = "columns")

def SummaryBaseMOPGmeanResults(Dataset, RR, CC, baseMop, mop):
    basefolder = '..\\results' + sep + 'fairness_result_' + baseMop + sep
    baseGmean = SummaryGmeanPopResults(Dataset, RR, CC, basefolder).loc['Gmean_Dtra']
    
    folder = '..\\results' + sep + 'fairness_result_' + mop + sep
    filesName = [folder + Dataset + "_trial" + str(rr) + str(cc) + sep + "results.csv" for rr in range(RR) for cc in range(CC)]
    dfList = list(map(lambda x: pd.read_csv(x), filesName))
    
    idxList = [abs(df['Gmean_Dtra'] - baseGmean).idxmin() for df in dfList]
    popList = pd.concat([dfList[i].loc[idxList[i]] for i in range(len(dfList))], axis = 1)
    
    return popList.mean(axis = "columns")

def getMedianTrial(Dataset, RR, CC):
    mediamIndex = (RR * CC / 2) - 1 if RR * CC % 2 == 0 else int((RR * CC) / 2) + 1;
    
    filesName = [folder + Dataset + "_trial" + str(rr) + str(cc) + sep + "results.csv" for rr in range(RR) for cc in range(CC)]
    dfList = list(map(lambda x: pd.read_csv(x), filesName))
    
    maxPopList = pd.concat([df[df['Gmean_Dtra'] == df['Gmean_Dtra'].max()].mean() for df in dfList])["Gmean_Dtra"]
    
    return np.argsort(maxPopList['Gmean_Dtra'])[int(mediamIndex)]


def SummaryByEvaluation(Dataset, evaluation, trial):
    fileName = folder + Dataset + "_trial" + f'{trial:02}' + sep + "FUN-" + str(evaluation) + ".txt"
    colmuns = ["g-mean", "ruleNum"]
    df = pd.read_table(fileName, header = None, names = colmuns)
    return {"g-mean" : -df['g-mean'].min(), "ruleNum" : df['ruleNum'].max()}


def getAvaragePoplation_Evaluation(Dataset, evaluation, RR, CC):
    evaluationFile = [folder + Dataset + sep + Dataset + "_trial" + str(rr) + str(cc) + sep + "FUN-" + str(evaluation) + ".txt" for rr in range(RR) for cc in range(CC)]
    colmuns = ["g-mean", "ruleNum", "FPR"]
    dfList = list(map(lambda x : pd.read_table(x, header = None, names = colmuns), evaluationFile))
    
    values = {str(i+1):[] for i in range(maxLimitRuleNum)}
    for df in dfList:
        for ruleNum in range(minLimitRuleNum, maxLimitRuleNum, 1):
            if(any(df['ruleNum'].isin([ruleNum]))):
                values[str(ruleNum)].append(df[df["ruleNum"] == ruleNum])
    
    plotIndividual = list(filter(lambda x : len(x) >= criteria, [values[v] for v in values]))
    
    x = list(map(lambda x : x[0].iloc[0]["ruleNum"], plotIndividual))
    y = list(map(lambda x : statistics.mean(pd.concat(x)["g-mean"]), plotIndividual))
    
    return x, np.abs(y)

def plotAveragePopulation_Evaluation(Dataset, evaluation, RR, CC):
    fig = plt.figure(figsize = (16, 9))
    ax = fig.gca()
    x, y = getAvaragePoplation_Evaluation(Dataset, evaluation, RR, CC)
    ax.scatter(x, y)
    ax.set_title(str(evaluation))
    ax.set_ylim(0, 1)
    ax.set_xlim(minLimitRuleNum, maxLimitRuleNum)
    if not os.path.exists(folder + Dataset + sep + "PNG"):
        os.mkdir(folder + Dataset + sep + "PNG")
    fig.savefig(folder + Dataset + sep + "PNG" + sep + "averagePopulation-" + str(evaluation) + ".png")
    plt.close("all")


"""
try SummaryBaseMOPGmeanResults****************************
"""
Dataset = "adult"
baseMop = "MOP1"
mop = "MOP4"

sep = "\\"
folder = '..\\results' + sep + 'fairness_result_' + mop + sep


start = 3000
end = 300000
freq = 3000

RR = 3
CC = 10

criteria = (CC * RR / 2 + 1)
#criteria = 1

maxLimitRuleNum = 60
minLimitRuleNum = 1


results = SummaryBaseMOPGmeanResults(Dataset, RR, CC, baseMop, mop)
print(results)

"""
try SummaryByEvaluation and Plot
"""
# x = np.arange(start, end + 1, freq)
# y = [SummaryByEvaluation(Dataset, evaluation, 0)['g-mean'] for evaluation in x]
# plt.ylim(0.472, 0.474)
# plt.plot(x, y)


"""
try getMedianTrial****************************
"""
# medianTrial = getMedianTrial(Dataset, RR, CC)
# print(medianTrial)


"""
try SummaryGmeanPopResults****************************
"""
results = SummaryGmeanPopResults(Dataset, RR, CC, folder)
print(results)

