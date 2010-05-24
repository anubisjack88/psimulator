/*
 * Gegründet am Dienstag 5.1.2010 Abend.
 */

package pocitac;

import datoveStruktury.IpAdresa;
import java.util.ArrayList;
import java.util.List;

/**
 * Třída pro jedno síťové rozhraní.
 * @author neiss & haldyr
 *
 * DŮLEŽITÁ POZNÁMKA:
 * V pátek 16.4.2010 jsme zavedli více IpAdres pro jedno rozhraní. Původně jsme všechno
 * dělali jen pro jednu IP na rozhraní, ale pro natování je jich potřeba víc. Protože
 * zjišťování, jak se cisco a linux chová v případě více adres a třeba z jedný nebo z
 * více sítí na jednom rozhraní chová, by bylo strašně složitý, vykašlali jsme se na to.
 * NA VÍCE ADRES NA JEDNOM ROZHRANÍ SE TEDA NEDÁ V ŽÁDNÉM PŘÍPADĚ SPOLÉHAT! Hlavní
 * adresou na rozhraní je vždy ta první, další jsou jen na strojích, kde běží nat a
 * jen pro jeho potřebu. Kdyby snad někdo v budoucnosti chtěl v tomto programu možnost
 * více adres na jednom rozhraní doprogramovat, ať si dává veliký pozor, zvlášť na
 * metody vratPrvni() a pridejNaPrvniPosici(), které počítají pravě jen s tou jednou
 * adresou. Například nové pakety z počítače jsou vždy odesílané s první adresou.
 */
public class SitoveRozhrani {

    /**
     * Seznam adres. Ta prvni je jaksi privilegovana.
     */
    public List<IpAdresa> seznamAdres = new ArrayList<IpAdresa>(); //pozor, neda se na to
        //spolehat - viz. poznamka v javadocu tridy
    public String jmeno;
    public String macAdresa;
    public SitoveRozhrani pripojenoK; //sitove rozhrani, se kterym je toto rozhrani spojeno
    private AbstraktniPocitac pc; //pocitac, kteremu toto rozhrani patri
    
    /**
     * Stav rozhrani. True..zapnuto, false..vypnuto. <br />
     * Cisco je defaultne vypnute, linux zapnuty.
     */
    private boolean nahozene;

    public SitoveRozhrani(String jmeno, AbstraktniPocitac pc, String macAdresa) {
        this.pc = pc;
        this.jmeno = jmeno;
        this.macAdresa = macAdresa;
        seznamAdres.add(null); //na prvni misto se pridava null, je to jako ta prvni adresa

        if (pc instanceof LinuxPocitac) {
            this.nahozene = true;
        } else if (pc instanceof CiscoPocitac) {
            this.nahozene = false;
        }
    }

    @Override
    public String toString() {
        String s = "jmeno: "+jmeno+"\n";
        s += " mac: "+macAdresa +"\n";
        for (IpAdresa adr : seznamAdres) {
            s += " adr: " + adr.vypisAdresu()+"\n";
        }
        s += " stav: ";
        s += nahozene ? "nahozene" : "zhozene";
	s += "\n";

        if (pripojenoK != null) {
            s += " pripojenoK: "+pripojenoK.getPc().jmeno + ":" + pripojenoK.jmeno+"\n";
        }
        return s;
    }

    /**
     * Vrati stav rozhrani - zapnuto/vypnuto. True..zapnuto, false..vypnuto
     * @return
     */
    public boolean jeNahozene() {
        return nahozene;
    }

    /**
     * Nastavi stav rozhrani
     * @param stav stav, ktery chceme nastavit
     */
    public void nastavRozhrani(boolean stav) {
        this.nahozene = stav;
    }

    /**
     * Getter pro pocitac, ktery drzi toto rozhrani.
     * @return
     */
    public AbstraktniPocitac getPc(){
        return pc;
    }

    /**
     * Vrati ip adresu na pozici 0 nebo null, pokud tam zadna IP neni.
     * @return
     * @author haldyr
     */
    public IpAdresa vratPrvni() {
        if (seznamAdres.size() == 0) return null;
        return seznamAdres.get(0);
    }

    /**
     * Vrati true, pokud najde ip adresu shodnou jen v adrese. <br />
     * Hleda pomoci jeStejnaAdresa().
     * @param hledana
     * @return
     * @author haldyr
     */
    public boolean obsahujeStejnouAdresu(IpAdresa hledana) {
        for (IpAdresa ip : seznamAdres) {
            if (ip!=null && ip.jeStejnaAdresa(hledana)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vrati true, pokud najde ip adresu shodnou v adrese + masce. <br />
     * Hleda pomoci equals().
     * @param hledana
     * @return
     * @author neiss
     */
    public boolean obsahujeStejnouAdresuEq(IpAdresa hledana) {
        for (IpAdresa ip : seznamAdres) {
            if (ip!=null && ip.equals(hledana)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Zmeni tu privilegovanou prvni adresu. Starou nejdriv smaze a pak tam da novou.
     * Ma smysl, kdyz je ta prvni adresa null.
     * @param adr
     * @author haldyr
     */
    public void zmenPrvniAdresu(IpAdresa adr) {
        if (seznamAdres.size() > 0) {
            seznamAdres.remove(0);
        } 
        seznamAdres.add(0, adr);
        if (pc instanceof CiscoPocitac) {
            ((CiscoPocitac)pc).getWrapper().update();
        }
    }

    /**
     * Prvni adresu si schovam, vsechny IP smazu a prvni zase pridam.
     * @author haldyr
     */
    public void smazVsechnyIpKromPrvni() {
        IpAdresa prvni = null;
        if (vratPrvni() != null) {
            prvni = vratPrvni().vratKopii();
        }
        seznamAdres.clear();
        seznamAdres.add(prvni);
    }
}