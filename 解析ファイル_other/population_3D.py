# -*- coding: utf-8 -*-
"""
Created on Tue Dec 21 15:35:00 2021

@author: konishi
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

plt.rc('font', family='Times New Roman')

numberofclasses = 2

sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german1origin' + sep 

Dataset = 'german'

addFontSize = 20

def SummaryOneTrial(Dataset, trial):
    
    df_results = pd.read_csv(folder + Dataset + "_trial" + trial + sep + 'results.csv')

    numberofclasses = 2

    df_results = df_results[df_results['ruleNum'] >= numberofclasses]
    
    df_results = pd.read_csv(folder + Dataset + "_trial" + trial + sep + 'results.csv')
    TraErrorList = list(df_results['Gmean_Dtra'])
    TstErrorList = list(df_results['Gmean_Dtst'])
    RuleNumList = list(df_results['ruleNum'])
    TraFPRList = list(df_results['FPR_Dtra'])
    TstFPRList = list(df_results['FPR_Dtst'])
    TraPPVList = list(df_results['PPV_Dtra'])
    TstPPVList = list(df_results['PPV_Dtst'])

    return {"TraError" : TraErrorList, "TstError" : TstErrorList, "ruleNum" : RuleNumList, "TraFPR" : TraFPRList, "TstFPR" : TstFPRList, "TraPPV" : TraPPVList, "TstPPV" : TstPPVList}

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(3) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

TraError = [results[trial]["TraError"] for trial in range(len(results))]

TstError = [results[trial]["TstError"] for trial in range(len(results))]

ruleNum = [results[trial]["ruleNum"] for trial in range(len(results))]

TraFPR = [results[trial]["TraFPR"] for trial in range(len(results))]

TstFPR = [results[trial]["TstFPR"] for trial in range(len(results))]

TraPPV = [results[trial]["TraPPV"] for trial in range(len(results))]

TstPPV = [results[trial]["TstPPV"] for trial in range(len(results))]

TraError = np.array(TraError)
TstError = np.array(TstError)
ruleNum = np.array(ruleNum)
TraFPR = np.array(TraFPR)
TstFPR = np.array(TstFPR)
TraPPV = np.array(TraPPV)
TstPPV = np.array(TstPPV)

average_TraError = np.mean(TraError, axis=0)
average_TstError = np.mean(TstError, axis=0)
average_ruleNum = np.mean(ruleNum, axis=0)
average_TraFPR  = np.mean(TraFPR , axis=0)
average_TstFPR = np.mean(TstFPR, axis=0)
average_TraPPV = np.mean(TraPPV, axis=0)
average_TstPPV = np.mean(TstPPV, axis=0)



alpha = 0.7
fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')

ax.scatter(average_TraFPR, average_TraPPV, average_TraError, color=[1,0,0], s=50,edgecolors="black", alpha=alpha, clip_on=False)
#ax.scatter(TstError, TstFPR, TstPPV, color=[1,0,0], s=100,edgecolors="black", alpha=alpha, clip_on=False)

ax.set_xlabel('FPRdiff',fontsize=10,labelpad=0.1)
ax.set_ylabel('PPVdiff',fontsize=10,labelpad=0.1)
ax.set_zlabel('1-Gmean',fontsize=10,labelpad=0.1)

ax.set_xlim(0.0, 0.21)
ax.set_ylim(0.0,0.41)
ax.set_zlim(0, 1.1)

ax.set_xticks(np.arange(0.0,0.21,0.05),fontsize=16)
ax.set_yticks(np.arange(0.0,0.41,0.05),fontsize=16)
ax.set_zticks(np.arange(0.0,1.1,0.2),fontsize=16)

ax.tick_params(axis='x', which='major', pad=0.01)
ax.tick_params(axis='y', which='major', pad=0.01)
ax.tick_params(axis='z', which='major', pad=0.01)

ax.grid()
plt.tight_layout()
plt.savefig("german1traX.png",format="png",dpi=400,bbox_inches='tight',pad_inches=0)
plt.show()

