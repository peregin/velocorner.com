import pyspark
from pyspark.sql import SparkSession
from pyspark.sql import Row
import os

def analyze():
    print("analyzing...")
    spark = SparkSession.builder \
        .config("spark.jars", "/Users/levi/Downloads/postgresql-42.2.6.jar") \
        .master("local") \
        .appName("PySparkPsql") \
        .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    df = spark.read.format("jdbc") \
        .option("url", "jdbc:postgresql://localhost:5492/velocorner") \
        .option("driver", "org.postgresql.Driver") \
        .option("dbtable", "activity") \
        .option("user", "velocorner") \
        .option("password", os.getenv('PASSWORD')).load()
    df.printSchema()
    df.show(5)

if __name__ == "__main__":
    analyze()
