package ENACTConsumer.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ENACTConsumer.api.SmoolKP;
import ENACTConsumer.model.smoolcore.impl.BlindPositionActuator;
import ENACTConsumer.model.smoolcore.impl.ContinuousInformation;
import ENACTConsumer.model.smoolcore.impl.SecurityAuthorization;

/**
 * Example of sending back to SMOOL am actuation message with security data.
 * 
 * <p>
 * Once a temperature value is reached, the temperature Observer calls this
 * class. Then, a new Actuation message on blinds is sent. To prevent other
 * clients to send actuation orders, this message is sent with security content.
 * </P>
 */
class CustomActuation {
	private boolean isFirstActuation = true;
	private String kpName;
	private String name;
	private double val = 1.0;
	private BlindPositionActuator actuator;
	private ContinuousInformation blindPos;
	private int counter=0;	
	
	//hard code the JWT token
	private final String token = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJFbmFjdENvbnN1bWVyIiwib2JqIjoiQmxpbmRQb3NpdGlvbkFjdHVhdG9yIiwiYWN0Ijoid3JpdGUiLCJpYXQiOjE1OTc3NTE1NzEsImV4cCI6MTU5Nzc1MjE3MX0.";
	
	//this token is a fake one just for testing purpose!
//	private final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTSU5URUYiLCJpYXQiOjE1OTgxOTE3NzQsImV4cCI6MTYyOTcyNzc3NCwiYXVkIjoiU01PT0wiLCJzdWIiOiJTbWFydEVuZXJneUFwcCIsIm9iaiI6IkJsaW5kUG9zaXRpb25BY3R1YXRvciIsImFjdCI6IndyaXRlIn0.wHSRvyvHrCU4PeD0llz-rMCt8BwKZrby4BxV-Qc_vWU";
	/**
	 * 	http://jwtbuilder.jamiekurtz.com/
{
 typ: "JWT",
 alg: "HS256"
}.
{
 iss: "SINTEF",
 iat: 1598191774,
 exp: 1629727774,
 aud: "SMOOL",
 sub: "SmartEnergyApp",
 obj: "BlindPositionActuator",
 act: "write"
}.
[signature]
	 * 
	 */
	
	public CustomActuation(String kpName) {
		this.kpName = kpName;
		this.name = kpName + "_blindActuator";
		actuator = new BlindPositionActuator(name + "_actuator");
		blindPos = new ContinuousInformation(name + "_blindPosition");	
	}

	public synchronized void run(double temp) {
		
		try {
			// ARF: see commit c8e62a47 to understand why a thread is created to avoid KP
			// locked when retrieving another message at the same time
			// and now, the KP could send two messages at the same time and the
			// channelReader at SIB could mess the messages and then raise a SAX exception
			// - one message is the SSAP to return confirm of the incoming message that
			// triggered the observer
			// - the other message is this one sending the actuation
			// since a message can have several packets and they can arrive at different
			// time, the tcp will order the messages but the buffer could be filled by one
			// part and then the other part of the message.
			// Therefore, this mini-sleep is to prevent this ssap message to arrive at the
			// same time that the confirm message is sent

			// Note, the real solution should be to accept only the TCP ack flag sent by kp
			// and sib instead of the ssap confirm (the ack of the last segment. Otherwise,
			// a solution with message queues will lead to blocking times

			Thread.sleep(100);
			SecurityAuthorization sec;
			sec = new SecurityAuthorization(name + "_security"+Integer.toString(counter++));
			sec.setType("JWT + CASBIN payload");
			System.out.println("Sending ACTUATION order because temp is " + temp);
			if (isFirstActuation) {

				// if started as insecure, do not send credentials (>java -Dinsecure app.jar)
				if (System.getProperty("insecure") != null) {
					// IMPORTANT, send data = "" instead of null since the global list of props must
					// be sent and if old BlindSobscriptions where created with that triple and then
					// with new insecure requests the data triple is not sent, the subscriptors
					// crash and cannot reconnect until smool server is restarted.
					sec.setData("");
				} else {
					//String token = HTTPS.post("https://localhost:8443/jwt", "{\"id\":\""+kpName+"\"}");
					//String token = getTokenFromCLI(security_command, null);
					sec.setData(token);
				}

				blindPos.setValue(val).setSecurityData(sec).setTimestamp(Long.toString(System.currentTimeMillis()));
				actuator.setBlindPos(blindPos);

				SmoolKP.getProducer().createBlindPositionActuator(name, kpName, "TECNALIA", null, blindPos, null);
				isFirstActuation = false;

			} else {
				val = val + 1; // WARNING: if val is the same as latest val, the message is not sent
				blindPos.setValue(val); // we can also send temp, so the SCADA will contain the temp-to-blind rule
				//String token = HTTPS.post("https://localhost:8443/jwt", "{\"id\":\""+kpName+"\"}");
				sec.setData(token).setTimestamp(Long.toString(System.currentTimeMillis()));
				blindPos.setSecurityData(sec).setTimestamp(Long.toString(System.currentTimeMillis()));
				SmoolKP.getProducer().updateBlindPositionActuator(name, kpName, "TECNALIA", null, blindPos, null);
			}
		} catch (Exception e) {
			System.out.println("Error: the actuation order cannot be sent. " + e.getMessage());
		}
	}
	
	/**
	 * OPTIONAL, use this method to hide security aspects from source code.
	 * Install the security application as CLI (or use the existing GPG or similar commands)
	 * and execute the command from here.
	 * @throws IOException
	 */
	public String getTokenFromCLI(String command, String[] args) throws IOException {
		Process process = new ProcessBuilder(command).start();
		InputStreamReader isr = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		String line;
		StringBuffer sb=new StringBuffer();
		while ((line = br.readLine()) != null)  sb.append(line+"\n");
		return sb.toString();
	}
}