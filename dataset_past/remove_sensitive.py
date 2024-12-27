import os
import shutil
import pandas as pd

# データセット名を設定
dataset = 'propublica-recidivism'

# 元のディレクトリと新しいディレクトリのパス
original_directory_path = dataset
new_directory_path = f"{dataset}_remove_a"
os.makedirs(new_directory_path, exist_ok=True)

# 処理するファイル名のパターンを設定 (traとtstファイル)
pattern_files_tra = {f"a{i}_{j}_{dataset}-10tra.dat" for i in range(3) for j in range(10)}
pattern_files_tst = {f"a{i}_{j}_{dataset}-10tst.dat" for i in range(3) for j in range(10)}

# 元のディレクトリ内のすべてのファイルを取得
all_files = os.listdir(original_directory_path)

# 各ファイルに対して処理を行う
for file_name in all_files:
    file_path = os.path.join(original_directory_path, file_name)

    # traまたはtstパターンに一致する場合は変更処理を行う
    if file_name in pattern_files_tra or file_name in pattern_files_tst:
        # データを文字列として読み込む
        first_row = pd.read_csv(file_path, nrows=1, header=None, dtype=str)
        rest_of_data = pd.read_csv(file_path, skiprows=1, header=None, dtype=str)

        # 3番目の列（インデックス2）を削除
        rest_of_data.drop(columns=[3], inplace=True)

        # 1行目の2列目の値を1減算し、文字列形式で保存
        first_row.iloc[0, 1] = str(int(first_row.iloc[0, 1]) - 1)

        # 変更されたデータを結合
        modified_df = pd.concat([first_row, rest_of_data], ignore_index=True)

        # 新しいファイル名を設定
        new_file_name = file_name.replace('-10tra.dat', '_remove_a-10tra.dat').replace('-10tst.dat', '_remove_a-10tst.dat')
        new_file_path = os.path.join(new_directory_path, new_file_name)

        # 新しいディレクトリに保存
        with open(new_file_path, 'w') as file:
            for index, row in modified_df.iterrows():
                # すべての行を文字列として処理し、NaNを除外して出力
                formatted_row = ','.join([str(val) for val in row if pd.notna(val)])
                file.write(formatted_row + '\n')
    else:
        # パターンに一致しない場合はコピーする
        shutil.copy(file_path, os.path.join(new_directory_path, file_name))
