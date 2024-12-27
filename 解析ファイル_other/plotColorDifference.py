# -*- coding: utf-8 -*-
"""
Created on Wed Aug  3 13:25:56 2022

@author: Hiroki
"""

import pandas as pd
from matplotlib import pyplot as plt
from matplotlib.ticker import AutoMinorLocator
import numpy as np
import math

sep = "\\"

RR = 1
CC = 3

maxLimitRuleNum = 60
minLimitRuleNum = 2
#criteria = RR * CC / 2 + 1
criteria = 1
# =============================================================================
# makeSetting:
# 各MOPのプロットの設定(形，色，大きさ，ラベル，etc)
# =============================================================================
def makeSetting(Dataset, measure, ytickMeasure):
    settings = []
    
    settings.append({"Dataset":Dataset, "mop":"MOP1", "measure":measure, "marker":"o", "color":"blue", "size":200, "x":None, "y":None, "label":"MOP1 ($\it{f₁, f₂}$)", "ytick":ytickMeasure})
    settings.append({"Dataset":Dataset, "mop":"MOP2", "measure":measure, "marker":"o", "color":"red", "size":200, "x":None, "y":None, "label":"MOP2 ($\it{f₁, f₂, f₃}$)", "ytick":ytickMeasure})
    #settings.append({"Dataset":Dataset, "mop":"MOP3", "measure":measure, "marker":"o", "color":"green", "size":200, "x":None, "y":None, "label":"MOP3 ($\it{f₁, f₂, f₄}$)", "ytick":ytickMeasure})
    #settings.append({"Dataset":Dataset, "mop":"MOP4", "measure":measure, "marker":"o", "color":"brown", "size":100, "x":None, "y":None, "label":"MOP4 ($\it{f₁, f₂, f₃, f₄}$)", "ytick":ytickMeasure})
    
    return settings

def manuFigSetting():
    settings = {}
    
    settings["Gmean_Dtra"] = {"xMin" : 1, "xMax" : 40, "yMin" : 0.2, "yMax" : 1.0}
    #settings["Gmean_Dtst"] = {"xMin" : 1, "xMax" : 30, "yMin" : 0.1, "yMax" : 0.75}
    settings["FPR_Dtra"] = {"xMin" : 1, "xMax" : 40, "yMin" : 0, "yMax" : 0.3}
    #settings["FPR_Dtst"] = {"xMin" : 1, "xMax" : 30, "yMin" : 0, "yMax" : 0.25}
    settings["PPV_Dtra"] = {"xMin" : 1, "xMax" : 40, "yMin" : 0, "yMax" : 0.5}
    #settings["PPV_Dtst"] = {"xMin" : 1, "xMax" : 30, "yMin" : 0, "yMax" : 0.35}
    
    
    
    return settings

# =============================================================================
# figSetting:
# 図の設定(図の大きさ，軸の範囲，グリット線，目盛りの表示)
# =============================================================================
def figSetting(xMin, xMax, yMin, yMax):
    # figsizeで図のサイズを指定 横長にしたいなら左の数値を大きくする．
    fig, axes = plt.subplots(1, 1, figsize=(7, 7))
    
    # X ticks (= Number of rule)
    xH = 10
    xticks = np.arange(0, xMax + xH, xH)

    if xMin < minLimitRuleNum:
        xticks[0] = minLimitRuleNum
    else:
        xticks[0] = xMin
    
    xMin = xticks[0]
    xMax = xticks[len(xticks) - 1]
    axes.set_xlim(xMin - 2, xMax + 2)
    axes.set_xticks(xticks)
    
    # Y ticks
    yH = 0.05
    yMin = (int)(yMin / yH)
    yMin = yMin * yH
    yMax = (int)((yMax + yH) / yH)
    yMax = yMax * yH  
    
    yH = 0.1
    yticks = np.arange(yMin, yMax+yH, yH)
    yMin = yticks[0]
    yMax = yticks[-1]
    axes.set_ylim(yMin - 0.02, yMax + 0.02)
    axes.set_yticks(yticks)
    
    axes.grid(linewidth=0.4)
    axes.yaxis.set_minor_locator(AutoMinorLocator(3))
    axes.tick_params(which = 'major', length = 8, color = 'black', labelsize = 25)
    axes.tick_params(which = 'minor', length = 5, color = 'black', labelsize = 25)
    
    return fig, axes

# =============================================================================
# getAvaragePopulation_Results:
# 1つのMOP・データセットの結果を集計し，プロットに使うdictionaryを返す．
# =============================================================================
def getAvaragePopulation_Results(setting):
    Dataset = setting["Dataset"]
    measure = setting["measure"]
    mop = setting["mop"]
    
    Dir = '..\\results' + sep + 'fairness_result_' + str(mop) + sep
    
    resultsFiles = [Dir + Dataset + "_trial" + str(rr) + str(cc) + sep + "results.csv" for rr in range(RR) for cc in range(CC)]
    
    df_results = [pd.read_csv(resultFile) for resultFile in resultsFiles]
    
    for df in df_results:
        
        df['Gmean_Dtra'] = 1 - df['Gmean_Dtra']
    
    values = {str(i + 1) : [] for i in range(maxLimitRuleNum)}
    
    # for df in df_results:
        
    #     for ruleNum in range(minLimitRuleNum, maxLimitRuleNum, 1):
            
    #         if(any(df['ruleNum'].isin([ruleNum]))):
                
    #             values[str(ruleNum)].append(df[df['ruleNum'] == ruleNum].mean())
        
    # plotIndividual = filter(lambda x : len(x) >= criteria, [values[v] for v in values])
    # plotIndividual = list(map(lambda x : pd.concat(x), plotIndividual))
    
    #measure2 = "FPR_Dtra"
    #x = list(map(lambda x : x[measure2].mean(), plotIndividual))
    
    # x = list(map(lambda x : x["ruleNum"].min(), plotIndividual))
    # y = list(map(lambda x : x[measure].mean(), plotIndividual))
    
    
    x = []
    y = []
    
    
    for df in df_results:
        x.append(df["ruleNum"])
        y.append(df)
    
    
    return {"x" : x, "y" : y}


# =============================================================================
# plotAveragePopulation_Results:
# settingList内の結果を集計し，プロットを行う．
# =============================================================================
def plotAveragePopulation_Results(settingList):
    # results = [getAvaragePopulation_Results(setting) for setting in settingList]    
    # xMin = min([min(result["x"]) for result in results])
    # xMax = max([max(result["x"]) for result in results])
    # yMin = min([min(result["y"]) for result in results])
    # yMax = max([max(result["y"]) for result in results])
    
    measure = settingList[0]["measure"]
    setting = manuFigSetting()
   
    
    measure2 = "Gmean_Dtra"
    
    
    xMin = setting[measure]["xMin"]
    xMax = setting[measure]["xMax"]
    yMin = setting[measure2]["yMin"]
    yMax = setting[measure2]["yMax"]
    fig, axes = figSetting(xMin, xMax, yMin, yMax)
    
    plt.rcParams["font.family"] = "Times New Roman"     #全体のフォントを設定
    plt.rcParams["font.size"] = 14                      #フォントの大きさ
    plt.rcParams["xtick.minor.visible"] = False         #x軸補助目盛りの追加
    plt.rcParams["ytick.direction"] = "out"             #y軸の目盛線が内向き('in')か外向き('out')か双方向か('inout')
    plt.rcParams["ytick.major.size"] = 10               #y軸主目盛り線の長さ
    
    plt.subplots_adjust(left = 0.2, bottom = 0.2)
    for setting in settingList:
        result = getAvaragePopulation_Results(setting)
        setting["x"] = result["x"]
        setting["y"] = result["y"]
        
        marker = setting['marker']
        color = setting['color']
        size = setting['size']
        label = setting['label']
        linewidths = 1
        edgecolors = 'black'
        alpha = 0.8
        plt.rcParams['font.family'] = 'Times New Roman'
        
        c = [((1 - x["Gmean_Dtra"] ) / 0.1) for x in setting["y"]]
        
        setting["y"] = [x[measure2] for x in setting["y"]]
        
        
        axes.scatter(setting["x"],
                     setting["y"], 
                     s = size,
                     marker = marker,
                     c = c,
                     label = label,
                     linewidths = linewidths,
                     edgecolors = edgecolors,
                     alpha = alpha,
                     cmap = 'jet'
                     )
            
    axes.set_xlabel("Minimize $\it{f_{2}}$ (Number of rules)", fontsize = 25)
    axes.set_ylabel("Minimize $\it{f_{1}}$ (1-G-mean)", fontsize = 25)
    #plt.xlim(-0.02, 0.31)
    #axes.set_xticks([0, 0.05, 0.1, 0.15, 0.20, 0.25, 0.30])
    #plt.xticks(fontsize = 24)
    plt.legend()
    plt.show()
    
    return fig, axes


def main(Datset, measure, ytickMeasure):
    settingList = makeSetting(Dataset, measure, ytickMeasure)
    
    fig, axes =  plotAveragePopulation_Results(settingList)
    fileType = ".png"
    fileName = Dataset + "_" + measure + fileType
    #dpiで解像度を設定，デフォルトはdpi = 72
    fig.savefig(fileName, dpi = 400)
    plt.close("all")
    
    
if __name__ == '__main__':
    Dataset = "adult"
    # measure = "Gmean_Dtra"
    # ytickMeasure = "Gmean"
    # main(Dataset, measure, ytickMeasure)
    
    measureList = [["Gmean_Dtra", "Maximize $\it{f₁}$ (G-mean)"], ["Gmean_Dtst", "Maximize $\it{f₁}$ (G-mean)"], ["FPR_Dtra", "Minimize $\it{f₃}$ (FPR)"], ["FPR_Dtst", "Minimize $\it{f₃}$ (FPR)"], ["PPV_Dtra", "Minimize $\it{f₄}$ (PPV)"], ["PPV_Dtst", "Minimize $\it{f₄}$ (PPV)"]]
    for measure in measureList:    
        main(Dataset, measure[0], measure[1])  
