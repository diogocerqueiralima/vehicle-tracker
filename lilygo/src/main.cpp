#include <HardwareSerial.h>

HardwareSerial SerialAT(1); // Serial1 para o módulo

void setup() {
  Serial.begin(115200);                  // Monitor Serial
  SerialAT.begin(115200, SERIAL_8N1, 26, 27); // RX=26, TX=27 do SIM7600

  Serial.println("Terminal AT iniciado.");
  Serial.println("Digite comandos AT e pressione ENTER.");
}

void loop() {
  // 1️⃣ Ler comando do monitor serial
  if (Serial.available()) {
    String comando = Serial.readStringUntil('\n');
    comando.trim();
    if (comando.length() > 0) {
      SerialAT.println(comando); // envia para o modem
    }
  }

  // 2️⃣ Ler resposta do modem
  while (SerialAT.available()) {
    String resposta = SerialAT.readStringUntil('\n');
    resposta.trim();
    Serial.println(resposta); // mostra no monitor serial
  }
}
