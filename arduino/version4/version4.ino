#include <b64.h>
#include <HttpClient.h>

//#include <SoftwareSerial.h>
#include <SPI.h>
#include <Ethernet.h>
#include <EthernetClient.h>

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

// Name of the server we want to connect to
const char kHostname[] = "35.225.243.115";
// Path to download (this is the bit after the hostname in the URL
// that you want to download
const char kPath[] = "/api/data?s=";

// Number of milliseconds to wait without receiving any data before we give up
const int kNetworkTimeout = 30*1000;
// Number of milliseconds to wait if no data is available before trying again
const int kNetworkDelay = 1000;

//SoftwareSerial mySerial(5, 6); // RX, TX

unsigned long lastReadingTime = 0;              // last time weather data received
long lastConnectionTime = 0;           // last time you connected to the server, in milliseconds
const unsigned long postingInterval = 60000;  // delay between updates, in milliseconds

char databuffer[35];
char mydatabuffer[35] = "c180s003g006t063r000p000h88b10038";

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
  lastConnectionTime = millis() - 290000;
}

void loop() {
   if (millis() - lastReadingTime > 3000) {
    // if there's a reading ready, read it:
    // don't do anything until the data ready pin is high:
    //Serial.println(F("Getting reading"));
    getBuffer();
    // timestamp the last time you got a reading:
    lastReadingTime = millis();
    // print the current readings, in HTML format:
    //Serial.write(databuffer, 35);
    databuffer[35]='\0';
    Serial.println();
  }
  
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
    lastConnectionTime = millis();
  }
}


// this method makes a HTTP connection to the server:
void httpRequest() {
  int err =0;
  // Serial.println(databuffer);
    HttpClient http(client);
    char buf[47] = "";
    strcpy(buf, kPath);
    strcpy(buf + strlen(kPath), databuffer);

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
}
