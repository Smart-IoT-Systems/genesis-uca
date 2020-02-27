#include <ChainableLED.h>
#define NUM_LEDS 1
#include <SoftwareSerial.h>

// LED ports
#define LED_CIN 6
#define LED_DIN 7

// RFID serial ports
// connected to port D2
#define RFID_RX 2
#define RFID_TX 3

// button ports
// SIG1: LED
// SIG2: button press
#define BTN_SIG1 4
#define BTN_SIG2 5

// LED init
ChainableLED leds(LED_CIN, LED_DIN, NUM_LEDS);

// RIFD reader
SoftwareSerial SoftSerial(RFID_RX, RFID_TX);
unsigned char buffer[64];       // buffer array for data receive over serial port
int count = 0;                    // counter for buffer array

void setup() {
  // RFID init
  SoftSerial.begin(9600);
  Serial.begin(9600);
  pinMode(BTN_SIG1, OUTPUT);
  pinMode(BTN_SIG2, INPUT);
}

int btn_state = 1, btn_state_tmp = 1;
int led_r = 0, led_g = 0, led_b = 0;
char cmd[30];
void loop() {
  // Button handling
  btn_state_tmp = digitalRead(BTN_SIG2);
  if (btn_state_tmp != btn_state) {
    btn_state = btn_state_tmp;
    Serial.print("BTN:");
    Serial.println(btn_state);
  }

  // command handling
  if (Serial.available() > 0) {
    // read cmd into buffer
    int size = Serial.readBytes(cmd, 31);
    // null terminate if too short
    cmd[size] = 0;

    char* split = strtok(cmd, ":");
    while (split != 0) {
      if (strcmp(split, "LED") == 0) {
        // we control the led, read the values
        split = strtok(0, ":");
        if (split != 0) {
          led_r = atoi(split);
        }
        split = strtok(0, ":");
        if (split != 0) {
          led_g = atoi(split);
        }
        split = strtok(0, ":");
        if (split != 0) {
          led_b = atoi(split);
        }

        // set led from read cmds
        leds.setColorRGB(0, led_r, led_g, led_b);
      }
      // check for more commands
      split = strtok(0, ":");
    }
  }

  // RFID handling
  if (SoftSerial.available()) {
    while (SoftSerial.available()) {
      buffer[count++] = SoftSerial.read();
      if (count == 64)break;
    }
    Serial.write("RFID:");
    Serial.write(buffer, count);
    Serial.write('\n');
    clearBufferArray();
    count = 0;
  }

  delay(100);
}

void clearBufferArray() {
  for (int i = 0; i < count; i++) {
    buffer[i] = 0;
  }
}
