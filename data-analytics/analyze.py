import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

def analyze():
    df = cleanup("data/432909.json.gz")

    plt.style.use('fivethirtyeight')
    #sns.countplot(df['type'])
    #plt.title('Types of activities')
    #sns.pairplot(df, hue='type')

    #input("Press any key to close")


def cleanup(name):
    print(f"loading and cleaning data from {name} ...")
    df = pd \
        .read_json(name, orient="values", compression="gzip") \
        .drop(
        ["external_id", "upload_id", "athlete", "name", "resource_state", "average_heartrate", "max_heartrate",
         "start_date_local", "gear_id"],
        axis=1)

    pd.set_option('display.max_columns', 15)
    pd.set_option('display.width', 200)

    print("convert units ...")
    df['distance'] = df['distance'] / 1000
    df['distance'] = df['distance'].round(2)

    df['avg_temp'] = df['average_temp']
    df.drop('average_temp', axis=1, inplace=True)

    df['avg_cadence'] = df['average_cadence'].round(1)
    df.drop('average_cadence', axis=1, inplace=True)

    df['avg_watts'] = df['average_watts'].round(1)
    df.drop('average_watts', axis=1, inplace=True)

    # km/h
    df['average_speed'] = df['average_speed'] * 18 / 5
    df['average_speed'] = df['average_speed'].round(2)
    df['avg_speed'] = df['average_speed']
    df.drop('average_speed', axis=1, inplace=True)
    df['max_speed'] = df['max_speed'] * 18 / 5
    df['max_speed'] = df['max_speed'].round(2)

    # seconds to minutes
    df['elapsed_time'] = df['elapsed_time'] / 60
    df['elapsed_time'] = df['elapsed_time'].astype(int)
    df['elapsed_mins'] = df['elapsed_time']
    df.drop('elapsed_time', axis=1, inplace=True)

    df['moving_time'] = df['moving_time'] / 60
    df['moving_time'] = df['moving_time'].astype(int)
    df['moving_mins'] = df['moving_time']
    df.drop('moving_time', axis=1, inplace=True)

    # set date as index
    df['start_date'] = pd.to_datetime(df['start_date']).dt.date
    df.set_index('start_date', inplace=True)
    df.index.rename('date', inplace=True)

    # keep significant data only
    df = df[df['type'].isin(['Ride', 'Run', 'Hike', 'AlpineSki'])]
    #print(df['type'].value_counts())

    # check NA fields
    df['max_watts'] = df['max_watts'].fillna(0)
    df['avg_watts'] = df['avg_watts'].fillna(0)
    df['avg_cadence'] = df['avg_cadence'].fillna(0)
    df['avg_temp'] = df['avg_temp'].fillna(20) # avg 20 C if NA?
    #print(df.columns[df.isna().any()].tolist())


    print(df.info())
    print(df.head(n=5))
    return df



if __name__ == "__main__":
    analyze()
