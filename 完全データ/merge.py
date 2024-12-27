import pandas as pd

# ファイル名
file1 = 'modified_propublica_violent_recidivism_combined_seed_100.csv'
file2 = 'modified_propublica_violent_recidivism_test_seed_100.csv'
output_file = 'propublica_violent_recidivism.csv'

# ファイルの読み込み
df1 = pd.read_csv(file1)
df2 = pd.read_csv(file2)

# 必要な列のみを抽出（一番右と右から2番目の列を除く）
df1_selected = df1.iloc[:, :-2]
df2_selected = df2.iloc[:, :-2]

# 結合
combined_df = pd.concat([df1_selected, df2_selected], ignore_index=True)

# modified_adult_combined_seed_100.csv の1行目を新しいファイルの1行目として保存
combined_df.columns = df1_selected.columns

# 新しいファイルに書き出し
combined_df.to_csv(output_file, index=False)
