# Search

## Facts
Elasticsearch, OpenSearch, Algolia, etc.
https://pureinsights.com/blog/2021/elasticsearch-vs-opensearch-user-point-of-view-part-1-of-3/
https://www.elastic.co/what-is/opensearch

## Local ELK Setup
https://elk-docker.readthedocs.io/

A minimum of 4GB RAM assigned to Docker is required for the container to run.
See 2GB RAM for a single node for an MVP.
```shell
./scripts/start_elk.sh
```
or
```shell
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk sebp/elk:7.16.3
```
only elasticsearch
```shell
docker run -d --rm --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "ES_JAVA_OPTS=-Xms256m -Xmx750m" elasticsearch:7.17.0
```


# Cycling Marketplace Aggregator (Brands)
https://intheknowcycling.com/online-bike-store-rankings/

https://www.bike-components.de/en/brands/
https://www.bikester.ch/marken/
https://www.veloplus.ch/shop/marken
https://www.chainreactioncycles.com/ch/en/sitemap
https://www.merlincycles.com/sitemap.html
https://www.wiggle.com/sitemap
https://www.performancebike.com/brands
https://www.bike24.com/brands?menu=1664,1664
https://www.probikeshop.com/en/gb/breands.html


