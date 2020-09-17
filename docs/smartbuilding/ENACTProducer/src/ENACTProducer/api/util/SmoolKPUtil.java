
/*
 * Generated Smool KP Utility class
 */
package ENACTProducer.api.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.smool.kpi.common.Logger;

public class SmoolKPUtil {

	private static final String DEBUG_TAG = "SmoolKPUtil";
	
	/**
	 * Used to check if a remote machine is listening in a specific port.
	 *
	 * @param ip The IP to test
	 * @param port The port to test
	 * @param protocol "TCP" or "UDP"
	 * @return TRUE if machine is listening
	 */
	public static boolean isMachineReacheable(String ip, int port, String protocol) {
		
		InetSocketAddress inetAddress = new InetSocketAddress(ip, port);
		Socket clientSocketTCP = null;
		DatagramSocket clientSocketUDP = null;
		if (protocol.trim().contains("TCP")){
			try{   
				Logger.debug(DEBUG_TAG, "Testing TCP connectivity to " + ip + " on port " + port);
				clientSocketTCP= new Socket();
				clientSocketTCP.connect(inetAddress, 3000);
				Logger.debug(DEBUG_TAG, "Machine is reacheable, connection is possible");
				clientSocketTCP.close();
				return true;
			}   
			catch(IOException e){   
				Logger.error(DEBUG_TAG, "Machine with IP " + ip + " in port " + port + " is not reacheable.");
				return false;
			}
		}
		else if (protocol.trim().contains("UDP")){
			try{   
				Logger.debug(DEBUG_TAG, "Testing UDP connectivity to " + ip + " on port " + port);
				clientSocketUDP= new DatagramSocket();
				clientSocketUDP.connect(inetAddress);
				Logger.debug(DEBUG_TAG, "Machine is reacheable, connection is possible");
				clientSocketUDP.close();
				return true;
			}   
			catch(SocketException e){   
				Logger.error(DEBUG_TAG, "Machine with IP " + ip + " in port " + port + " is not reacheable.");
				return false;
			}
		}
		return false;
		
	}
	
}

