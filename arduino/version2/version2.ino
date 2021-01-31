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


// Initialize the Ethernet server library
// with the IP address and port you want to use
// (port 80 is default for HTTP):
EthernetServer server(80);
boolean alreadyConnected = false; // whether or not the client was connected previously

//SoftwareSerial mySerial(5, 6); // RX, TX

long lastReadingTime = 0;

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

  // start listening for clients
  server.begin();

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
  //pinMode( 2, INPUT ); // force it?
  // check for a reading no more than once a second.
  if (millis() - lastReadingTime > 3000) {
    // if there's a reading ready, read it:
    // don't do anything until the data ready pin is high:
    //Serial.println(F("Getting reading"));
    getBuffer();
    // timestamp the last time you got a reading:
    lastReadingTime = millis();
    // print the current readings, in HTML format:
    //Serial.write(databuffer, 35);
    Serial.println();
  }
  // listen for incoming clients
  EthernetClient client = server.available();
  if (client) {
    //Serial.println("client true");
    client.setConnectionTimeout(100);  // set the timeout duration for client.connect() and client.stop()
    //Serial.println(F("Got a client"));
    // an http request ends with a blank line
    boolean currentLineIsBlank = true;
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        // if you've gotten to the end of the line (received a newline
        // character) and the line is blank, the http request has ended,
        // so you can send a reply
        if (c == '\n' && currentLineIsBlank) {
          // send a standard http response header
          client.write(HTTP_HEADER, 16);
          client.write(CONTENT_TYPE, 46);
          client.write(CONNECTION_CLOSE, 18); // connection closed completion of response
          client.println();
          client.write(RESULT_START, 6);
          client.write(databuffer, 35);
          client.write(RESULT_END, 2);
          break;
        }
        if (c == '\n') {
          // you're starting a new line
          currentLineIsBlank = true;
        } else if (c != '\r') {
          // you've gotten a character on the current line
          currentLineIsBlank = false;
        }
      }
    }
    // give the web browser time to receive the data
    delay(1);
    // close the connection:
    client.stop();
  }
}
