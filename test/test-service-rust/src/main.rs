use std::iter::Map;
use std::sync::mpsc;
use std::thread;
use awc::Client;
use serde::{Serialize, Deserialize};

#[derive(Serialize, Deserialize, Debug)]
struct Response {
    base: String,
    //rates: Map<String, String>
}

#[actix_web::main]
async fn main() {
    let (tx, rx) = mpsc::channel();
    thread::spawn(move || {
        let val = String::from("aloha");
        tx.send(val).unwrap();
    });
    let received = rx.recv().unwrap();
    println!("Got: {received}");

    let client = Client::default();
    let reply = client.get("https://api.exchangerate.host/latest")
        .insert_header(("User-Agent", "actix-web"))
        .insert_header(("Content-Type", "application/json"))
        .send()                               // <- Send http request
        .await;

    let mut payload = reply.unwrap();
    println!("status={:?}, ratelimit-remaining={:?}", payload.status(), payload.headers().get("x-ratelimit-remaining"));
    let json = payload.json::<Response>().await;
    println!("json = {:?}", json);
}
