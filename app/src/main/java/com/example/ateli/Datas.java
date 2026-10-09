package com.example.ateli;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Utilidades para datas vindas do MaterialDatePicker. */
public final class Datas {

    private Datas() { }

    /**
     * O MaterialDatePicker devolve a data em milissegundos no fuso UTC.
     * Por isso o formatador também usa UTC — senão a data aparece um dia antes.
     */
    public static String formatar(long millisUtc) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        formato.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formato.format(new Date(millisUtc));
    }

    /** Ex.: "sábado, 10 de outubro de 2026". */
    public static String formatarPorExtenso(long millisUtc) {
        SimpleDateFormat formato = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy",
                new Locale("pt", "BR"));
        formato.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formato.format(new Date(millisUtc));
    }
}
