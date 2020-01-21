package ch.pschatzmann.jflightcontroller4pi.integration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Simple UDPOutputStream which sends DatagramPackets
 * @author pschatzmann
 *
 */
public class UDPOutputStream extends OutputStream {
	private byte buf[] = new byte[5000];
	private int pos = -1;
	private DatagramSocket sock;
	private InetAddress address;

	public UDPOutputStream(DatagramSocket sock) throws SocketException {
		this.sock = sock;
	}

	@Override
	public synchronized void write(int b) throws IOException {
		buf[++pos] = (byte) b;
	}

	@Override
	public void write(byte b[]) throws IOException {
		for (int j = 0; j < b.length; j++) {
			buf[j] = b[j];
		}
		pos = b.length;
		flush();
	}

	@Override
	public synchronized void flush() throws IOException {
		if (address != null) {
			DatagramPacket dp = new DatagramPacket(buf, pos, address, sock.getLocalPort());
			sock.send(dp);
			pos = -1;
		}
	}
	
	public void setAddress(InetAddress address) {
		this.address = address;
	}
}
