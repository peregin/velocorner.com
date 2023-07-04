# Install Rust

https://www.rust-lang.org/learn/get-started

Install Rust from shell
```shell
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

Update `rust-analyzer`, the default is not working with IntelliJ (will cause compilation error when using `cached` library)
```shell
rustup component add rust-analyzer
rustup run stable rust-analyzer --version
```

# Learn Rust
- https://www.rust-lang.org/
- https://github.com/google/comprehensive-rust
- https://opensource.googleblog.com/2023/06/rust-fact-vs-fiction-5-insights-from-googles-rust-journey-2022.html
- https://app.pluralsight.com/library/courses/fundamentals-rust/table-of-contents
- https://cheats.rs/
- https://github.com/mre/idiomatic-rust
- https://github.com/rust-unofficial/awesome-rust
- https://github.com/ctjhoa/rust-learning

# Exchange Rate Service
Connects to exchangerate.host on demand and retrieves the latest conversion rates.
It uses a one-hour cache. 

Supports the following `json` endpoints:
- /rates/currencies - to retrieve supported currencies 
- /rates/:base - to retrieve all FX rates for a given base currency 
- /rates/:base/:counter - to retrieve a specific rate for a given currency pair

The root path `/` retrieves a welcome page in `text/html`.

## Cargo
Useful commands

```shell
# check for updates
cargo update --dry-run
# clean build
cargo clean
cargo tree
cargo fix
cargo build --release
```

## Docker
```shell
docker build -t peregin/velocorner.rates .
docker run --rm -it -p 9012:9012 peregin/velocorner.rates
docker push peregin/velocorner.rates:latest
```

