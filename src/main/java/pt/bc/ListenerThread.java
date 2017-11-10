package pt.bc;

import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ListenerThread extends Thread {
	protected MulticastSocket socket = null;
	protected String network;
	protected String hostAddress;
	private boolean isRunning = true;

	public ListenerThread(String name, String hostAddress, String network) throws IOException {
		super(name);
		this.socket = new MulticastSocket(40000);
		socket.setReuseAddress(true);
		this.hostAddress = hostAddress;
		this.network = network;
	}

	public void run() {
		try {
			prepare();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.printf("Failed to listener @%s -> %s\n", hostAddress, network);
			return;
		}
		startPrint();

		while (isRunning) {
			try {
				byte[] buf = new byte[32];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);

				socket.receive(packet);

				System.out.printf("%s@%s got -> Packet w/ %s size\n", getName(), hostAddress, buf.length);

				InetAddress peerAdress = packet.getAddress();

				//hack
				if (!Agent.getAgentIps().contains(peerAdress.getHostAddress())) {
					int port = packet.getPort();

					byte[] response = solveChallenge(buf);

					DatagramPacket responsePacket = new DatagramPacket(response, response.length, peerAdress, port);

					DatagramSocket responseSocket = new DatagramSocket();
					System.out.printf("%s@%s -> Solved the challenge, sending response\n", getName(), hostAddress);
					responseSocket.send(responsePacket);
				}

			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}

	public void kill() {
		isRunning = false;
	}

	private void prepare() throws IOException {
		InetAddress address = InetAddress.getByName(network);
		socket.joinGroup(address);
	}

	private void startPrint() {
		System.out.printf("Starting listener @%s -> %s\n", hostAddress, network);
	}

	private byte[] solveChallenge(byte[] challenge) throws NoSuchAlgorithmException {
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

		return sha256.digest(challenge);
	}
}
