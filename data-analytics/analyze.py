import pandas as pd


def analyze():
    name = "data/432909.json.gz"
    print(f"analyzing {name} ...")
    df = pd\
        .read_json(name, orient="values", compression="gzip")\
        .drop(["type", "external_id", "upload_id", "athlete", "name"], axis=1)
    print(df.info())
    print(df.head())


if __name__ == "__main__":
    analyze()
