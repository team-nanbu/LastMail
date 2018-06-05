#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <WiFiClientSecure.h>
#include <Adafruit_NeoPixel.h>

#define _NO_WIFI_

/* for IFTTT */
String makerEvent = "**********"; // Maker Webhooks
String makerKey = "*************"; // Maker Webhooks
const char* serverIFTTT = "maker.ifttt.com";  // Server URL
const char* ifttt_ca_cert = \
"-----BEGIN CERTIFICATE-----\n" \
"MIIDxTCCAq2gAwIBAgIBADANBgkqhkiG9w0BAQsFADCBgzELMAkGA1UEBhMCVVMx\n" \
"EDAOBgNVBAgTB0FyaXpvbmExEzARBgNVBAcTClNjb3R0c2RhbGUxGjAYBgNVBAoT\n" \
"EUdvRGFkZHkuY29tLCBJbmMuMTEwLwYDVQQDEyhHbyBEYWRkeSBSb290IENlcnRp\n" \
"ZmljYXRlIEF1dGhvcml0eSAtIEcyMB4XDTA5MDkwMTAwMDAwMFoXDTM3MTIzMTIz\n" \
"NTk1OVowgYMxCzAJBgNVBAYTAlVTMRAwDgYDVQQIEwdBcml6b25hMRMwEQYDVQQH\n" \
"EwpTY290dHNkYWxlMRowGAYDVQQKExFHb0RhZGR5LmNvbSwgSW5jLjExMC8GA1UE\n" \
"AxMoR28gRGFkZHkgUm9vdCBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkgLSBHMjCCASIw\n" \
"DQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAL9xYgjx+lk09xvJGKP3gElY6SKD\n" \
"E6bFIEMBO4Tx5oVJnyfq9oQbTqC023CYxzIBsQU+B07u9PpPL1kwIuerGVZr4oAH\n" \
"/PMWdYA5UXvl+TW2dE6pjYIT5LY/qQOD+qK+ihVqf94Lw7YZFAXK6sOoBJQ7Rnwy\n" \
"DfMAZiLIjWltNowRGLfTshxgtDj6AozO091GB94KPutdfMh8+7ArU6SSYmlRJQVh\n" \
"GkSBjCypQ5Yj36w6gZoOKcUcqeldHraenjAKOc7xiID7S13MMuyFYkMlNAJWJwGR\n" \
"tDtwKj9useiciAF9n9T521NtYJ2/LOdYq7hfRvzOxBsDPAnrSTFcaUaz4EcCAwEA\n" \
"AaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwHQYDVR0OBBYE\n" \
"FDqahQcQZyi27/a9BUFuIMGU2g/eMA0GCSqGSIb3DQEBCwUAA4IBAQCZ21151fmX\n" \
"WWcDYfF+OwYxdS2hII5PZYe096acvNjpL9DbWu7PdIxztDhC2gV7+AJ1uP2lsdeu\n" \
"9tfeE8tTEH6KRtGX+rcuKxGrkLAngPnon1rpN5+r5N9ss4UXnT3ZJE95kTXWXwTr\n" \
"gIOrmgIttRD02JDHBHNA7XIloKmf7J6raBKZV8aPEjoJpL1E/QYVN8Gb5DKj7Tjo\n" \
"2GTzLH4U/ALqn83/B2gX2yKQOC16jdFU8WnjXzPKej17CuPKf1855eJ1usV2GDPO\n" \
"LPAvTK33sefOT6jEm0pUBsV/fdUID+Ic/n4XuKxe9tQWskMJDE32p2u0mYRlynqI\n" \
"4uJEvlz36hz1\n" \
"-----END CERTIFICATE-----\n" \
;

/* for WiFi */
const char* ssid     = "**********";
const char* password = "***********";
const char* server = "*******************************************";
WiFiClientSecure client;

/* for LED */
#define LED_PIN 16
#define LED_NUM 5

Adafruit_NeoPixel strip = Adafruit_NeoPixel(LED_NUM, LED_PIN, NEO_GRB + NEO_KHZ800);
#define LIGHT_SETTLE_MS 5

/* for SW */
uint8_t SW_PIN = 17;
uint8_t Sw_cnt = 0;
#define SW_CNT_CONFIRM  2

/* for IFTTT */
void sendIFTTT() {

  client.setCACert(ifttt_ca_cert);

  Serial.println("\nStarting connection to server...");
  if (!client.connect(serverIFTTT, 443)) {
    Serial.println("Connection failed!");
  } else {
    Serial.println("Connected to server!");
    // Make a HTTP request:
    String url = "https://maker.ifttt.com/trigger/" + makerEvent + "/with/key/" + makerKey;
    // url += "?value1=VALUE1";
    client.println("GET " + url + " HTTP/1.1");
    client.print("Host: ");
    client.println(server);
    client.println("Connection: close");
    client.println();

    Serial.print("Waiting for response "); //WiFiClientSecure uses a non blocking implementation

    /* response phase */
    int count = 0;
    while (!client.available()) {
      delay(50); //
      Serial.print(".");

      count++;
      if (count > 20 * 20) { // about 20s
        Serial.println("(send) failed!");
        return;
      }
    }
    while (client.available()) {
      char c = client.read();
      Serial.write(c);
    }

    /* disconect phase */
    if (!client.connected()) {
      Serial.println();
      Serial.println("disconnecting from server.");
      client.stop();
    }
  }
}

/* for LED */
void startupLed(uint8_t wait) {
  int8_t loop;

  strip.setPixelColor(0, strip.Color(128, 0, 0));
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(wait);
  for( loop=1 ; loop<5 ; loop++ ) {
    strip.setPixelColor(loop, strip.Color(128, 0, 0));
    strip.setPixelColor(loop-1, 0);
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(wait);
  }

  for( loop=3 ; loop>=0 ; loop-- ) {
    strip.setPixelColor(loop, strip.Color(128, 0, 0));
    strip.setPixelColor(loop+1, 0);
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(wait);
  }
  strip.setPixelColor(0, 0);
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
}

void pushDetect() {
  int16_t i;

  for(i=0; i<LED_NUM; i++) {
//    strip.setPixelColor(i, strip.Color(255, 255, 0));
    strip.setPixelColor(i, strip.Color(128, 128, 250));
  }
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
}

#define G_1_1 255
#define G_1_2 67
#define G_1_3 53
#define G_2_1 66
#define G_2_2 133
#define G_2_3 255
#define G_3_1 52
#define G_3_2 200
#define G_3_3 83
#define G_4_1 251
#define G_4_2 200
#define G_4_3 5

void googleLED() {
  uint8_t loop;

  strip.setPixelColor(0, strip.Color(G_1_1, G_1_2, G_1_3));
  strip.setPixelColor(1, strip.Color(G_2_1, G_2_2, G_2_3));
  strip.setPixelColor(3, strip.Color(G_3_1, G_3_2, G_3_3));
  strip.setPixelColor(4, strip.Color(G_4_1, G_4_2, G_4_3));
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(1000);

  for( loop=0 ; loop<5 ; loop++) {
    strip.setPixelColor(1, strip.Color(G_1_1, G_1_2, G_1_3));
    strip.setPixelColor(3, strip.Color(G_2_1, G_2_2, G_2_3));
    strip.setPixelColor(4, strip.Color(G_3_1, G_3_2, G_3_3));
    strip.setPixelColor(0, strip.Color(G_4_1, G_4_2, G_4_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
    strip.setPixelColor(3, strip.Color(G_1_1, G_1_2, G_1_3));
    strip.setPixelColor(4, strip.Color(G_2_1, G_2_2, G_2_3));
    strip.setPixelColor(0, strip.Color(G_3_1, G_3_2, G_3_3));
    strip.setPixelColor(1, strip.Color(G_4_1, G_4_2, G_4_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
    strip.setPixelColor(4, strip.Color(G_1_1, G_1_2, G_1_3));
    strip.setPixelColor(0, strip.Color(G_2_1, G_2_2, G_2_3));
    strip.setPixelColor(1, strip.Color(G_3_1, G_3_2, G_3_3));
    strip.setPixelColor(3, strip.Color(G_4_1, G_4_2, G_4_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
    strip.setPixelColor(0, strip.Color(G_1_1, G_1_2, G_1_3));
    strip.setPixelColor(1, strip.Color(G_2_1, G_2_2, G_2_3));
    strip.setPixelColor(3, strip.Color(G_3_1, G_3_2, G_3_3));
    strip.setPixelColor(4, strip.Color(G_4_1, G_4_2, G_4_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
  }
  strip.setPixelColor(0, 0);
  strip.setPixelColor(1, 0);
  strip.setPixelColor(2, 0);
  strip.setPixelColor(3, 0);
  strip.setPixelColor(4, 0);
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(150);
}

#define R_1_1 0
#define R_1_2 0
#define R_1_3 255
#define R_2_1 0
#define R_2_2 0
#define R_2_3 150
#define R_3_1 0
#define R_3_2 0
#define R_3_3 50
#define R_4_1 0
#define R_4_2 0
#define R_4_3 25
#define R_5_1 0
#define R_5_2 0
#define R_5_3 0

void ringLED() {
  uint8_t loop;

/*
  strip.setPixelColor(0, strip.Color(0, 0, 255));
  strip.setPixelColor(1, strip.Color(0, 0, 0));
  strip.setPixelColor(2, strip.Color(0, 0, 0));
  strip.setPixelColor(3, strip.Color(0, 0, 0));
  strip.setPixelColor(4, strip.Color(0, 0, 0));
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(500);
  strip.setPixelColor(0, strip.Color(R_2_1, R_2_2, R_2_3));
  strip.setPixelColor(1, strip.Color(R_1_1, R_1_2, R_1_3));
  strip.setPixelColor(2, strip.Color(0, 0, 0));
  strip.setPixelColor(3, strip.Color(0, 0, 0));
  strip.setPixelColor(4, strip.Color(0, 0, 0));
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(150);
  strip.setPixelColor(0, strip.Color(R_3_1, R_3_1, R_3_1));
  strip.setPixelColor(1, strip.Color(R_2_1, R_2_2, R_2_3));
  strip.setPixelColor(2, strip.Color(R_1_1, R_1_2, R_1_3));
  strip.setPixelColor(3, strip.Color(0, 0, 0));
  strip.setPixelColor(4, strip.Color(0, 0, 0));
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(150);
  strip.setPixelColor(0, strip.Color(R_4_1, R_4_1, R_4_1));
  strip.setPixelColor(1, strip.Color(R_3_1, R_3_2, R_3_3));
  strip.setPixelColor(2, strip.Color(R_2_1, R_2_2, R_2_3));
  strip.setPixelColor(3, strip.Color(R_1_1, R_1_2, R_1_3));
  strip.setPixelColor(4, strip.Color(0, 0, 0));
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(150);
  strip.setPixelColor(0, strip.Color(R_5_1, R_5_1, R_5_1));
  strip.setPixelColor(1, strip.Color(R_4_1, R_4_2, R_4_3));
  strip.setPixelColor(2, strip.Color(R_3_1, R_3_2, R_3_3));
  strip.setPixelColor(3, strip.Color(R_2_1, R_2_2, R_2_3));
  strip.setPixelColor(4, strip.Color(R_1_1, R_1_2, R_1_3));
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(150);
*/
  for( loop=0 ; loop<5 ; loop++) {
    strip.setPixelColor(0, strip.Color(R_1_1, R_1_2, R_1_3));
    strip.setPixelColor(1, strip.Color(R_2_1, R_2_2, R_2_3));
    strip.setPixelColor(2, strip.Color(R_3_1, R_3_2, R_3_3));
    strip.setPixelColor(3, strip.Color(R_4_1, R_4_2, R_4_3));
    strip.setPixelColor(4, strip.Color(R_5_1, R_5_2, R_5_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
    strip.setPixelColor(1, strip.Color(R_1_1, R_1_2, R_1_3));
    strip.setPixelColor(2, strip.Color(R_2_1, R_2_2, R_2_3));
    strip.setPixelColor(3, strip.Color(R_3_1, R_3_2, R_3_3));
    strip.setPixelColor(4, strip.Color(R_4_1, R_4_2, R_4_3));
    strip.setPixelColor(0, strip.Color(R_5_1, R_5_2, R_5_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
    strip.setPixelColor(2, strip.Color(R_1_1, R_1_2, R_1_3));
    strip.setPixelColor(3, strip.Color(R_2_1, R_2_2, R_2_3));
    strip.setPixelColor(4, strip.Color(R_3_1, R_3_2, R_3_3));
    strip.setPixelColor(0, strip.Color(R_4_1, R_4_2, R_4_3));
    strip.setPixelColor(1, strip.Color(R_5_1, R_5_2, R_5_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
    strip.setPixelColor(3, strip.Color(R_1_1, R_1_2, R_1_3));
    strip.setPixelColor(4, strip.Color(R_2_1, R_2_2, R_2_3));
    strip.setPixelColor(0, strip.Color(R_3_1, R_3_2, R_3_3));
    strip.setPixelColor(1, strip.Color(R_4_1, R_4_2, R_4_3));
    strip.setPixelColor(2, strip.Color(R_5_1, R_5_2, R_5_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
    strip.setPixelColor(4, strip.Color(R_1_1, R_1_2, R_1_3));
    strip.setPixelColor(0, strip.Color(R_2_1, R_2_2, R_2_3));
    strip.setPixelColor(1, strip.Color(R_3_1, R_3_2, R_3_3));
    strip.setPixelColor(2, strip.Color(R_4_1, R_4_2, R_4_3));
    strip.setPixelColor(3, strip.Color(R_5_1, R_5_2, R_5_3));
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(150);
  }
  strip.setPixelColor(0, 0);
  strip.setPixelColor(1, 0);
  strip.setPixelColor(2, 0);
  strip.setPixelColor(3, 0);
  strip.setPixelColor(4, 0);
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(100);
}

void ledWave(uint8_t red, uint8_t green, uint8_t blue, uint8_t wait) {
  int16_t j;
  int16_t i;
  int16_t wred, wgreen, wblue;

  wred = wgreen = wblue = 0;
  for(j=0; j<50; j++) {
    if( red > wred )     wred += 5;
    if( green > wgreen ) wgreen += 5;
    if( blue > wblue )   wblue += 5;

    for(i=0; i<LED_NUM; i++) {
      strip.setPixelColor(i, strip.Color(wred, wgreen, wblue));
//      delay(LIGHT_SETTLE_MS);
    }
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(wait);
  }
  for(j=0; j<20; j++) {
    delay(50);
  }
  for(j=0; j<50; j++) {
    if( 0 < wred )   wred -= 5;
    if( 0 < wgreen ) wgreen -= 5;
    if( 0 < wblue )  wblue -= 5;

    for(i=0; i<LED_NUM; i++) {
      strip.setPixelColor(i, strip.Color(wred, wgreen, wblue));
//      delay(LIGHT_SETTLE_MS);
    }
    portDISABLE_INTERRUPTS();
    strip.show();
    portENABLE_INTERRUPTS();
    delay(wait);
  }
  delay(1000);
  strip.setPixelColor(0, 0);
  strip.setPixelColor(1, 0);
  strip.setPixelColor(2, 0);
  strip.setPixelColor(3, 0);
  strip.setPixelColor(4, 0);
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  delay(100);
}

void ledOff() {
  int16_t i;

  for(i=0; i<LED_NUM; i++) {
    strip.setPixelColor(i, 0);
  }
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
}

void cloudKick() {

  Serial.println("\nStarting connection to server...");
  if (!client.connect(server, 443))
    Serial.println("Connection failed!");
  else {
    Serial.println("Connected to server!");
    // Make a HTTP request:
    client.println("GET https:*****************************/**** HTTP/1.0");
    client.println("Host: **************************");
    client.println("Connection: close");
    client.println();

    while (client.connected()) {
      String line = client.readStringUntil('\n');
      if (line == "\r") {
        Serial.println("headers received");
        break;
      }
    }
    while (client.available()) {
      char c = client.read();
      Serial.write(c);
    }
    client.stop();
  }
}

void setup() {
  Serial.begin(115200);
  delay(100);

  /* for SW */
  pinMode(SW_PIN, INPUT);

#ifndef _NO_WIFI_
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
#endif

  /* for LED */
  strip.begin();
  strip.setBrightness(250);
  portDISABLE_INTERRUPTS();
  strip.show();
  portENABLE_INTERRUPTS();
  startupLed(150);
//  ledOff();

  Serial.println("WiFi connected"); Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  delay(1000);
}

/* for Hall sensor */
uint8_t hs_timing = 0;
uint8_t hs_status = 0;
uint8_t hs_cht = 0;

void loop() {
  uint8_t r_sw;
  int halls;

  hs_timing++;
  if( hs_timing > 3 ) {
    halls = hallRead();
//    Serial.println(halls,DEC);
    hs_timing = 0;
    if( halls < 20 ) {
      hs_cht++;
      if( hs_cht >= 2 ) {
        if( hs_status == 0 ) {
          Serial.println(halls,DEC);
          hs_status = 1;
          ledWave(255,255,255,10);
        }
      }
    }
    else {
      hs_cht = 0;
      hs_status = 0;
    }
  }

  r_sw = digitalRead(SW_PIN);
  if( r_sw == 0 ) {
//    Serial.println("switch detect");
    Sw_cnt++;
    if( Sw_cnt >= SW_CNT_CONFIRM ) {
      pushDetect();
#ifndef _NO_WIFI_
      cloudKick();
//      sendIFTTT();
#endif
      ledOff();
      Sw_cnt = 0;
      if( hs_status == 1 ) {
        ringLED();
      }
      else {
        googleLED();
      }
//      ledOff();
    }
  }
  delay(100);
}

