package api

//go:generate go run github.com/deepmap/oapi-codegen/cmd/oapi-codegen -generate client,types,server,spec -package api -o api.gen.go api.yaml
//go:generate gofmt -s -w api.gen.go
