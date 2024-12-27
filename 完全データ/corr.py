import pandas as pd
import numpy as np
from sklearn.feature_selection import mutual_info_classif
from scipy.stats import chi2_contingency
from sklearn.preprocessing import LabelEncoder

# データの読み込み
data = pd.read_csv("propublica_violent_recidivism.csv")

# センシティブ属性を定義
sensitive_attr = "race"

# データの前処理：量的変数と質的変数を分類
quantitative_cols = [col for col in data.columns if any(data[col] > 0)]  # 正の値を含む列
qualitative_cols = [col for col in data.columns if any(data[col] < 0)]   # 負の値を含む列
qualitative_cols.remove(sensitive_attr)  # センシティブ属性を除外

# 相関結果を格納する辞書
correlation_results = {}

# センシティブ属性と量的変数の相関比を計算
def eta_squared(x, y):
    # x: 質的変数, y: 量的変数
    classes = list(set(x))
    n_total = len(y)
    y_mean = np.mean(y)
    ss_total = sum((y_i - y_mean) ** 2 for y_i in y)

    ss_between = sum(len(y[x == c]) * (np.mean(y[x == c]) - y_mean) ** 2 for c in classes)
    return ss_between / ss_total

for col in quantitative_cols:
    if col != sensitive_attr:
        eta = eta_squared(data[sensitive_attr], data[col])
        correlation_results[col] = ("相関比", eta)

# センシティブ属性と質的変数の連関係数を計算
for col in qualitative_cols:
    if col != sensitive_attr:
        contingency_table = pd.crosstab(data[sensitive_attr], data[col])
        chi2, p, dof, expected = chi2_contingency(contingency_table)
        n = contingency_table.sum().sum()
        phi2 = chi2 / n
        r, k = contingency_table.shape
        phi2corr = max(0, phi2 - ((k - 1) * (r - 1)) / (n - 1))
        rcorr = r - ((r - 1) ** 2) / (n - 1)
        kcorr = k - ((k - 1) ** 2) / (n - 1)
        cramers_v = np.sqrt(phi2corr / min((kcorr - 1), (rcorr - 1)))
        correlation_results[col] = ("連関係数", cramers_v)

# 結果を表示
for attr, (method, value) in correlation_results.items():
    print(f"{sensitive_attr} と {attr} の {method}: {value:.3f}")
