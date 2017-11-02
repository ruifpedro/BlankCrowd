package pt.bc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 */
public class BroadcastThread extends Thread {
	protected DatagramSocket socket = null;
	protected String network;
	protected String hostAddress;
	private final int broadcaatPort = Configuration.authPort;
	private static int broadcastSleep = Configuration.broadcastSleep;

	public ConcurrentSkipListSet<String> peers;

	/**
	 * BroadcastThread constructor
	 * @param name
	 * @param hostAddress
	 * @param network
	 * @throws SocketException
	 */
	public BroadcastThread(String name, String hostAddress, String network) throws SocketException {
		super(name);
		this.socket = new DatagramSocket();
		socket.setReuseAddress(true);
		this.hostAddress = hostAddress;
		this.network = network;
		this.peers = new ConcurrentSkipListSet<>();
	}

	/**
	 *
	 */
	public void run() {
		startPrint();
		byte[] challenge = genChallenge();

		//broadcasting task
		Runnable broadcastTask = () -> {
			while (true) {
				try {
					System.out.printf("%s@%s -> Broadcasting challenge\n", getName(), hostAddress);
					//broadcast a challenge
					broadcast(socket, network, challenge, challenge.length);
					sleep(broadcastSleep);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		//challenge solved listener
		Runnable challengeListener = () -> {
			while (true) {
				byte[] data = new byte[32];
				DatagramPacket packet = new DatagramPacket(data, data.length);
				try {
					socket.receive(packet);

					if (checkChallengeResponse(challenge, data)) {
						System.out.printf("%s@%s -> Found a friend!!\n", getName(), hostAddress);

						String peerAddress = packet.getAddress().getHostAddress();
						peers.add(peerAddress);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		};

		Runnable sharingTask = () -> {
			while(true){
				//poc - share peer list¶
				try {
					byte[] toShare = {0};
					peers.toString().getBytes(Charset.defaultCharset());

					DatagramPacket toSharePacket = new DatagramPacket(toShare, toShare.length);

					DatagramSocket commSocket = new DatagramSocket(Configuration.commPort);

					commSocket.send(toSharePacket);
					commSocket.close();
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};

		ArrayList<Thread> threads = new ArrayList<>();

		threads.add(new Thread(broadcastTask));
		threads.add(new Thread(challengeListener));

		threads.stream().sequential().forEach(Thread::start);

		while (!threads.isEmpty()) {
			threads.stream().sequential().forEach(t -> {
				try {
					t.join(1000);
//					if(!t.isAlive())
//						threads.remove(t);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}

	/**
	 *
	 */
	private void startPrint() {
		System.out.printf("Starting broadcaster @%s -> %s\n", hostAddress, network);
	}

	/**
	 *
	 * @param socket
	 * @param network
	 * @param content
	 * @param content_length
	 * @throws IOException
	 */
	private void broadcast(DatagramSocket socket, String network, byte[] content, int content_length) throws IOException {
		InetAddress group = InetAddress.getByName(network);
		DatagramPacket packet;
		packet = new DatagramPacket(content, content_length, group, broadcaatPort);
		socket.send(packet);
	}

	/**
	 *
	 * @return
	 */
	private byte[] genChallenge() {
		SecureRandom sRandomGen = new SecureRandom();
		byte[] random = new byte[32];
		sRandomGen.nextBytes(random);
		return random;
	}

	/**
	 *
	 * @param challenge
	 * @param response
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private boolean checkChallengeResponse(byte[] challenge, byte[] response) throws NoSuchAlgorithmException {
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

		return Arrays.equals(response, sha256.digest(challenge));
	}
}
