package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"

	"github.com/go-chi/chi"
	httpSwagger "github.com/swaggo/http-swagger"
)

// @title User Analytics
// @version 1.0
// @description Service for user analytics
// @termsOfService http://swagger.io/terms/

// @contact.name API Support
// @contact.email velocorner.com@gmail.com

// @license.name Apache 2.0
// @license.url http://www.apache.org/licenses/LICENSE-2.0.html

// @host velocorner.com
func main() {
	serverPort := flag.Int("port", 3000, "Port to run this service on")
	fmt.Println("using port:", *serverPort)

	r := chi.NewRouter()

	r.Get("/", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "Aloha")
	})
	r.Get("/swagger/*", httpSwagger.Handler(
		httpSwagger.URL("http://localhost:3000/swagger/doc.json"), //The url pointing to API definition"
	))

	log.Fatal(http.ListenAndServe(":3000", r))
}
