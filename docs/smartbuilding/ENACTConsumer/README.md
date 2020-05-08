# ENACT Consumer

Template for the *smart building* IoT app using SMOOL middleware.

This app reads sensor data, for example, temperature. When these values pass comfort values, an actuation order is sent via SMOOL, therefore all the actuators listening for SMOOL messages could perform actions, for instance, lower the blinds.

## Requisites

- Java 8 or upper must be installed

## Usage

Normal execution (use the .sh file or execute the following command):

```sh
java -cp bin:lib/* ENACTConsumer.logic.ConsumerMain
```

Optionally, to start on different SMOOL server than the default, use:

```sh
java -cp bin:lib/* ENACTConsumer.logic.ConsumerMain sibName server.address port
```

## Usage (for insecure actuation detection scenario)

To send non secure actuation orders, execute the following command:

```sh
java -cp bin:lib/* -Dinsecure="true" ENACTConsumer.logic.ConsumerMain
```

The app will send SMOOL actuation orders but without the security concepts. The *Security Agent* listening SMOOL data will detect these orders and send event to the security monitoring tool.

