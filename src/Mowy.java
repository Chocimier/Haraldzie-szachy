/* Ten plik jest częścią programu „Haraldzie szachy”
 * Prawa autorskie (C) 2013 Piotr Wójcik
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
import java.io.ByteArrayOutputStream;
import java.util.Vector;

public class Mowy
{
    boolean wczytano = false;
    Vector najpierw = new Vector();
    Vector potem = new Vector();
    public Mowy()
    {
        String lokalizacja = System.getProperty("microedition.locale").replace('-','_');
        wczytaj("/mowy/"+lokalizacja+".txt");
        if (!wczytano)
            wczytaj("/mowy/"+lokalizacja.substring(0, lokalizacja.indexOf("_"))+".txt");
    }
    public Mowy(String jezyk, String kraj)
    {
        wczytaj("/mowy/"+jezyk+"_"+kraj+".txt");
        if (!wczytano)
            wczytaj("/mowy/"+jezyk+".txt");
    }
    public Mowy(String jezyk)
    {
        wczytaj("/mowy/"+jezyk+".txt");
    }
    public void wczytaj(String sciezka)
    {
        String tekst;
        InputStream strumien;
        tekst = "";
        try
        {
            strumien = getClass().getResourceAsStream(sciezka);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[16 * 1024];
            int bytesRead;
            while ((bytesRead = strumien.read(buffer)) > 0)
                baos.write(buffer, 0, bytesRead);
            tekst = new String(baos.toByteArray(), "UTF8");
            strumien.close();
        }
        catch (Exception w){w.printStackTrace();System.out.println(w.toString()); return;}
        String liniaPom, linia1;
        int n;
        boolean jeszczeLinie, jest1;
        jeszczeLinie = tekst.length()>0;
        jest1 = false;
        linia1 = "";
        System.out.println("leci");
        while (jeszczeLinie)
        {
            n = tekst.indexOf("\n");
            if (n==0)
            {
                tekst = tekst.substring(1);
                continue;
            }
            else if (n>=0)
            {
                liniaPom = tekst.substring(0, n);
                tekst = tekst.substring(n+1);
            }
            else
            {
                liniaPom = tekst;
                tekst = "";
            }
            if (liniaPom.endsWith("\r"))
                liniaPom = liniaPom.substring(0, n-1);
            if (jest1)
            {
                najpierw.addElement(linia1);
                potem.addElement(liniaPom);
            }
            else
                linia1 = liniaPom;
            jest1 = !jest1;
            if (tekst.length()==0)
                jeszczeLinie = false;
        }
        wczytano = true;
    }
    public String przeklad(String napis)
    {
        int ktory = najpierw.indexOf(napis);
        if (ktory<0)
            return napis;
        else
            return potem.elementAt(ktory).toString();
    }
}
