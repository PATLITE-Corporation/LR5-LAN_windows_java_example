package jp.co.patlite.LR5_LAN.sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		Control ctr = new Control("192.168.10.1", 10000);

		// Connect to LR5-LAN
		int ret = ctr.SocketOpen();
		if (ret == -1) {
			return;
		}
		try {
            String commandId = "";
            if (args.length > 0) {
                commandId = args[0];
            }

            switch (commandId)
            {
                case "S":
                    {
                        // operation control command
                        if (args.length >= 7)
                        {
                            Control.PNS_RUN_CONTROL_DATA runControlData = ctr.new PNS_RUN_CONTROL_DATA();
                       	    runControlData.ledRedPattern = (byte)Integer.parseInt(args[1]);
                            runControlData.ledAmberPattern = (byte)Integer.parseInt(args[2]);
                            runControlData.ledGreenPattern = (byte)Integer.parseInt(args[3]);
                            runControlData.ledBluePattern = (byte)Integer.parseInt(args[4]);
                            runControlData.ledWhitePattern = (byte)Integer.parseInt(args[5]);
                            runControlData.buzzerMode = (byte)Integer.parseInt(args[6]);
                            ctr.PNS_RunControlCommand(runControlData);
                        }

                        break;
                    }
                case "C":
                    {
                        // clear command
                    	ctr.PNS_ClearCommand();
                        break;
                    }
                case "G":
                    {
                        // get status command
            			Control.PNS_STATUS_DATA statusData = ctr.PNS_GetDataCommand();
                        if (ret == 0)
                        {
                            // Display acquired data
                            System.err.println("Response data for status acquisition command");
                            // LED Red pattern
                            System.err.println("LED Red pattern : " + statusData.ledPattern[0]);
                            // LED Amber pattern
                            System.err.println("LED Amber pattern : " + statusData.ledPattern[1]);
                            // LED Green pattern
                            System.err.println("LED Green pattern : " + statusData.ledPattern[2]);
                            // LED Blue pattern
                            System.err.println("LED Blue pattern : " + statusData.ledPattern[3]);
                            // LED White pattern
                            System.err.println("LED White pattern : " + statusData.ledPattern[4]);
                            // Buzzer mode
                            System.err.println("Buzzer mode : " + statusData.buzzer);

                        }
                        break;
                    }
            }
		} finally {
		            // Close the socket
			ctr.SocketClose();
		}
	}

}

class Control {
	/** product category */
	private static short PNS_PRODUCT_ID = 0x4142;

	// PNS command identifier
	/** operation control command */
	private static byte PNS_RUN_CONTROL_COMMAND = 0x53;
	/** clear command */
	private static byte PNS_CLEAR_COMMAND = 0x43;
	/** get status command */
	private static byte PNS_GET_DATA_COMMAND = 0x47;

	// response data for PNS command
	/** normal response */
	private static byte PNS_ACK = 0x06;
	/** abnormal response */
	private static byte PNS_NAK = 0x15;

	// LED unit for motion control command
	/** light off */
	public static byte PNS_RUN_CONTROL_LED_OFF = 0x00;
	/** light on */
	public static byte PNS_RUN_CONTROL_LED_ON = 0x01;
	/** flashing (slow) */
	public static byte PNS_RUN_CONTROL_LED_BLINKING_SLOW = 0x02;
	/** flashing (medium) */
	public static byte PNS_RUN_CONTROL_LED_BLINKING_MEDIUM = 0x03;
	/** flashing (high) */
	public static byte PNS_RUN_CONTROL_LED_BLINKING_HIGH = 0x04;
	/** flashing single */
	public static byte PNS_RUN_CONTROL_LED_FLASHING_SINGLE = 0x05;
	/** flashing double */
	public static byte PNS_RUN_CONTROL_LED_FLASHING_DOUBLE = 0x06;
	/** flashing triple */
	public static byte PNS_RUN_CONTROL_LED_FLASHING_TRIPLE = 0x07;
	/** no change */
	public static byte PNS_RUN_CONTROL_LED_NO_CHANGE = 0x09;

	// buzzer for motion control command
	/** stop */
	public static byte PNS_RUN_CONTROL_BUZZER_STOP = 0x00;
	/** sing */
	public static byte PNS_RUN_CONTROL_BUZZER_RING = 0x01;
	/** no change */
	public static byte PNS_RUN_CONTROL_BUZZER_NO_CHANGE = 0x09;

	/**
	 * operation control data structure
	 */
	public class PNS_RUN_CONTROL_DATA {
		/** 1st LED unit pattern */
		public byte ledRedPattern = 0;

		/** 2nd LED unit pattern */
		public byte ledAmberPattern = 0;

		/** 3rd LED unit pattern */
		public byte ledGreenPattern = 0;

		/** 4th LED unit pattern */
		public byte ledBluePattern = 0;

		/** 5th LED unit pattern */
		public byte ledWhitePattern = 0;

		/** buzzer pattern 1 to 3 */
		public byte buzzerMode = 0;
	}

	/**
	 * status data of operation control
	 */
	public class PNS_STATUS_DATA {
		/** LED Pattern 1 to 5 */
		public byte[] ledPattern = new byte[5];

		/** buzzer mode */
		public byte buzzer = 0;

	}

	// local variable
	/** IP address */
	private String ip;
	/** port number */
	private int port;
	/** Socket */
	private Socket sock;
	/** Transmit stream */
	private OutputStream out;
	/** Receive Stream */
	private InputStream in;

	/**
	 * constructor
	 *
	 * @param ip   IP address
	 * @param port port number
	 */
	Control(final String ip, final int port) {
		this.ip = ip;
		this.port = port;
		this.sock = null;
		this.out = null;
		this.in = null;
	}

	/**
	 * Connect to LR5-LAN
	 *
	 * @return success: 0, failure: non-zero
	 */
	public int SocketOpen() {
		try {
			// Create a socket
			this.sock = new Socket(this.ip, this.port);

			// Obtaining the transmitted and received streams
			this.out = this.sock.getOutputStream();
			this.in = this.sock.getInputStream();

		} catch (IOException ex) {
			ex.printStackTrace();
			return -1;
		}

		return 0;
	}

	/**
	 * Close the socket.
	 */
	public void SocketClose() {
		try {
			if (this.in != null) {
				this.in.close();
				this.in = null;
			}

			if (this.out != null) {
				this.out.close();
				this.out = null;
			}

			if (this.sock != null) {
				this.sock.close();
				this.sock = null;
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Send command
	 *
	 * @param sendData send data
	 * @return received data
	 */
	private byte[] SendCommand(final byte[] sendData) {
		try {
			if (this.sock == null) {
				System.err.println("socket is not");
				return null;
			}

			// Send
			this.out.write(sendData);

			// Receive response data
			byte[] recvData = new byte[1024];
			int size = this.in.read(recvData);
			// Truncate the incoming data to the size you read in.
			recvData = Arrays.copyOf(recvData, size);

			return recvData;

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Send operation control command for PNS command<br>
	 * Each color of the LED unit and the buzzer can be controlled by the pattern specified in the data area<br>
	 * Operates with the color and buzzer set in the signal light mode
	 *
	 * @param runControlData Red/amber/green/blue/white LED unit operation patterns, buzzer mode<br>
	 *                       Pattern of LED unit(off: 0, on: 1, blinking(slow): 2, blinking(medium): 3, blinking(high): 4, flashing single: 5, flashing double: 6, flashing triple: 7, no change: 9)<br>
	 *                       Pattern of buzzer (stop: 0, ring: 1, no change: 9)
	 * @return success: 0, failure: non-zero
	 */
	public int PNS_RunControlCommand(final PNS_RUN_CONTROL_DATA runControlData) {
		ByteBuffer sendData = ByteBuffer.allocate(12);

		// Product Category (AB)
		sendData.putShort(PNS_PRODUCT_ID);

		// Command identifier (S)
		sendData.put(PNS_RUN_CONTROL_COMMAND);

		// Empty
		sendData.put((byte) 0x00);

		// Data size、Data area
		byte[] data = { runControlData.ledRedPattern, // LED Red pattern
				runControlData.ledAmberPattern, // LED Amber pattern
				runControlData.ledGreenPattern, // LED Green pattern
				runControlData.ledBluePattern, // LED Blue pattern
				runControlData.ledWhitePattern, //LED White pattern
				runControlData.buzzerMode, // Buzzer mode
		};
		sendData.putShort((short) data.length);
		sendData.put(data);

		// Send PNS command
		byte[] recvData = this.SendCommand(sendData.array());
		if (recvData == null) {
			System.err.println("failed to send data");
			return -1;
		}

		// check the response data
		if (recvData[0] == PNS_NAK) {
			// receive abnormal response
			System.err.println("negative acknowledge");
			return -1;
		}

		return 0;
	}

	/**
	 * Send clear command for PNS command<br>
	 * Turn off the LED unit and stop the buzzer
	 *
	 * @return success: 0, failure: non-zero
	 */
	public int PNS_ClearCommand() {
		ByteBuffer sendData = ByteBuffer.allocate(6);

		// Product Category (AB)
		sendData.putShort(PNS_PRODUCT_ID);

		// Command identifier (C)
		sendData.put(PNS_CLEAR_COMMAND);

		// Empty
		sendData.put((byte) 0x00);

		// Data size
		sendData.putShort((short) 0);

		// Send PNS command
		byte[] recvData = this.SendCommand(sendData.array());
		if (recvData == null) {
			System.err.println("failed to send data");
			return -1;
		}

		// check the response data
		if (recvData[0] == PNS_NAK) {
			// receive abnormal response
			System.err.println("negative acknowledge");
			return -1;
		}

		return 0;
	}

	/**
	 * Send status acquisition command for PNS command<br>
	 * LED unit and buzzer status can be acquired
	 *
	 * @return Received data of status acquisition command (status of LED unit and buzzer)
	 */
	public PNS_STATUS_DATA PNS_GetDataCommand() {
		ByteBuffer sendData = ByteBuffer.allocate(6);

		// Product Category (AB)
		sendData.putShort(PNS_PRODUCT_ID);

		// Command identifier (G)
		sendData.put(PNS_GET_DATA_COMMAND);

		// Empty
		sendData.put((byte) 0x00);

		// Data size
		sendData.putShort((short) 0);

		// Send PNS command
		byte[] recvData = this.SendCommand(sendData.array());
		if (recvData == null) {
			System.err.println("failed to send data");
			return null;
		}

		// check the response data
		if (recvData[0] == PNS_NAK) {
			// receive abnormal response
			System.err.println("negative acknowledge");
			return null;
		}

		PNS_STATUS_DATA statusData = new PNS_STATUS_DATA();

		// LED Pattern 1 to 5
		System.arraycopy(recvData, 0, statusData.ledPattern, 0, statusData.ledPattern.length);

		// buzzer Mode
		statusData.buzzer = recvData[5];

		return statusData;
	}

}
