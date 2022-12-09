use std::sync::mpsc;
use std::thread;
use awc::Client;

#[actix_web::main]
async fn main() {
    let (tx, rx) = mpsc::channel();

    thread::spawn(move || {
        let val = String::from("aloha");
        tx.send(val).unwrap();
    });

    let received = rx.recv().unwrap();
    println!("Got: {}", received);
    let client = Client::default();
    let res = client.get("http://www.rust-lang.org")   // <- Create request builder
        .insert_header(("User-Agent", "Actix-web"))
        .send()                               // <- Send http request
        .await;

    println!("res = {:?}", res);
}
