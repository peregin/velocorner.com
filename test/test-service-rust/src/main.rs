use std::sync::mpsc;
use std::thread;
use actix_web::client;

fn main() {
    let (tx, rx) = mpsc::channel();

    thread::spawn(move || {
        let val = String::from("aloha");
        tx.send(val).unwrap();
    });

    let received = rx.recv().unwrap();
    println!("Got: {}", received);

    client::get("http://www.rust-lang.org")   // <- Create request builder
        .header("User-Agent", "Actix-web")
        .finish().unwrap()
        .send()                               // <- Send http request
        .map_err(|_| ())
        .and_then(|response| {                // <- server http response
            println!("Response: {:?}", response);
            Ok(())
        });
}
