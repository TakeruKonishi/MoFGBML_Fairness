import pandas as pd
import numpy as np
import os

# 数値属性とカテゴリカル属性の指定
numeric_columns = ['age', 'fnlwgt', 'education-num', 'capital-gain', 'capital-loss', 'hours-per-week']
categorical_columns = ['workclass', 'education', 'marital-status', 'occupation', 'relationship', 'race', 'sex', 'native-country']
target_column = 'y'  # クラスラベルの列

# ファイルが存在するディレクトリ
directory = os.getcwd()  # 同じディレクトリにあることを想定

# 正規化とラベル付けを行う関数
def process_file(file_path):
    df = pd.read_csv(file_path)
    
    # 欠損値を含む行を削除
    df = df.replace("?", np.nan).dropna()

    # 数値属性の0-1正規化
    for col in numeric_columns:
        if col in df.columns:
            max_value = df[col].max()
            min_value = df[col].min()
            df[col] = (df[col] - min_value) / (max_value - min_value)
    
    # カテゴリカル属性のラベル付け（負の数でラベル化）
    for col in categorical_columns:
        if col in df.columns:
            unique_values = df[col].unique()
            label_map = {val: -(i+1) for i, val in enumerate(unique_values)}
            df[col] = df[col].map(label_map)

    # race列をy列の左側にコピーして挿入
    if 'race' in df.columns:
        df.insert(df.columns.get_loc(target_column), 'race_copy', df['race'])  # y列の左側にrace列を挿入
    
    # コピーされたrace列の値を変換 (-1は1に、それ以外は0に変換)
    df['race_copy'] = df['race_copy'].apply(lambda x: 1 if x == -1 else 0)

    return df

# パターン数、属性数、クラス数を1行目に記載してファイルを保存する関数
def process_file_for_header(file_path):
    df = process_file(file_path)
    
    # パターン数: 2行目以降の行数
    pattern_count = len(df)
    
    # 属性数: y列とrace_copy列を除く列数
    attribute_count = len(df.columns) - 2  # yとrace_copyを除外
    
    # ターゲットクラスのクラス数: y列のユニークな値の数
    class_count = df['y'].nunique()
    
    # ファイルの1行目に "パターン数,属性数,クラス数" を書き込み、データをその下に続ける
    header = f"{pattern_count},{attribute_count},{class_count}"
    
    # 新しいファイルの書き込み
    output_file = file_path.replace(".csv", "_modified.csv")
    with open(output_file, mode="w", newline='') as f:  # newline='' を指定
        # 1行目にパターン数,属性数,クラス数を記述
        f.write(header + "\n")
        # それ以降のデータを書き込む
        df.to_csv(f, index=False, header=False)
    
    print(f"ファイル '{output_file}' を作成しました。")

# 処理するファイルのリスト
file_list = [f"adult_train_combined_seed_{seed}.csv" for seed in range(100, 110)] + \
            [f"adult_train_seed_{seed}.csv" for seed in range(100, 110)] + \
            [f"adult_val_seed_{seed}.csv" for seed in range(100, 110)] + \
            [f"adult_test_seed_{seed}.csv" for seed in range(100, 110)]

# 各ファイルに対して処理を実行
for file_name in file_list:
    file_path = os.path.join(directory, file_name)
    
    # ファイルを読み込んで処理
    process_file_for_header(file_path)
