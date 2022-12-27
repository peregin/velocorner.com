use actix_web::{web, App, HttpRequest, HttpServer, Responder};
use awc::Client;
use chrono::{DateTime, Utc};
use qstring::QString;
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, default::Default, env};
use log::info;
use lru::LruCache;
use std::num::NonZeroUsize;
use std::sync::Mutex;

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

    dbg!(
        "ratelimit-remaining={:?}",
        reply.headers().get("x-ratelimit-remaining")
    );
    reply.json::<ExchangeRate>().await.unwrap()
}

async fn welcome(_: HttpRequest) -> impl Responder {
    let now: DateTime<Utc> = Utc::now();
    format!(r#"
    Welcome to <b>exchange rate service</b>, <i>{}</i><br/>
    OS type is <i>{} {}</i>
    "#, now, env::consts::OS, env::consts::ARCH)
        .customize()
        .insert_header(("content-type", "text/html; charset=utf-8"))
}

async fn rates(req: HttpRequest, data: web::Data<Mutex<LruCache<String, ExchangeRate>>>) -> impl Responder {
    let qs = QString::from(req.query_string());
    let currency = qs.get("base").unwrap_or("CHF");

    let mut cache = data.lock().unwrap();
    let maybeRates = cache.get(currency);
    let rates = match maybeRates {
        Some(rates) => rates,
        None => {
            let rates1 = live(currency.to_string()).await;
            cache.put(currency.to_string(), rates1);
            &rates1
        }
    };

    //let rates = live(currency.to_string()).await;
    web::Json(rates)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    env_logger::init();
    let port = option_env!("SERVICE_PORT").unwrap_or("9013");
    info!("starting exchange service on port {port} ...");

    let cache: LruCache<String, ExchangeRate> = LruCache::new(NonZeroUsize::new(10).unwrap());
    let safeCache = Mutex::new(cache);
    let webCache = web::Data::new(safeCache);

    HttpServer::new(|| {
        App::new()
            .route("/", web::get().to(welcome))
            .route("/rates", web::get().to(rates))
    })
        .bind(format!("0.0.0.0:{port}"))?
        .run()
        .await
}
