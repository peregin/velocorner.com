# Install Rust

https://www.rust-lang.org/learn/get-started

Check for updates
```shell
cargo update --dry-run
```

## Docker
```shell
docker build -t peregin/velocorner.rates .
docker run --rm -it -p 9012:9012 peregin/velocorner.rates
docker push peregin/velocorner.rates:latest
```

