import pandas as pd
import os

# スクリプトと同じディレクトリにファイルが配置されていると仮定
current_directory = os.getcwd()  # 現在のディレクトリを取得

# 100から109までのtrainとvalのファイルを統合
for seed in range(100, 110):
    # ファイル名を作成
    train_file = f"adult_train_seed_{seed}.csv"
    val_file = f"adult_val_seed_{seed}.csv"
    
    # データフレームとして読み込み
    train_df = pd.read_csv(train_file)
    val_df = pd.read_csv(val_file)
    
    # trainとvalを結合
    combined_df = pd.concat([train_df, val_df], ignore_index=True)
    
    # 統合したデータを新しいCSVファイルとして保存
    combined_file = f"adult_train_combined_seed_{seed}.csv"
    combined_df.to_csv(combined_file, index=False)

    print(f"ファイル '{combined_file}' を作成しました。")

# testファイルはそのまま使用します
