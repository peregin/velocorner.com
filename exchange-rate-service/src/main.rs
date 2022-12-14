use actix_web::{web, App, HttpRequest, HttpServer, Responder};
use awc::Client;
use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, default::Default};

#[derive(Serialize, Deserialize, Debug)]
struct ExchangeRate {
    base: String,
    rates: HashMap<String, f32>,
}

async fn live() -> ExchangeRate {
    let client = Client::default();
    let mut reply = client
        .get("https://api.exchangerate.host/latest?base=CHF")
        .insert_header(("User-Agent", "actix-web"))
        .insert_header(("Content-Type", "application/json"))
        .send()
        .await
        .unwrap();

    //let mut payload = reply.unwrap();
    println!(
        "status={:?}, ratelimit-remaining={:?}",
        reply.status(),
        reply.headers().get("x-ratelimit-remaining")
    );
    let json = reply.json::<ExchangeRate>().await.unwrap();
    println!("json = {:#?}", json);
    json
}

async fn welcome(_: HttpRequest) -> impl Responder {
    let now: DateTime<Utc> = Utc::now();
    format!("Welcome to exchange rate service, {now}")
}

async fn rates(req: HttpRequest) -> impl Responder {
    let name = req.match_info().get("name").unwrap_or("World");
    format!("Hello {}!", &name)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
            .route("/", web::get().to(welcome))
            .route("/rates", web::get().to(rates))
    })
    .bind(("localhost", 8080))?
    .run()
    .await
}
