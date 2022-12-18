####################################################################################################
## Builder
####################################################################################################
FROM rust:1.65 AS builder

RUN update-ca-certificates

# Create appuser
ENV USER=rates
ENV UID=10001

RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    "${USER}"


WORKDIR /rates

COPY ./ .

# We no longer need to use the x86_64-unknown-linux-musl target
RUN cargo build --release

####################################################################################################
## Final image
####################################################################################################
FROM debian:buster-slim

RUN apt-get update && \
    apt-get dist-upgrade -y && \
    apt-get install -y libssl1.1 libssl-dev openssl

# Import from builder.
COPY --from=builder /etc/passwd /etc/passwd
COPY --from=builder /etc/group /etc/group

WORKDIR /rates

# Copy our build
COPY --from=builder /rates/target/release/exchange-rate-service ./

# Use an unprivileged user.
USER rates:rates

# enable logging with env_logger
ENV RUST_LOG=trace

CMD ["/rates/exchange-rate-service"]