# data-crawler

Module responsible for crawling data from various sources.
Testing with cycling platforms, web shops.
Searches for products from multiple sources and shows the price, availability, description, etc.

# Setup

## Repo setup when adding external dependencies
```shell script
# detect all dependencies
go mod tidy
```

## Run
```shell script
# generate models
go generate ./api/

# run server
go run cmd/server/main.go
```