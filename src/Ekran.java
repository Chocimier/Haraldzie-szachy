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

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class Ekran extends GameCanvas implements Runnable
{
    boolean doPrzerysowania, blutufem, wstrzasac;
    char bierka, bierkaWcz;
    int dotX, dotY, wskX, wskY;
    String stanGry, stanGryWcz, sciezka, stanGryCof;
    Szachy midlet;
    Thread thread = null;
    Image obrazek;
    Graphics tuszem;
    Displayable menuGlowne;

    public Ekran(boolean sp, Szachy mid, Displayable menuGl)
    {
        super(sp);
        menuGlowne  = menuGl;
        midlet = mid;
        stanGry = "wsghkgswpppppppp................................PPPPPPPPWSGHKGSW";
        stanGryWcz = stanGry;
        stanGryCof = stanGry;
        doPrzerysowania = true;
        wstrzasac = true;
        //setFullScreenMode(true);
        dotX = -1; // dotknięta bierka
        dotY = -1;
        wskX =  0; // wskazane pole
        wskY =  0;
        tuszem = getGraphics();
        thread = new Thread(this);
        thread.start();
    }
    public void run()
    {
        int bokPola = Math.min(getWidth(), getHeight())/8;
        int bokPlanszy = bokPola*8;
        System.out.print("bok pola: ");
        System.out.println(bokPola);
        while (true)
        {
            if (doPrzerysowania)
            {
                doPrzerysowania = false;
                System.out.println("rysowanie");
                for (int i=0; i<8; ++i)
                    for (int j=0; j<8; ++j)
                    {
                        if ((i%2)==(j%2))
                            tuszem.setColor(200,190,200);
                        else
                            tuszem.setColor(70,50,0);
                        tuszem.fillRect(bokPola*i, bokPola*j, bokPola, bokPola);
                        bierka = stanGry.charAt(i+j*8);
                        bierkaWcz = stanGryWcz.charAt(i+j*8);
                        if (bierka!='.')
                        {
                            if (Character.isUpperCase(bierka))
                                sciezka = "/"+bierka+"b.png";
                            else
                                sciezka = "/"+bierka+"c.png";
                            try{obrazek = Image.createImage(sciezka);}catch(Exception w){w.printStackTrace();}
                            tuszem.drawImage(obrazek, bokPola*i, bokPola*j, Graphics.TOP|Graphics.LEFT);
                        }
                        if (bierka!=bierkaWcz)
                        {
                            tuszem.setColor(50,100,200);
                            tuszem.fillArc(bokPola*i+bokPola/3, bokPola*j+bokPola/3, bokPola/3, bokPola/3, 0, 360);
                        }
                    }
                // rysujemy ramkę wkoło wsazanego pola
                tuszem.setColor(50,100,200);
                tuszem.drawRect(bokPola*wskX,bokPola*wskY,bokPola-1,bokPola-1);
                tuszem.drawRect(bokPola*wskX+1,bokPola*wskY+1,bokPola-3,bokPola-3);
                // obwodzimy dotkniętą bierkę
                if (dotX>=0 && dotY>=0)
                {
                    tuszem.setColor(150,0,0);
                    tuszem.drawRect(bokPola*dotX+1,bokPola*dotY+1,bokPola-3,bokPola-3);
                    tuszem.drawRect(bokPola*dotX+2,bokPola*dotY+2,bokPola-5,bokPola-5);
                }
            }
            stanGryWcz = stanGry;
            flushGraphics();
            try{Thread.sleep(50);}catch(Exception w){w.printStackTrace();};
        }
    }
    public void keyPressed(int key)
    {
        int a, wskPole;
        char wskBierka;
        switch (key)
        {
        case -3: /* lewo */
        case 52: /* 4 */
            wskX = (wskX+7)%8;
            doPrzerysowania = true;
            break;
        case -4: /* prawo */
        case 54: /* 6 */
            wskX = (wskX+1)%8;
            doPrzerysowania = true;
            break;
        case -1: /* góra */
        case 50: /* 2 */
            wskY = (wskY+7)%8;
            doPrzerysowania = true;
            break;
        case -2: /* dół */
        case 56: /* 8 */
            wskY = (wskY+1)%8;
            doPrzerysowania = true;
            break;
        case 49: /* 1 */
            wskX = (wskX+7)%8;
            wskY = (wskY+7)%8;
            doPrzerysowania = true;
            break;
        case 51: /* 3 */
            wskX = (wskX+1)%8;
            wskY = (wskY+7)%8;
            doPrzerysowania = true;
            break;
        case 55: /* 7 */
            wskX = (wskX+7)%8;
            wskY = (wskY+1)%8;
            doPrzerysowania = true;
            break;
        case 57: /* 9 */
            wskX = (wskX+1)%8;
            wskY = (wskY+1)%8;
            doPrzerysowania = true;
            break;
        case 53: /* 5 */
        case -5: /* środek */
            wskPole = wskY*8+wskX;
            int dotPole = dotY*8+dotX;
            wskBierka = stanGry.charAt(wskPole);
            char dotBierka;

            if (dotPole>=0)
                dotBierka = stanGry.charAt(dotPole);
            else
                dotBierka = '.';
            /*if (wziętyPion) // dostawiamy pion spoza planszy
            {
                pola[nrPolaY][nrPolaX]=wziętyPion;
                wziętyPion=false;
                // zapisz();
            }
            else*/
            if (dotX == wskX && dotY == wskY) // rezygnujemy z ruchu daną bierką
            {
                dotX=-1;
                dotY=-1;
            }
            else if
            (
                ( dotX==-1 && wskBierka!='.' ) || // dobywamy bierki, lub
                (
                  wskBierka!='.' && // na polu jest bierka
                  Character.isUpperCase(wskBierka) == // w tym samym kolorze, co dotknięta
                  Character.isUpperCase(dotBierka)
                )
            )
            {
                dotX = wskX;
                dotY = wskY;
            }
            else if (dotX != -1) // już wcześniej dotknęliśmy bierki
            {
                /*
                if (stanGry.charAt(wskPole)!='.') // bijemy
                    if (Character.isUpperCase(wskBierka)) // białą
                        zbiteB.textContent+=pola[nrPolaY][nrPolaX]+' ';
                    else
                        zbiteC.textContent+=pola[nrPolaY][nrPolaX]+' ';
                */
                String pomoc = stanGry.substring(0, wskPole)+dotBierka+stanGry.substring(wskPole+1, 64);
                pomoc = pomoc.substring(0, dotPole)+"."+pomoc.substring(dotPole+1, 64);
                ustaw(pomoc, false);
                dotX = -1;
                dotY = -1;
                // zapisz();
            }
            doPrzerysowania = true;
            break;
        case 42: /* gwiazdka */
        case 35: /* kratka */
        case 48: /* 0 */
            break;
        default:
            System.out.println("»"+Integer.toString(key)+"«");
            break;
        }
    }
    public void ustaw(String noweUstawienie, boolean wibruj)
    {
        if (wstrzasac && wibruj)
            Display.getDisplay(midlet).vibrate(250);
        if (blutufem)
            midlet.naSerwer(noweUstawienie);
        else
        {
            stanGryCof = stanGry;
            stanGry = noweUstawienie;
            doPrzerysowania = true;
        }
    }
    public void promuj()
    {
        int wskPole = wskY*8+wskX;
        char wskBierka = stanGry.charAt(wskPole);
        if (wskBierka!='.')
        {
            String kolejnoscBiale = "phwsgkp";
            String kolejnoscCzarne = "PHWSGKP";
            if (wskY==0 && Character.isUpperCase(wskBierka))
                ustaw(stanGry.substring(0, wskPole)+kolejnoscCzarne.charAt(kolejnoscCzarne.indexOf(wskBierka)+1)+stanGry.substring(wskPole+1, 64), false);
            else if (wskY==7 && !(Character.isUpperCase(wskBierka)))
                ustaw(stanGry.substring(0, wskPole)+kolejnoscBiale.charAt(kolejnoscBiale.indexOf(wskBierka)+1)+stanGry.substring(wskPole+1, 64), false);
            doPrzerysowania = true;
        }
    }
}
