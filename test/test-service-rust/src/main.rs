use std::sync::mpsc;
use std::thread;
use awc::Client;
use serde::{Serialize, Deserialize};
use std::{default::Default, collections::HashMap};

#[derive(Serialize, Deserialize, Debug)]
struct Response {
    base: String,
    rates: HashMap<String, f32>
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
    let mut reply = client.get("https://api.exchangerate.host/latest")
        .insert_header(("User-Agent", "actix-web"))
        .insert_header(("Content-Type", "application/json"))
        .send()                               // <- Send http request
        .await
        .unwrap();

    //let mut payload = reply.unwrap();
    println!("status={:?}, ratelimit-remaining={:?}", reply.status(), reply.headers().get("x-ratelimit-remaining"));
    let json = reply.json::<Response>().await.unwrap();
    println!("json = {:#?}", json);
}
