####################################################################################################
## Builder - do not upgrade - openssl3 not supported properly
####################################################################################################
FROM rust:1.68 AS builder

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
#FROM debian:bookworm-slim - security issue
FROM debian:buster-slim

RUN apt-get update -y && \
    apt-get dist-upgrade -y && \
    apt-get install -y libssl-dev openssl ca-certificates && \
    update-ca-certificates

# Import from builder.
COPY --from=builder /etc/passwd /etc/passwd
COPY --from=builder /etc/group /etc/group

WORKDIR /rates

# Copy our build
COPY --from=builder /rates/target/release/exchange-rate-service ./

# Use an unprivileged user.
USER rates:rates

# enable logging with env_logger
ENV RUST_LOG=info
# display backtrace
ENV RUST_BACKTRACE=1
ENV RUST_BACKTRACE=full

EXPOSE 9012

CMD ["/rates/exchange-rate-service"]