FROM golang:1.14-alpine AS build

WORKDIR /src/
COPY internal/*.go /src/
RUN CGO_ENABLED=0 go build -o /bin/service

# start from an empty container and copy over the binaries from the build context
FROM scratch
COPY --from=build /bin/service /bin/service
ENTRYPOINT ["/bin/service"]
