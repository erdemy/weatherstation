//#include <SoftwareSerial.h>
#include <SPI.h>
#include <Ethernet.h>

// assign a MAC address for the Ethernet controller.
// fill in your address here:
byte mac[] = {
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED
};
// assign an IP address for the controller:
IPAddress ip(192, 168, 1, 5);
IPAddress myDns(192, 168, 0, 1);

// initialize the library instance:
EthernetClient client;

char server[] = "localhost";  // also change the Host line in httpRequest()
//IPAddress server(64,131,82,241);

boolean alreadyConnected = false; // whether or not the client was connected previously

//SoftwareSerial mySerial(5, 6); // RX, TX

unsigned long lastConnectionTime = 0;           // last time you connected to the server, in milliseconds
const unsigned long postingInterval = 10*1000;  // delay between updates, in milliseconds

char databuffer[35];

const char HTTP_HEADER[] = "HTTP/1.1 200 OK\n";
const char CONTENT_TYPE[] = "Content-Type: application/json; charset=utf-8\n";
const char CONNECTION_CLOSE[] = "Connection: close\n";
const char RESULT_START[] = "{\"r\":\"";
const char RESULT_END[] = "\"}";

void getBuffer() //Get weather status data
{
  int index;
  for (index = 0; index < 35; index ++)
  {
    if (Serial.available())
    {
      databuffer[index] = Serial.read();
      if (databuffer[0] != 'c')
      {
        index = -1;
      }
    }
    else
    {
      index --;
    }
  }
  Serial.flush();
}

void setup() {
  // You can use Ethernet.init(pin) to configure the CS pin
  //Ethernet.init(10);  // Most Arduino shields
  //Ethernet.init(5);   // MKR ETH shield
  //Ethernet.init(0);   // Teensy 2.0
  //Ethernet.init(20);  // Teensy++ 2.0
  //Ethernet.init(15);  // ESP8266 with Adafruit Featherwing Ethernet
  //Ethernet.init(33);  // ESP32 with Adafruit Featherwing Ethernet

  //SPI.begin();
  // disable the SD card by switching pin 4 High
  pinMode(4, OUTPUT);
  digitalWrite(4, HIGH);

  // start the Ethernet connection
  Ethernet.begin(mac, ip);

  // Open serial communications and wait for port to open:
  Serial.begin(9600);


  while (!Serial && millis() < 30000) {
  ; // wait for serial port to connect. Needed for native USB port only
  }

  // give the sensor and Ethernet shield time to set up:
  delay(2000);


  // Check for Ethernet hardware present
  if (Ethernet.hardwareStatus() == EthernetNoHardware) {
    Serial.println(F("Ethernet shield was not found."));
    while (true) {
      delay(1); // do nothing, no point running without Ethernet hardware
    }
  }
  if (Ethernet.linkStatus() == LinkOFF) {
    Serial.println(F("Ethernet cable is not connected."));
  }
}

void loop() {
  // if there's incoming data from the net connection.
  // send it out the serial port.  This is for debugging
  // purposes only:
  if (client.available()) {
    char c = client.read();
    Serial.write(c);
  }

  // if ten seconds have passed since your last connection,
  // then connect again and send data:
  if (millis() - lastConnectionTime > postingInterval) {
    httpRequest();
  }
  // give the web browser time to receive the data
  delay(1);
}


// this method makes a HTTP connection to the server:
void httpRequest() {
  // close any connection before send a new request.
  // This will free the socket on the WiFi shield
  client.stop();

  // if there's a successful connection:
  if (client.connect(server, 80)) {
    Serial.println("connecting...");
    // send the HTTP GET request:
    client.println("GET /api/data?s=c180s003g006t063r000p000h88b10038 HTTP/1.1");
    client.println("Host: localhost");
    client.println("User-Agent: arduino-ethernet");
    client.println("Connection: close");
    client.println();

    // note the time that the connection was made:
    lastConnectionTime = millis();
  } else {
    // if you couldn't make a connection:
    Serial.println("connection failed");
  }
}
