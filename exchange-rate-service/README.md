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

## Goodies
Install Rust from shell
```shell
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```
Update `rust-analyzer`, the default is not working with IntelliJ
```shell
rustup component add rust-analyzer
rustup run stable rust-analyzer --version
```
Troubleshoot
```shell
cargo clean
cargo tree
cargo fix
cargo build --release
```

