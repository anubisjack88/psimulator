/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import logging.Loggable;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpCommand extends CiscoCommand {

	private final boolean no;
	private AbstractCommand command;
//	private final NetworkInterface iface;
	private final int state;
	private final boolean debug = true;

	public IpCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
		this.state = parser.getShell().getMode();
//		this.debug = Logger.logg(getDescription(), )

	}

	@Override
	public void run() {
		String dalsi = nextWord();

        if (no) {
            dalsi = nextWord();
        }

        if(dalsi.length() == 0) {
            incompleteCommand();
            return;
        }

        if (state == CommandShell.CISCO_CONFIG_MODE || (debug && state == CommandShell.CISCO_PRIVILEGED_MODE)) {

            if (kontrolaBezVypisu("route", dalsi, 5)) {
//                command = new CiscoIpRoute(pc, kon, slova, no);
                return;
            }

            if (kontrolaBezVypisu("nat", dalsi, 3)) {
//                command = new CiscoIpNat(pc, kon, slova, no);
                return;
            }

            if (kontrolaBezVypisu("classless", dalsi, 2)) {
                if (no) {
					getNetMod().ipLayer.routingTable.classless = false;
                } else {
					getNetMod().ipLayer.routingTable.classless = true;
                }
                return;
            }
        }

        if (state == CommandShell.CISCO_CONFIG_IF_MODE) {
            if (kontrolaBezVypisu("address", dalsi, 3)) {
//                command = new CiscoIpAddress(pc, kon, slova, no, rozhrani);
                return;
            }

            if (kontrolaBezVypisu("nat", dalsi, 2)) {
//                command = new CiscoIpNatRozhrani(pc, kon, slova, no, rozhrani);
                return;
            }
        }

        if (dalsi.length() != 0 && ambiguous == false) { // jestli to je prazdny, tak to uz vypise kontrolaBezVypisu
            invalidInputDetected();
        }
	}

	@Override
	public void catchUserInput(String input) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
