package shell;

import device.Device;
import java.io.IOException;
import java.util.List;
import logging.Logger;
import logging.LoggingCategory;
import shell.apps.CommandShell.CommandShell;
import shell.apps.TerminalApplication;
import shell.apps.TextEditor.TextEditor;
import telnetd.io.BasicTerminalIO;
import telnetd.net.Connection;
import telnetd.net.ConnectionEvent;
import telnetd.shell.Shell;

/**
 *
 * @author Martin Lukáš
 */
public class TelnetSession implements Shell {

	private Connection m_Connection;
	private BasicTerminalIO m_IO;
	private int port = -1;
	private Device device;
	private TerminalApplication rootApplication;

	public void run(Connection con) {

		Logger.log(Logger.INFO, LoggingCategory.TELNET, "telnet session estabilished with host: " + con.getConnectionData().getHostAddress() + " port: " + con.getConnectionData().getPort());


		this.port = con.getConnectionData().getSocket().getLocalPort();
		this.m_Connection = con;
		this.m_IO = m_Connection.getTerminalIO();
		this.m_Connection.addConnectionListener(this); //dont forget to register listener


		// SEARCH FOR DEVICE OBJECT LISTENIG ON TelnetSession.port
		List<Device> devices = psimulator2.Psimulator.getPsimulator().devices;
		for (Device comparedDevice : devices) {
			if (comparedDevice.getTelnetPort() == this.port) // found it!!
			{
				this.device = comparedDevice;
				break; // end of search
			}
		}

		if (this.device == null) // upps there is a problem
		{
			Logger.log(Logger.ERROR, LoggingCategory.TELNET, "Cannot find device which listen on port: " + this.port);
		}

		Logger.log(Logger.INFO, LoggingCategory.TELNET, "TelnetSession sucessfuly created for device:" + device.getName() + " on port: " + this.port+ " using:" + con.getConnectionData().getNegotiatedTerminalType());



		CommandShell cmd = new CommandShell(m_IO, this.device);  // create command shell
		this.rootApplication = cmd;

//		TextEditor edt = new TextEditor(m_IO, device);
//		this.rootApplication = edt;
//		
		
		int retValue = this.rootApplication.run();

		if (retValue != 0) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Command shell escaped with non-zero return value: " + retValue);
		}


	}

	//this implements the ConnectionListener!
	@Override
	public void connectionTimedOut(ConnectionEvent ce) {
		try {
			m_IO.write("CONNECTION_TIMEDOUT");
			m_IO.flush();
			//close connection
			m_Connection.close();
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection timeout");
		}
	}//connectionTimedOut

	@Override
	public void connectionIdle(ConnectionEvent ce) {
		try {
			m_IO.write("CONNECTION_IDLE");
			m_IO.flush();
		} catch (IOException e) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "CONNECTION_IDLE");

		}

	}//connectionIdle

	@Override
	public void connectionLogoutRequest(ConnectionEvent ce) {
		try {
			this.rootApplication.quit();
			m_IO.write("CONNECTION_LOGOUTREQUEST");
			m_IO.flush();
			this.m_Connection.close();
		} catch (Exception ex) {
			Logger.log(Logger.INFO, LoggingCategory.TELNET, "CONNECTION_LOGOUTREQUEST");
		}
	}//connectionLogout

	@Override
	public void connectionSentBreak(ConnectionEvent ce) {
		try {
			m_IO.write("CONNECTION_BREAK");
			m_IO.flush();
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "CONNECTION_BREAK");

		}
	}//connectionSentBreak

	public static Shell createShell() {
		return new TelnetSession();
	}//telnetd library needs this method
}
