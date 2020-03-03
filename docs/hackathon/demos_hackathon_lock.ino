#include <ChainableLED.h>
#include <SoftwareSerial.h>

// Button
// button ports
#define BTN_LED 3
#define BTN_BUTTON 4
#define BUTTON_LED_MODE   2         // 1: toggle mode, 2: follow mode

const boolean breathMode = true;    // if or not the led lights as breath mode when it's on
int buttonState = LOW;              // the current reading from the input pin
int lastButtonState = HIGH;         // the previous reading from the input pin
int btnLedState = LOW;              // the current state of the output pin
int ledFadeValue = 0;
int ledFadeStep = 5;
unsigned int ledFadeInterval = 10;           // milliseconds
unsigned long lastDebounceTime = 0; // the last time the output pin was toggled
unsigned long debounceDelay = 50;   // the debounce time; increase if the output flickers
unsigned long lastLedFadeTime = 0;

/// RGB Leds
#define NUM_LEDS 1
// led ports
#define LED_CIN 6
#define LED_DIN 7

ChainableLED leds(LED_CIN, LED_DIN, NUM_LEDS);
int led_r = 0, led_g = 0, led_b = 0;

// RFID
// rfid serial ports
#define RFID_RX 2 // connected to port D2
#define RFID_TX 3

SoftwareSerial SoftSerial(RFID_RX, RFID_TX);
#define RFID_MAX_LENGTH 14
char rfidBuffer[RFID_MAX_LENGTH] = { 0 }; // buffer array for data received over software serial port
int indexRfid = 0;                         // counter for rfidBuffer array

// CMD received on hardware serial
#define CMD_MAX_LENGTH 32

char cmdBuffer[CMD_MAX_LENGTH]; // buffer array for data received over serial port
int indexCmd;                            // index for cmdBuffer

void setup() {
  // Button read init
  pinMode(BTN_BUTTON, INPUT);
  pinMode(BTN_LED, OUTPUT);
  digitalWrite(BTN_LED, btnLedState);
  // RGB Led init
  leds.setColorRGB(0, 64, 64, 64);
  delay(150);
  leds.setColorRGB(0, 0, 0, 0);
  // RFID init
  SoftSerial.begin(9600);
  // Serial com init
  Serial.begin(9600);
  Serial.setTimeout(10);
}

void loop() {
  // Button handling
  int reading = digitalRead(BTN_BUTTON);
  if (reading != lastButtonState) {
    lastDebounceTime = millis();
  }
  
  if ((millis() - lastDebounceTime) > debounceDelay) {
    // whatever the reading is at, it's been there for longer
    // than the debounce delay, so take it as the actual current state:

    // if the button state has changed:
    if (reading != buttonState) {
      buttonState = reading;
      Serial.print("BTN:");
      Serial.println(buttonState);
#if BUTTON_LED_MODE == 1
      if (buttonState == LOW) {  //button is pressed
          btnLedState = !btnLedState;
          ledFadeValue = 0;
          lastLedFadeTime = millis();
      }
#else
      if (buttonState == LOW) {  //button is pressed
        btnLedState = HIGH;
        ledFadeValue = 0;
        lastLedFadeTime = millis();
      } else {                   //button is released
        btnLedState = LOW;
      }
#endif
    }
  }
  buttonLedFade();
  lastButtonState = reading;

  // Command handling
  while (Serial.available() > 0) {
    char incomingByte = Serial.read();
    if (incomingByte != '\n') {
      cmdBuffer[indexCmd++] = incomingByte;
    } else {
      cmdBuffer[indexCmd] = 0;
      parseCmd(cmdBuffer);
      clearBufferArray(cmdBuffer, CMD_MAX_LENGTH);
      indexCmd = 0;
      break;
    }
  }

  // RFID handling
  while (SoftSerial.available() > 0) {
    rfidBuffer[indexRfid++] = SoftSerial.read();
    if (indexRfid == RFID_MAX_LENGTH) {
      Serial.write("RFID:");
      Serial.write(rfidBuffer + 1, indexRfid - 2); // Skip start and stop byte
      Serial.write('\n');
      clearBufferArray(rfidBuffer, RFID_MAX_LENGTH);
      indexRfid = 0;
      break;
    }
  }
}

void buttonLedFade() {
  // set the LED:
  if (breathMode && btnLedState != LOW) {
    if (millis() - lastLedFadeTime > ledFadeInterval) {
      lastLedFadeTime = millis();
      analogWrite(BTN_LED, ledFadeValue);
      ledFadeValue += ledFadeStep;
      if (ledFadeValue > 255){
        ledFadeValue = 255 - ledFadeStep;
        ledFadeStep = -ledFadeStep;
      } else if (ledFadeValue < 0) {
        ledFadeValue = 0;
        ledFadeStep = -ledFadeStep;
      }
    }
  } else {
    digitalWrite(BTN_LED, btnLedState);
  }
}

void parseCmd(char *buf) {
    char* split = strtok(buf, ":");
    while (split != NULL) {
      if (strcmp(split, "LED") == 0) {
        // we control the led, read the values
        split = strtok(NULL, ":");
        if (split != NULL) {
          led_r = atoi(split);
        }
        split = strtok(NULL, ":");
        if (split != NULL) {
          led_g = atoi(split);
        }
        split = strtok(NULL, ":");
        if (split != NULL) {
          led_b = atoi(split);
        }
        // set led values
        leds.setColorRGB(0, led_r, led_g, led_b);
      }
      // check for more arguments
      split = strtok(NULL, ":");
    }
}

void clearBufferArray(char * buf, int len) {
  for (int i = 0; i < len; i++) {
    buf[i] = 0;
  }
}
