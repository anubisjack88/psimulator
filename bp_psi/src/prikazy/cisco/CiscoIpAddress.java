/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package prikazy.cisco;

import datoveStruktury.IpAdresa;
import java.util.List;
import pocitac.AbstraktniPocitac;
import pocitac.CiscoPocitac;
import pocitac.Konsole;
import pocitac.SitoveRozhrani;
import vyjimky.SpatnaAdresaException;
import vyjimky.SpatnaMaskaException;

/**
 * Trida pro prikaz 'ip address' ve stavu IFACE.
 * @author haldyr
 */
public class CiscoIpAddress extends CiscoPrikaz {

    SitoveRozhrani rozhrani;
    IpAdresa adr;
    boolean noBezAdresy = false;

    public CiscoIpAddress(AbstraktniPocitac pc, Konsole kon, List<String> slova, boolean no, SitoveRozhrani rozhrani) {
        super(pc, kon, slova, no);
        this.rozhrani = rozhrani;

        debug = false;
        ladici("konstruktor");

        boolean pokracovat = zpracujRadek();
        if (pokracovat) {
            vykonejPrikaz();
        }
    }

    @Override
    protected boolean zpracujRadek() {
        //ip address 192.168.2.129 255.255.255.128
        if (no) { // vim, ze ve slova je 'no ip'
            dalsiSlovo();
        }

        if (!kontrola("address", dalsiSlovo(), 3)) {
            return false;
        }
        ladici("po address");

        String ip = dalsiSlovo();

        if (ip.length() == 0){
            noBezAdresy = true;
            return true;
        }

        String maska = dalsiSlovo();
        if (jePrazdny(ip) || jePrazdny(maska)) {
            return false;
        }

        if (IpAdresa.jeZakazanaIpAdresa(ip)) {
            kon.posliRadek("Not a valid host address - " + ip);
            return false;
        }

        try {
            ladici("vytvarim IP");
            adr = new IpAdresa(ip, maska);
        } catch (SpatnaMaskaException e) {
            String[] pole = maska.split("\\.");
            String s = "";
            int i;
            for (String bajt : pole) {
                try {
                    i = Integer.parseInt(bajt);
                    s += Integer.toHexString(i);

                } catch (NumberFormatException exs) {
                    invalidInputDetected();
                    return false;
                }
            }
            kon.posliRadek("Bad mask 0x" + s.toUpperCase() + " for address " + ip);
        } catch (SpatnaAdresaException e) {
            invalidInputDetected();
        } catch (Exception e) {
            e.printStackTrace(); //TODO: e.printStackTrace(); pak zrusit?
            invalidInputDetected();
        }

        if (adr.dej32BitAdresu() == 0) {
            kon.posliRadek("Not a valid host address - " + ip);
            return false;
        }

        if (adr.jeCislemSite() || adr.jeBroadcastemSite() || adr.dej32BitMasku() == 0) {
            // Router(config-if)#ip address 147.32.120.0 255.255.255.0
            // Bad mask /24 for address 147.32.120.0
            kon.posliRadek("Bad mask /" + adr.pocetBituMasky() + " for address " + adr.vypisAdresu());
            return false;
        }

        ladici("konec");
        if (dalsiSlovo().length() != 0) {
            invalidInputDetected();
            return false;
        }
        return true;
    }

    @Override
    protected void vykonejPrikaz() {

        if (no) {
            if (noBezAdresy) {
                rozhrani.seznamAdres.set(0, null);
                return;
            }

            if (rozhrani.vratPrvni().dej32BitAdresu() != adr.dej32BitAdresu()) {
                kon.posliRadek("Invalid address");
                return;
            }
            if (rozhrani.vratPrvni().dej32BitMasku() != adr.dej32BitMasku()) {
                kon.posliRadek("Invalid address mask");
                return;
            }

            rozhrani.seznamAdres.set(0, null);
            return;
        }
        
        rozhrani.zmenPrvniAdresu(adr);

        ((CiscoPocitac) pc).getWrapper().update();
    }
}