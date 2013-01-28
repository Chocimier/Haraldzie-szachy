/* Ten plik jest częścią programu „Haraldzie szachy”
 * Copyleft (C) 2013 Piotr Wójcik
 *
 * Program jest objęty Licencją publiczną Unii Europejskiej (EUPL)
 * w wersji dokładnie 1.1, dostępnej pod adresem
 * http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *
 * Kod źródłowy programu można pobrać pod adresem
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.lcdui.*;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import javax.microedition.midlet.MIDlet;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class Szachy extends MIDlet implements Runnable, CommandListener, DiscoveryListener
{
    Mowy mowy;
    List menuGlowne, wykazSerwerow;
    Form czekanie, zbite, pomoc;
    Ekran plotno;
    Vector serwery;
    Command polWybrano, polWstecz, polSzukaj, polWarcaby, polPromocja, polSzachy, polWibracja, polWyjscie, polZbite, polCofnij, polGlowne;
    StreamConnectionNotifier obserwadlo;
    StreamConnection rurka;
    DiscoveryAgent wykrywadlo;
    Thread watek;
    String cecha, adresSerweru;
    boolean serwerTu, sluchac;

    public Szachy()
    {
        cecha = "e0f4c2bb9a844b588ea0025f4c105763";

        polWybrano = new Command(_("Wybierz"), Command.SCREEN, 1);
        polSzukaj = new Command(_("Szukaj"), Command.SCREEN, 2);
        polWarcaby = new Command(_("Warcaby"), Command.SCREEN, 2);
        polSzachy = new Command(_("Szachy"), Command.SCREEN, 2);
        polPromocja = new Command(_("Promuj"), Command.SCREEN, 2);
        polWstecz = new Command(_("Wstecz"), Command.BACK, 3);
        polGlowne = new Command(_("Menu główne"), Command.BACK, 3);
        polWibracja = new Command(_("Nie wstrząsaj"), Command.SCREEN, 3);
        polWyjscie = new Command(_("Wyjście"), Command.EXIT, 3);
        polZbite = new Command(_("Obejrzyj zbite"), Command.SCREEN, 3);
        polCofnij = new Command(_("Wycofaj ruch"), Command.SCREEN, 3);

        plotno=new Ekran(false, this, menuGlowne);
        plotno.addCommand(polWybrano);
        plotno.addCommand(polWarcaby);
        plotno.addCommand(polSzachy);
        plotno.addCommand(polPromocja);
        plotno.addCommand(polGlowne);
        plotno.addCommand(polZbite);
        plotno.addCommand(polCofnij);
        plotno.addCommand(polWibracja);
        plotno.setCommandListener(this);

        zbite = new Form(_("Zbite bierki"));
        zbite.addCommand(polWstecz);
        zbite.setCommandListener(this);

        pomoc = new Form(_("Pomoc, O programie"));
        pomoc.append("Aby promować pion, należy ustawić go w ostatniej linii, wskazać i wybrać pozycję  „promocja” z menu.\n  *  *  *  \nHaraldzie szachy\nPrawa autorskie (C) 2013 Piotr Wójcik\nProgram jest objęty Licencją publiczną Unii Europejskiej (EUPL) w wersji dokładnie 1.1, dostępnej pod adresem http://joinup.ec.europa.eu/software/page/eupl/licence-eupl. Kod źródłowy programu można pobrać pod adresem");
        pomoc.addCommand(polWstecz);
        pomoc.setCommandListener(this);

        menuGlowne = new List(_("Menu"), List.IMPLICIT);
        menuGlowne.append(_("Nowa gra na 1 telefonie"), null);
        menuGlowne.append(_("Założenie gry niebieskim zębem"), null);
        menuGlowne.append(_("Dołączenie do gry niebieskim zębem"), null);
        menuGlowne.append(_("Ciąg dalszy gry"), null);
        menuGlowne.append(_("Pomoc, O programie"), null);
        menuGlowne.append(_("Wyjście"), null);
        menuGlowne.setSelectCommand(polWybrano);
        menuGlowne.addCommand(polWyjscie);
        menuGlowne.setCommandListener(this);

        wyswietl(menuGlowne);
    }
    public void startApp(){}
    public void pauseApp(){}
    public void destroyApp(boolean koniecznie){this.notifyDestroyed();}
    public void commandAction(Command polecenie, Displayable ekran)
    {
        if (ekran==menuGlowne)
        {
            if (polecenie==polWybrano)
            {
                int pozycja = menuGlowne.getSelectedIndex();
                if (pozycja==0)
                { // Nowa gra podawana
                    plotno.blutufem = false;
                    naSerwer("wsghkgswpppppppp................................PPPPPPPPWSGHKGSW");
                    wyswietl(plotno);
                }
                else if (pozycja==1) // Założenie gry niebieskim zębem
                    serwerWRuch();
                else if (pozycja==2) // Dołączenie do gry niebieskim zębem
                    klientWRuch(true);
                else if (pozycja==3) // C. d. gry podawanej
                    wyswietl(plotno);
                else if (pozycja==4) // Pomoc
                    wyswietl(pomoc);
                else if (pozycja==5) // Wyjście
                    destroyApp(false);
            }
            else if (polecenie==polWyjscie)
            {
                destroyApp(false);
            }
        }
        else if (ekran==wykazSerwerow)
        {
            if (polecenie==polSzukaj)
                szukajSerwerow();
            if (polecenie==polWybrano)
            {
                UUID[] szukaneUslugi = new UUID[1];
                szukaneUslugi[0] = new UUID(cecha, false);
                RemoteDevice serwer = (RemoteDevice) serwery.elementAt(wykazSerwerow.getSelectedIndex());
                try{wykrywadlo.searchServices(new int[] {256}, szukaneUslugi, serwer, this);}
                catch(BluetoothStateException w){blad("б", w);}
            }
        }
        else if (ekran==plotno)
        {
            if (polecenie==polWybrano)
                plotno.keyPressed(-5);
            else if (polecenie==polWarcaby)
                naSerwer("p.p.p.p..p.p.p.pp.p.p.p..................P.P.P.PP.P.P.P..P.P.P.P");
            else if (polecenie==polSzachy)
                naSerwer("wsghkgswpppppppp................................PPPPPPPPWSGHKGSW");
            else if (polecenie==polPromocja)
                plotno.promuj();
            else if (polecenie==polGlowne)
            {
                sluchac = false;
                wyswietl(menuGlowne);
            }
            else if (polecenie==polWibracja)
            {
                plotno.removeCommand(polWibracja);
                if (plotno.wstrzasac)
                {
                    plotno.wstrzasac = false;
                    polWibracja = new Command (_("Wstrząsaj"), Command.SCREEN, 3);
                }
                else
                {
                    plotno.wstrzasac = true;
                    polWibracja = new Command (_("Nie wstrząsaj"), Command.SCREEN, 3);
                }
                plotno.addCommand(polWibracja);
            }
            else if (polecenie==polZbite)
            {
                int p=8; int w=2; int s=2; int g=2; int h=1; int P=8; int W=2; int S=2; int G=2; int H=1;
                int dlugosc = plotno.stanGry.length();
                for (int i=0; i<dlugosc; ++i)
                {
                    switch (plotno.stanGry.charAt(i))
                    {
                        case 'p': --p; break;
                        case 'P': --P; break;
                        case 'w': --w; break;
                        case 'W': --W; break;
                        case 's': --s; break;
                        case 'S': --S; break;
                        case 'g': --g; break;
                        case 'G': --G; break;
                        case 'h': --h; break;
                        case 'H': --H; break;
                    }
                }
                zbite.deleteAll();
                String[] nazwy = new String[]{"pc", "wc", "sc", "gc", "hc", "Pb", "Wb", "Sb", "Gb", "Hb"};
                int[] liczby = new int[]{p, w, s, g, h, P, W, S, G, H};
                Image obrazek;
                for (int i=0; i<10; ++i)
                {
                    if (liczby[i]>0)
                    {
                        try{obrazek = Image.createImage("/"+nazwy[i]+".png");}
                        catch(Exception wyj){blad("м", wyj);continue;}
                        for (int j=0; j<liczby[i]; ++j)
                            zbite.append(obrazek);
                    }
                    if (i==4)
                        zbite.append("\n");
                }
                wyswietl(zbite);
            }
            else if (polecenie==polCofnij)
                naSerwer(plotno.stanGryCof);
        }
        else if (ekran==zbite)
        {
            if (polecenie==polWstecz)
                wyswietl(plotno);
        }
        else if (ekran==pomoc)
        {
            if (polecenie==polWstecz)
                wyswietl(menuGlowne);
        }
    }
    public void serwerWRuch()
    {
        String adres = "btspp://localhost:"+cecha+";name=Haraldzie_Szachy";
        czekanieNa(_("Czekanie na 2 gracza"));
        wyswietl(czekanie);
        try
        {
            obserwadlo = (StreamConnectionNotifier) Connector.open(adres);
            rurka = (StreamConnection) obserwadlo.acceptAndOpen();
            watek = new Thread(this);
            watek.start();
        }
        catch(Exception w){blad("в", w);}
        klientWRuch(false);
    }
    public void run()
    {
        byte masa[];
        int sczytano, dlugosc;
        InputStream wlot;

        sluchac = true;
        try
        {
            while (sluchac)
            {
                masa = null;
                wlot = rurka.openInputStream();
                dlugosc = wlot.read();
                try{masa = new byte[dlugosc];}
                catch(NegativeArraySizeException w)
                {
                    blad("л", w);
                    sluchac = false;
                    continue;
                }
                dlugosc = 0;
                while (dlugosc != masa.length)
                {
                    sczytano = wlot.read(masa, dlugosc, masa.length - dlugosc);
                    if (sczytano > -1)
                        dlugosc += sczytano;
                }
                wlot.close();
                if (serwerTu)
                    naSerwer(new String(masa));
                else
                    doKlientu(new String(masa));
            }
            rurka.close();
        }
        catch(BluetoothStateException w){blad("г", w);}
        catch(IOException w){blad("к", w);}
    }
    public void klientWRuch(boolean poBlutufie)
    {
        serwerTu = !poBlutufie;
        if (poBlutufie)
        {
            try {wykrywadlo = LocalDevice.getLocalDevice().getDiscoveryAgent();}
            catch(BluetoothStateException w){blad("д", w);};
            serwery = new Vector();
            wykazSerwerow = new List(_("Serwery"), List.IMPLICIT);
            wykazSerwerow.setSelectCommand(polWybrano);
            wykazSerwerow.addCommand(polSzukaj);
            wykazSerwerow.setCommandListener(this);
            wyswietl(wykazSerwerow);
            szukajSerwerow();
        }
        else
        {
            plotno.blutufem = true;
            wyswietl(plotno);
        }
    }
    public void szukajSerwerow()
    {
        serwery.removeAllElements();
        wykazSerwerow.deleteAll();
        try{wykrywadlo.startInquiry(DiscoveryAgent.GIAC, this);}
        catch(BluetoothStateException w){blad("а", w);}
        czekanieNa(_("Szukanie serwerów"));
        wyswietl(czekanie);
    }
    public void doKlientu(String wiadomosc)
    {
        plotno.ustaw(wiadomosc, true);
    }
    public void naSerwer(String wiadomosc)
    {
        doKlientu(wiadomosc);
        if (plotno.blutufem)
            portem(wiadomosc.getBytes());
    }
    public void portem(byte[] masa)
    {
        try
        {
            OutputStream wylot = rurka.openOutputStream();
            wylot.write(masa.length);
            wylot.write(masa);
            wylot.flush();
            wylot.close();
        }
        catch(IOException w){blad("е", w);};
    }
    public void inquiryCompleted(int wynik)
    {
        wyswietl(wykazSerwerow);
    }
    public void deviceDiscovered(RemoteDevice urzadzenie, DeviceClass cod)
    {
        String nazwa;
        try{nazwa = urzadzenie.getFriendlyName(false);}
        catch(IOException w){nazwa = urzadzenie.getBluetoothAddress();blad("ж", w);}
        wykazSerwerow.append(nazwa, null);
        serwery.addElement(urzadzenie);
    }
    public void servicesDiscovered(int cechaSzukania, ServiceRecord[] opisUslug)
    {
        adresSerweru = opisUslug[0].getConnectionURL(0, false);
    }
    public void serviceSearchCompleted(int cechaSzukania, int kodOdp)
    {
        try{rurka = (StreamConnection) Connector.open(adresSerweru);}
        catch(IOException w){blad("з", w);}
        watek = new Thread(this);
        watek.start();
        plotno.blutufem = true;
        wyswietl(plotno);
    }
    public void czekanieNa(String costam)
    {
        czekanie = new Form(_("Chwilka…"));
        czekanie.append(costam+"\n");
        czekanie.addCommand(polWstecz);
        czekanie.setCommandListener(this);
    }
    public void wyswietl(Displayable ekran)
    {
        Display.getDisplay(this).setCurrent(ekran);
    }
    public void blad(String ozn, Exception wyj)
    {
        System.out.println("Ош. "+ozn+": "); // ost. użyty: м
        wyj.printStackTrace();
    }
    public String _(String napis)
    {
        if (mowy == null)
            mowy = new Mowy();
        return mowy.przeklad(napis);
    }
}
