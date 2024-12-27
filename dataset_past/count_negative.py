import pandas as pd

# DataFrameを読み込む (CSVファイルからの例)
df = pd.read_csv('adult_cilabo.csv')  # 'your_file.csv'を適切なファイルパスに置き換えてください

def count_unique_negative_values(df):
    negative_counts = {}
    for column in df.columns:
        # 列の値が負のものをフィルタリング
        negatives = df[column][df[column] < 0]
        if not negatives.empty:
            # ユニークな負の値の数をカウント
            negative_counts[column] = negatives.nunique()
    return negative_counts

# 結果を取得
negative_counts = count_unique_negative_values(df)
print('adult_cilabo.csv')
print(negative_counts)
