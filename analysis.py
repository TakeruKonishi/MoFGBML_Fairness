# -*- coding: utf-8 -*-
"""
Created on Tue Dec 17 11:52:00 2021

@author: konishi
"""

import pandas as pd
import statistics


sep = "\\"

folder = 'results' + sep + 'Fairness' + sep + 'propublica-recidivism1' + sep 

Dataset = 'propublica-recidivism'

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
    
    item = 'Gmean_Dtra'
    
    item_dict = {
        "pop_min": df_results[df_results['pop'] == pop_min][item].iloc[0],
        "pop_first_quartile": df_results[df_results['pop'] == pop_first_quartile][item].iloc[0],
        "pop_median": df_results[df_results['pop'] == pop_median][item].iloc[0],
        "pop_third_quartile": df_results[df_results['pop'] == pop_third_quartile][item].iloc[0],
        "pop_max": df_results[df_results['pop'] == pop_max][item].iloc[0]
    }

    return item_dict


# make trial number rr = {0}, cc = {0,1,...9}
trial = [str(rr) + str(cc) for rr in range(1) for cc in range(10)]

results = list(map(lambda x : SummaryOneTrial(Dataset, x), trial))

pop_min = statistics.mean([results[trial]["pop_min"] for trial in range(len(results))])

pop_minstd = statistics.stdev([results[trial]["pop_min"] for trial in range(len(results))])

pop_first_quartile = statistics.mean([results[trial]["pop_first_quartile"] for trial in range(len(results))])

pop_first_quartilestd = statistics.stdev([results[trial]["pop_first_quartile"] for trial in range(len(results))])

pop_median = statistics.mean([results[trial]["pop_median"] for trial in range(len(results))])

pop_medianstd = statistics.stdev([results[trial]["pop_median"] for trial in range(len(results))])

pop_third_quartile = statistics.mean([results[trial]["pop_third_quartile"] for trial in range(len(results))])

pop_third_quartilestd = statistics.stdev([results[trial]["pop_third_quartile"] for trial in range(len(results))])

pop_max = statistics.mean([results[trial]["pop_max"] for trial in range(len(results))])

pop_maxstd = statistics.stdev([results[trial]["pop_max"] for trial in range(len(results))])


# ----print result ----

print("Average pop_min : " + str(pop_min))

print("std pop_min : " + str(pop_minstd))

print("Average pop_first_quartile : " + str(pop_first_quartile))

print("std pop_first_quartile : " + str(pop_first_quartilestd))

print("Average pop_median : " + str(pop_median))

print("std pop_median : " + str(pop_medianstd))

print("Average pop_third_quartile : " + str(pop_third_quartile))

print("std pop_third_quartile : " + str(pop_third_quartilestd))

print("Average pop_max : " + str(pop_max))

print("std pop_max : " + str(pop_maxstd))






