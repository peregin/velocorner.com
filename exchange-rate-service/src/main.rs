use actix_web::get;
use actix_web::{web, App, HttpRequest, HttpResponse, HttpServer, Responder};
use awc::Client;
use cached::proc_macro::cached;
use chrono::{DateTime, Utc};
use log::info;
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, default::Default, env};

const HOST: &str = "https://api.frankfurter.app";


// structure for the exchangerate.host/frankfurter.app (same for both) response and exposed on rates endpoint as well
#[derive(Serialize, Deserialize, Debug, Clone)]
struct ExchangeRate {
    base: String,
    rates: HashMap<String, f32>,
}

#[cached(time = 3600)]
async fn rates_of(base: String) -> ExchangeRate {
    let client = Client::default();
    let mut reply = client
        .get(format!("{}/latest?from={}", HOST, base))
        .insert_header(("User-Agent", "actix-web"))
        .insert_header(("Content-Type", "application/json"))
        .send()
        .await
        .unwrap();
    let reply = reply.json::<ExchangeRate>().await.unwrap();
    info!("base={:#?}, {:#?} rates", base, reply.rates.keys().len());
    reply
}

// map of ISO3 code -> description
#[cached(time = 3600)]
async fn symbols() -> HashMap<String, String> {
    let client = Client::default();
    let mut reply = client
        .get(format!("{}/currencies", HOST))
        .insert_header(("User-Agent", "actix-web"))
        .insert_header(("Content-Type", "application/json"))
        .send()
        .await
        .unwrap();
    reply.json::<HashMap<String, String>>().await.unwrap()
}

// root path, simple welcome message
async fn welcome(_: HttpRequest) -> impl Responder {
    let now: DateTime<Utc> = Utc::now();
    format!(
        r#"
    Welcome to <b>exchange rate service</b>, <i>{}</i><br/>
    OS type is <i>{} {}</i>
    "#,
        now,
        env::consts::OS,
        env::consts::ARCH
    )
        .customize()
        .insert_header(("content-type", "text/html; charset=utf-8"))
}

#[get("/rates/currencies")]
async fn currencies() -> impl Responder {
    web::Json(symbols().await.keys().cloned().collect::<Vec<_>>())
}

#[get("/rates/{base}")]
async fn rates(info: web::Path<String>) -> impl Responder {
    let base = info.into_inner().to_uppercase();
    let exchanges = rates_of(String::from(base)).await;
    web::Json(exchanges)
}

#[get("/rates/{base}/{counter}")]
async fn rate(info: web::Path<(String, String)>) -> HttpResponse {
    let info = info.into_inner();
    let base = info.0.to_uppercase();
    let counter = info.1.to_uppercase();
    let exchanges = rates_of(String::from(base)).await;
    match exchanges.rates.get(&counter) {
        Some(fx) => HttpResponse::Ok().json(fx),
        None => HttpResponse::NotFound().finish(),
    }
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    env_logger::init();
    let port = option_env!("SERVICE_PORT").unwrap_or("9012");
    info!("starting exchange service on port {port} ...");

    HttpServer::new(|| {
        App::new()
            .route("/", web::get().to(welcome))
            .service(currencies)
            .service(rate)
            .service(rates)
    })
        .bind(format!("0.0.0.0:{port}"))?
        .run()
        .await
}
