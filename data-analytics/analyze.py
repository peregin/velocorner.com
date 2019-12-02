import pandas as pd


def analyze():
    name = "data/432909.json.gz"
    print(f"loading and cleaning data from {name} ...")
    df = pd \
        .read_json(name, orient="values", compression="gzip") \
        .drop(
        ["type", "external_id", "upload_id", "athlete", "name", "resource_state", "average_heartrate", "max_heartrate",
         "start_date_local", "gear_id"],
        axis=1)
    print("convert units ...")
    df['distance'] = df['distance'] / 1000
    df['distance'] = df['distance'].round(2)
    df['average_speed'] = df['average_speed'] * 18 / 5
    df['average_speed'] = df['average_speed'].round(2)

    print(df.info())
    print(df.head())

    print("done ...")


if __name__ == "__main__":
    analyze()
