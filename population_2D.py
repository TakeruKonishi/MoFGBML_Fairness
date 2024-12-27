# -*- coding: utf-8 -*-
"""
Created on Tue Dec 21 15:35:00 2021

@author: konishi
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

plt.rc('font', family='Times New Roman')

addFontSize = 20

def SummaryOneTrial(Dataset, trial):

    df_results = pd.read_csv(folder + Dataset + "_trial" + trial + sep + 'results.csv')
    
    numberofclasses = 2
    
    df_results = df_results[df_results['ruleNum'] >= numberofclasses]
    
    def find_closest_pop(value):
        return df_results.iloc[(df_results['Gmean_Dtra'] - value).abs().argsort()[:1]]
    
    #gmean_dtra_stats = df_results['Gmean_Dtst'].describe(percentiles=[.10, .20, .30, .40, .50, .60, .70, .80, .90])
    #percentile_values = [gmean_dtra_stats[name] for name in ['min', '10%', '20%', '30%', '40%', '50%', '60%', '70%', '80%', '90%', 'max']]

    #gmean_dtra_stats = df_results['Gmean_Dtra'].describe(percentiles=[.25, .50, .75])
    #percentile_values = [gmean_dtra_stats[name] for name in ['min', '25%', '50%', '75%', 'max']]
    
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
    
    pop_values = [pop_min,pop_first_quartile,pop_median,pop_third_quartile,pop_max]
    
    GmeanList = [df_results[df_results['pop'] == pop]['Gmean_Dtst'].iloc[0] for pop in pop_values]
    
    item = 'PPV_Dtst'
    item_list = [df_results[df_results['pop'] == pop][item].iloc[0] for pop in pop_values]

    return {"Gmean" : GmeanList, "item" : item_list}



sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german160000' + sep 

Dataset = 'german'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(1) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean1 = np.mean(Gmean, axis=0)
average_item1 = np.mean(item, axis=0)



sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german260000' + sep 

Dataset = 'german'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(1) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean2 = np.mean(Gmean, axis=0)
average_item2 = np.mean(item, axis=0)



sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german360000' + sep 

Dataset = 'german'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(1) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean3 = np.mean(Gmean, axis=0)
average_item3 = np.mean(item, axis=0)



sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german_remove_a160000' + sep 

Dataset = 'german_remove_a'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(1) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean4 = np.mean(Gmean, axis=0)
average_item4 = np.mean(item, axis=0)



sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german_remove_a260000' + sep 

Dataset = 'german_remove_a'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(1) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean5 = np.mean(Gmean, axis=0)
average_item5 = np.mean(item, axis=0)



sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'german_remove_a360000' + sep 

Dataset = 'german_remove_a'

# make trial number rr = {0,1,2}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(1) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

Gmean = [results[trial]["Gmean"] for trial in range(len(results))]

item = [results[trial]["item"] for trial in range(len(results))]

Gmean = np.array(Gmean)
item = np.array(item)

average_Gmean6 = np.mean(Gmean, axis=0)
average_item6 = np.mean(item, axis=0)

"""
average_item1=np.ma.masked_where(average_item1<2,average_item1)
average_item2=np.ma.masked_where(average_item2<2,average_item2)
average_item3=np.ma.masked_where(average_item3<2,average_item3)
average_item4=np.ma.masked_where(average_item4<2,average_item4)
"""


alpha = 0.9
plt.scatter(average_item1,average_Gmean1,color=[1,0,0],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item1, average_Gmean1,color=[1,0,0], alpha= alpha, zorder=1)
plt.scatter(average_item2,average_Gmean2,color=[0,0,1],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item2, average_Gmean2,color=[0,0,1], alpha= alpha, zorder=1)    
plt.scatter(average_item3,average_Gmean3,color=[0,1,0],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item3, average_Gmean3,color=[0,1,0], alpha= alpha, zorder=1)
plt.scatter(average_item4,average_Gmean4,color=[0.961,0.51,0.125],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item4, average_Gmean4,color=[0.961,0.51,0.125], alpha= alpha, zorder=1)
plt.scatter(average_item5,average_Gmean5,color=[0,1,1],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item5, average_Gmean5,color=[0,1,1], alpha= alpha, zorder=1)
plt.scatter(average_item6,average_Gmean6,color=[0,0.5,0],s=100,edgecolors="black", alpha= alpha, clip_on=False, zorder=2)
plt.plot(average_item6, average_Gmean6,color=[0,0.5,0], alpha= alpha, zorder=1)


plt.xlabel('PPVdiff',fontsize=20)
plt.ylabel('1-Gmean',fontsize=20)
#plt.xticks([2,10,20,30],fontsize=16)
plt.xticks(np.arange(0.10,0.36,0.05),fontsize=16)
plt.yticks(np.arange(0.30,0.51,0.05),fontsize=16)
plt.grid()
plt.rcParams['axes.axisbelow'] = True
plt.ylim(0.30,0.51)
plt.tight_layout()
#plt.savefig("adultPPVtst60000.pdf")
plt.savefig("germanPPVtst60000.png",format="png",dpi=400,bbox_inches='tight',pad_inches=0)
plt.show()

