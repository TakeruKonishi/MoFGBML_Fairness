# -*- coding: utf-8 -*-
"""
Created on Tue Dec 21 15:35:00 2021

@author: konishi
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

plt.rc('font', family='Times New Roman')

sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german1' + sep 

Dataset = 'german'

addFontSize = 20

def SummaryOneTrial(Dataset, trial):

    df_results = pd.read_csv(folder + Dataset + "_trial" + trial + sep + 'results.csv')
    
    numberofclasses = 2
    
    df_results = df_results[df_results['ruleNum'] >= numberofclasses]
    
    def find_closest_pop(value):
        return df_results.iloc[(df_results['Gmean_Dtra'] - value).abs().argsort()[:1]]
    
    gmean_dtra_stats = df_results['Gmean_Dtra'].describe()
    min_value = gmean_dtra_stats['min']
    first_quartile = gmean_dtra_stats['25%']
    median = gmean_dtra_stats['50%']
    third_quartile = gmean_dtra_stats['75%']
    max_value = gmean_dtra_stats['max']

    pop_min = find_closest_pop(min_value)['pop'].iloc[0]
    pop_first_quartile = find_closest_pop(first_quartile)['pop'].iloc[0]
    pop_median = find_closest_pop(median)['pop'].iloc[0]
    pop_third_quartile = find_closest_pop(third_quartile)['pop'].iloc[0]
    pop_max = find_closest_pop(max_value)['pop'].iloc[0]
    
    GmeanList = [min_value,first_quartile,median,third_quartile,max_value]
    
    item = 'ruleNum'
    
    item_dict = {
        "pop_min": df_results[df_results['pop'] == pop_min][item].iloc[0],
        "pop_first_quartile": df_results[df_results['pop'] == pop_first_quartile][item].iloc[0],
        "pop_median": df_results[df_results['pop'] == pop_median][item].iloc[0],
        "pop_third_quartile": df_results[df_results['pop'] == pop_third_quartile][item].iloc[0],
        "pop_max": df_results[df_results['pop'] == pop_max][item].iloc[0]
    }
    
    itemList = [
    item_dict['pop_min'],
    item_dict['pop_first_quartile'],
    item_dict['pop_median'],
    item_dict['pop_third_quartile'],
    item_dict['pop_max']
    ]

    return {"Gmean" : GmeanList, "item" : itemList}


# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(3) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean = np.mean(Gmean, axis=0)
average_item = np.mean(item, axis=0)

"""
sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'propublica-recidivism1' + sep 

Dataset = 'propublica-recidivism'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(3) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean = np.mean(Gmean, axis=0)
average_item = np.mean(item, axis=0)


sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'propublica-recidivism1' + sep 

Dataset = 'propublica-recidivism'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(3) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean = np.mean(Gmean, axis=0)
average_item = np.mean(item, axis=0)
"""

alpha = 0.7
plt.scatter(average_item,average_Gmean,color=[1,0,0],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item, average_Gmean,color=[1,0,0], alpha= alpha, zorder=1)
"""
plt.scatter(average_item,average_Gmean,color=[0,0,1],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item, average_Gmean,color=[0,0,1], alpha= alpha, zorder=1)    
plt.scatter(average_item,average_Gmean,color=[0,1,0],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item, average_Gmean,color=[0,1,0], alpha= alpha, zorder=1)
"""
plt.xlabel('item',fontsize=20)
plt.ylabel('1-Gmean',fontsize=20)
plt.xticks(np.arange(0.10,0.16,0.01),fontsize=16)
plt.yticks(np.arange(0.25,0.36,0.01),fontsize=16)
plt.grid()
plt.rcParams['axes.axisbelow'] = True
plt.ylim(0.25,0.36)
plt.tight_layout()
plt.savefig("propublica-recidivism.png",format="png",dpi=400,bbox_inches='tight',pad_inches=0)
plt.show()

