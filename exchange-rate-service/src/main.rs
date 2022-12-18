use actix_web::{web, App, HttpRequest, HttpServer, Responder};
use awc::Client;
use chrono::{DateTime, Utc};
use qstring::QString;
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, default::Default};
use log::info;

#[derive(Serialize, Deserialize, Debug)]
struct ExchangeRate {
    base: String,
    rates: HashMap<String, f32>,
}

async fn live(base: String) -> ExchangeRate {
    let client = Client::default();
    let mut reply = client
        .get(format!(
            "https://api.exchangerate.host/latest?base={}",
            base
        ))
        .insert_header(("User-Agent", "actix-web"))
        .insert_header(("Content-Type", "application/json"))
        .send()
        .await
        .unwrap();

    //let mut payload = reply.unwrap();
    dbg!(
        "ratelimit-remaining={:?}",
        reply.headers().get("x-ratelimit-remaining")
    );
    reply.json::<ExchangeRate>().await.unwrap()
}

async fn welcome(_: HttpRequest) -> impl Responder {
    let now: DateTime<Utc> = Utc::now();
    format!("Welcome to <b>exchange rate service</b>, <i>{now}</i>")
        .customize()
        .insert_header(("content-type", "text/html; charset=utf-8"))
}

async fn rates(req: HttpRequest) -> impl Responder {
    let qs = QString::from(req.query_string());
    let currency = qs.get("base").unwrap_or("CHF");
    let rates = live(String::from(currency)).await;
    web::Json(rates)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    env_logger::init();
    info!("starting exchange service...");

    HttpServer::new(|| {
        App::new()
            .route("/", web::get().to(welcome))
            .route("/rates", web::get().to(rates))
    })
    .bind("127.0.0.1:9012")?
    .run()
    .await
}
