# go-swagger

```shell
docker pull quay.io/goswagger/swagger
alias swagger="docker run --rm -it  --user $(id -u):$(id -g) -e GOPATH=$HOME/go:/go -v $HOME:$HOME -w $(pwd) quay.io/goswagger/swagger"
swagger version

OR

brew tap go-swagger/go-swagger
brew install go-swagger
ln -s /usr/local/Cellar/go-swagger/0.25.0/bin/swagger /usr/local/bin/go-swagger

OR

go get -u github.com/go-swagger/go-swagger/cmd/swagger


go mod init velocorner.com/swagger
swagger validate ./swagger/swagger.yml
swagger generate server --target=./swagger --spec=./swagger/swagger.yml --name=user-service
```

# swaggo
```shell
go get github.com/swaggo/swag/cmd/swag
swag init # at the main.go location
```

# docker

docker image build -t user-service .
docker container run -p 8080:8080 user-service
```
