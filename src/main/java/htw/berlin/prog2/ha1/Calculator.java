package htw.berlin.prog2.ha1;

/**
 * Eine Klasse, die das Verhalten des Online Taschenrechners imitiert, welcher auf
 * https://www.online-calculator.com/ aufgerufen werden kann (ohne die Memory-Funktionen)
 * und dessen Bildschirm bis zu zehn Ziffern plus einem Dezimaltrennzeichen darstellen kann.
 * Enthält mit Absicht noch diverse Bugs oder unvollständige Funktionen.
 * 
 * Abweichungen: pressClearKey() löscht alle gespeicherten Werte, obwohl beim ersten mal Drücken nur der Bildschirm zurückgesetzt werden soll
 * pressEqualsKey(): Methode wirft Exception wenn man "=" drückt, ohne vorher eine Operation eingegeben zu haben. Es sollte aber eigentlich nichts passieren
 * 
 ** Änderungen / Bugfixes:
 * - pressClearKey(): implementiert CE einmaliges Löschen (nur Bildschirm) und bei erneutem Drücken kompletter Reset.
 * - pressEqualsKey(): macht nichts, wenn keine Operation gesetzt ist; unterstützt wiederholtes Drücken von '=';
 *   behandelt NaN/Infinity korrekt.
 * - Eingabelogik: startNewNumber-Flag ersetzt Vergleich mit latestValue.
 */
public class Calculator {

    public static void main(String[] args) {

    }

    private String screen = "0";

    private double latestValue;

    private String latestOperation = "";

    // neu: speichert letzter zweiter Operand für wiederholtes '='
    private double lastOperand = Double.NaN;

    // neu: Steuerflag: beim Start einer neuen Zahl (nach Operation oder nach '=')
    private boolean startNewNumber = false;

    // neu: um CE/C-Logik zu unterscheiden: wenn bereits "0" und Clear gedrückt -> komplett reset
    private boolean lastClearWasScreenOnly = false;

    /**
     * @return den aktuellen Bildschirminhalt als String
     */
    public String readScreen() {
        return screen;
    }

    /**
     * Empfängt den Wert einer gedrückten Zifferntaste. Da man nur eine Taste auf einmal
     * drücken kann muss der Wert positiv und einstellig sein und zwischen 0 und 9 liegen.
     * Führt in jedem Fall dazu, dass die gerade gedrückte Ziffer auf dem Bildschirm angezeigt
     * oder rechts an die zuvor gedrückte Ziffer angehängt angezeigt wird.
     * @param digit Die Ziffer, deren Taste gedrückt wurde
     */
    public void pressDigitKey(int digit) {
        if (digit > 9 || digit < 0) throw new IllegalArgumentException();

        // Wenn der Bildschirm gerade "0" anzeigt oder wir einen neuen Eingabewert starten:
        if (screen.equals("0") || startNewNumber) {
            screen = "";
            startNewNumber = false;
        }

        // Kontrolliere Maximallänge: ohne Punkt max 10, mit Punkt max 11 (Ziffern + Dezimalpunkt)
        int maxLen = screen.contains(".") ? 11 : 10;
        if (screen.length() >= maxLen) {
            // ignore additional digits
            return;
        }

        screen = screen + digit;
        lastClearWasScreenOnly = false;
    }

    /**
     * Empfängt den Befehl der C- bzw. CE-Taste (Clear bzw. Clear Entry).
     * Einmaliges Drücken der Taste löscht die zuvor eingegebenen Ziffern auf dem Bildschirm
     * so dass "0" angezeigt wird, jedoch ohne zuvor zwischengespeicherte Werte zu löschen.
     * Wird daraufhin noch einmal die Taste gedrückt, dann werden auch zwischengespeicherte
     * Werte sowie der aktuelle Operationsmodus zurückgesetzt, so dass der Rechner wieder
     * im Ursprungszustand ist.
     */
    public void pressClearKey() {
        if (!screen.equals("0") && !lastClearWasScreenOnly) {
            // erstes Drücken: nur Bildschirm löschen (CE)
            screen = "0";
            lastClearWasScreenOnly = true;
        } else {
            // zweites Drücken (oder wenn schon "0"): kompletter Reset (C)
            screen = "0";
            latestOperation = "";
            latestValue = 0.0;
            lastOperand = Double.NaN;
            lastClearWasScreenOnly = false;
            startNewNumber = false;
        }
    }

    /**
     * Empfängt den Wert einer gedrückten binären Operationstaste, also eine der vier Operationen
     * Addition, Substraktion, Division, oder Multiplikation, welche zwei Operanden benötigen.
     * Beim ersten Drücken der Taste wird der Bildschirminhalt nicht verändert, sondern nur der
     * Rechner in den passenden Operationsmodus versetzt.
     * Beim zweiten Drücken nach Eingabe einer weiteren Zahl wird direkt des aktuelle Zwischenergebnis
     * auf dem Bildschirm angezeigt. Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * @param operation "+" für Addition, "-" für Substraktion, "x" für Multiplikation, "/" für Division
     */
    public void pressBinaryOperationKey(String operation)  {
        // Wenn bereits eine Operation gesetzt ist und ein neuer Operand eingegeben wurde,
        // könnte man hier Zwischenberechnung durchführen. Für jetzt speichern wir den aktuellen Wert.
        latestValue = Double.parseDouble(screen);
        latestOperation = operation;
        startNewNumber = true;
        lastOperand = Double.NaN; // Reset des lastOperand, da eine neue Operation startet
        lastClearWasScreenOnly = false;
    }

    /**
     * Empfängt den Wert einer gedrückten unären Operationstaste, also eine der drei Operationen
     * Quadratwurzel, Prozent, Inversion, welche nur einen Operanden benötigen.
     * Beim Drücken der Taste wird direkt die Operation auf den aktuellen Zahlenwert angewendet und
     * der Bildschirminhalt mit dem Ergebnis aktualisiert.
     * @param operation "√" für Quadratwurzel, "%" für Prozent, "1/x" für Inversion
     */
    public void pressUnaryOperationKey(String operation) {
        latestValue = Double.parseDouble(screen);
        // latestOperation muss für unary nicht gesetzt werden
        double result;
        switch (operation) {
            case "√" -> result = Math.sqrt(Double.parseDouble(screen));
            case "%" -> result = Double.parseDouble(screen) / 100.0;
            case "1/x" -> {
                double v = Double.parseDouble(screen);
                result = 1.0 / v;
            }
            default -> throw new IllegalArgumentException();
        }

        if (Double.isNaN(result) || Double.isInfinite(result)) {
            screen = "Error";
        } else {
            screen = Double.toString(result);
            // Entferne .0 bei ganzen Zahlen, wie vorher
            if (screen.endsWith(".0")) screen = screen.substring(0, screen.length() - 2);
            // Trunkiere korrekt: mit Punkt max 11 Zeichen, ohne Punkt max 10 Zeichen
            if (screen.contains(".")) {
                if (screen.length() > 11) screen = screen.substring(0, 11);
            } else {
                if (screen.length() > 10) screen = screen.substring(0, 10);
            }
        }
        startNewNumber = true;
        lastClearWasScreenOnly = false;
    }

    /**
     * Empfängt den Befehl der gedrückten Dezimaltrennzeichentaste, im Englischen üblicherweise "."
     * Fügt beim ersten Mal Drücken dem aktuellen Bildschirminhalt das Trennzeichen auf der rechten
     * Seite hinzu und aktualisiert den Bildschirm. Daraufhin eingegebene Zahlen werden rechts vom
     * Trennzeichen angegeben und daher als Dezimalziffern interpretiert.
     * Beim zweimaligem Drücken, oder wenn bereits ein Trennzeichen angezeigt wird, passiert nichts.
     */
    public void pressDotKey() {
        if (!screen.contains(".")) {
            // Wenn wir einen neuen Eingabewert starten (z. B. nach Operation), zeigen wir "0." an
            if (startNewNumber || screen.equals("0")) {
                screen = "0.";
            } else {
                screen = screen + ".";
            }
            startNewNumber = false;
        }
        lastClearWasScreenOnly = false;
    }

    /**
     * Empfängt den Befehl der gedrückten Vorzeichenumkehrstaste ("+/-").
     * Zeigt der Bildschirm einen positiven Wert an, so wird ein "-" links angehängt, der Bildschirm
     * aktualisiert und die Inhalt fortan als negativ interpretiert.
     * Zeigt der Bildschirm bereits einen negativen Wert mit führendem Minus an, dann wird dieses
     * entfernt und der Inhalt fortan als positiv interpretiert.
     */
    public void pressNegativeKey() {
        screen = screen.startsWith("-") ? screen.substring(1) : "-" + screen;
        lastClearWasScreenOnly = false;
    }

    /**
     * Empfängt den Befehl der gedrückten "="-Taste.
     * Wurde zuvor keine Operationstaste gedrückt, passiert nichts.
     * Wurde zuvor eine binäre Operationstaste gedrückt und zwei Operanden eingegeben, wird das
     * Ergebnis der Operation angezeigt. Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * Wird die Taste weitere Male gedrückt (ohne andere Tasten dazwischen), so wird die letzte
     * Operation (ggf. inklusive letztem Operand) erneut auf den aktuellen Bildschirminhalt angewandt
     * und das Ergebnis direkt angezeigt.
     */
    public void pressEqualsKey() {
        if (latestOperation == null || latestOperation.isEmpty()) {
            // Laut JavaDoc: passiert nichts, wenn keine Operation gesetzt wurde.
            return;
        }

        double rightOperand;
        if (Double.isNaN(lastOperand)) {
            // erstes '=': rechter Operand ist aktuelle Anzeige
            rightOperand = Double.parseDouble(screen);
        } else if (startNewNumber) {
            // Falls der Benutzer gerade eine neue Zahl gestartet hat, verwenden wir diese als rightOperand
            rightOperand = Double.parseDouble(screen);
        } else {
            // wiederholtes '=': rechter Operand ist lastOperand
            rightOperand = lastOperand;
        }

        double result;
        switch (latestOperation) {
            case "+" -> result = latestValue + rightOperand;
            case "-" -> result = latestValue - rightOperand;
            case "x" -> result = latestValue * rightOperand;
            case "/" -> result = latestValue / rightOperand;
            default -> throw new IllegalArgumentException();
        }

        if (Double.isNaN(result) || Double.isInfinite(result)) {
            screen = "Error";
            // nach Error ist es sinnvoll, Zustand zurückzusetzen
            latestOperation = "";
            latestValue = 0.0;
            lastOperand = Double.NaN;
            startNewNumber = true;
            lastClearWasScreenOnly = false;
            return;
        }

        // Formatierung: entfernen von ".0" bei Ganzzahlen
        screen = Double.toString(result);
        if (screen.endsWith(".0")) screen = screen.substring(0, screen.length() - 2);

        // Trunkiere korrekt: mit Punkt max 11 Zeichen, ohne Punkt max 10 Zeichen
        if (screen.contains(".")) {
            if (screen.length() > 11) screen = screen.substring(0, 11);
        } else {
            if (screen.length() > 10) screen = screen.substring(0, 10);
        }

        // Für wiederholte '=' speichern:
        lastOperand = rightOperand;
        latestValue = result;
        startNewNumber = true;
        lastClearWasScreenOnly = false;
    }
}