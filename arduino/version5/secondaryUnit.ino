//#include <b64.h>
//#include <HttpClient.h>
#include <ArduinoHttpClient.h>

//#include <SoftwareSerial.h>
#include <SPI.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(8, 7); // RX, TX

// assign a MAC address for the Ethernet controller.
// fill in your address here:
byte mac[] = {
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED
};
// assign an IP address for the controller:
IPAddress ip(192, 168, 1, 6);
IPAddress myDns(192, 168, 0, 1);

// initialize the library instance:
EthernetClient client;

// Name of the server we want to connect to
const char kHostname[] = "35.225.243.115";
// Path to download (this is the bit after the hostname in the URL
// that you want to download
const char kPath[] = "/api/json?v=";

HttpClient httpClient = HttpClient(client, kHostname, 2828);

// Number of milliseconds to wait without receiving any data before we give up
const int kNetworkTimeout = 30*1000;
// Number of milliseconds to wait if no data is available before trying again
const int kNetworkDelay = 1000;

//SoftwareSerial mySerial(5, 6); // RX, TX

unsigned long lastReadingTime = 0;              // last time weather data received
long lastConnectionTime = 0;           // last time you connected to the server, in milliseconds
const unsigned long postingInterval = 60000;  // delay between updates, in milliseconds

String Data = "", lastData = "";
char databuffer[35];

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

  // set the data rate for the SoftwareSerial port
  mySerial.begin(9600);

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
  lastConnectionTime = millis() - 290000;
}

void loop() {
  while (mySerial.available())
  {
      char character = mySerial.read(); // Receive a single character from the software serial port
      Data.concat(character); // Add the received character to the receive buffer
      if (character == '\n')
      {
          Serial.print("Received: ");
          Serial.println(Data);

          // Add your code to parse the received line here....

          // Clear receive buffer so we're ready to receive the next line
          if (Data.startsWith("{\"rt\":0", 0)) {
            lastData = Data;
          }
          Data = "";
      }
  }
/*
   if (millis() - lastReadingTime > 3000) {
    // if there's a reading ready, read it:
    // don't do anything until the data ready pin is high:
    //Serial.println(F("Getting reading"));
    //getBuffer();
    // timestamp the last time you got a reading:
    lastReadingTime = millis();
    // print the current readings, in HTML format:
    //Serial.write(Data);
    Serial.println(Data);
  }
  */
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
    if (lastData != "") {
      httpRequest();
      Serial.println(F("sending data"));
      Serial.println(lastData);
      lastConnectionTime = millis();
    }
  }
}


// this method makes a HTTP connection to the server:
void httpRequest() {
  int err =0;
  // Serial.println(databuffer);
  //  HttpClient http(client);
  /*
    char buf[lastData.length() + 13] = "";
    strcpy(buf, kPath);
    lastData.replace("\"","%22");
    strcpy(buf + strlen(kPath), lastData.c_str());

Serial.println(buf);*/
  String contentType = "application/json";

  httpClient.post("/api/json", contentType, lastData);

  // read the status code and body of the response
  int statusCode = httpClient.responseStatusCode();
  String response = httpClient.responseBody();

  Serial.print("Status code: ");
  Serial.println(statusCode);
  Serial.print("Response: ");
  Serial.println(response);


/*
    err = http.get(kHostname, 2828, buf);
    if (err == 0)
    {
      err = http.responseStatusCode();
      if (err >= 0)
      {
        Serial.print(F("StatusCode: "));
        Serial.println(err);

        // Usually you'd check that the response code is 200 or a
        // similar "success" code (200-299) before carrying on,
        // but we'll print out whatever response we get

        err = http.skipResponseHeaders();
        if (err >= 0)
        {
          int bodyLen = http.contentLength();
          Serial.println(F("Body:"));

          // Now we've got to the body, so we can print it out
          unsigned long timeoutStart = millis();
          char c;
          // Whilst we haven't timed out & haven't reached the end of the body
          while ( (http.connected() || http.available()) &&
                 ((millis() - timeoutStart) < kNetworkTimeout) )
          {
              if (http.available())
              {
                  c = http.read();
                  // Print out this character
                  Serial.print(c);

                  bodyLen--;
                  // We read something, reset the timeout counter
                  timeoutStart = millis();
              }
              else
              {
                  // We haven't got any data, so let's pause to allow some to
                  // arrive
                  delay(kNetworkDelay);
              }
          }
        }
        else
        {
          Serial.print(F("Failed to skip response headers: "));
          Serial.println(err);
        }
      }
      else
      {
        Serial.print(F("Getting response failed: "));
        Serial.println(err);
      }
    }
    else
    {
      Serial.print(F("Connect failed: "));
      Serial.println(err);
    }
    http.stop();
    */
}