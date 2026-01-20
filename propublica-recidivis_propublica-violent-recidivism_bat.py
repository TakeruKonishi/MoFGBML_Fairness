# -*- coding: utf-8 -*-
"""
Created on Fri Oct  7 01:04:14 2022

@author: kawano
"""

import subprocess
from concurrent.futures import ThreadPoolExecutor


def detection_async(_request):
    
    result = subprocess.run(["Java", "-Xms1g", "-Xmx8g", "-jar",
                             _request["jarFile"],
                             _request["dataset"],
                             _request["algorithmID"],
                             _request["experimentID"],
                             _request["parallelCores"],
                             _request["trainFile"],
                             _request["testFile"],
                             _request["mopIndex"],
                             _request["seed"],
                             _request["sensitiveIndex"]])
    
    return result


def detection_async_parallel(_requests):
    
    results = []
    
    with ThreadPoolExecutor() as executor:
        
        for result in executor.map(detection_async, _requests):
            
            results.append(result)
            
    return results

def run(Dataset):

    mopIndex = 1
    sensitiveIndex = 3

    requests = [{"trial" : f"{i}_{j}",
                "dataset" : Dataset, 
                "jarFile" : "target\MoFGBML-23.0.0-SNAPSHOT-Fairness-Group.jar", 
                "algorithmID" : f"Fairness\{Dataset}{mopIndex}",
                "parallelCores" : "5",
                "experimentID" : f"trial{i}{j}",
                "trainFile" : f"dataset\\{Dataset}\\a{i}_{j}_{Dataset}-10tra.dat",
                "testFile" : f"dataset\\{Dataset}\\a{i}_{j}_{Dataset}-10tst.dat",
                "mopIndex" : str(mopIndex),
                "seed": str(100 + j),
                "sensitiveIndex": str(sensitiveIndex)} \
                for i in range(1) for j in range(10)]
    
    for result in detection_async_parallel(requests):
        
        print(result)

if __name__ == "__main__":
    
    DatasetList = ["propublica-recidivism", "propublica-violent-recidivism"]

    for dataset in DatasetList:
        run(dataset)
    
    