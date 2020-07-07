package com.quayo.barcodeutils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hibcc.HIBC;
import com.injoin.gs1utils.ApplicationIdentifier;
import com.injoin.gs1utils.ElementStrings;

import java.util.Calendar;
import java.util.Date;

public class BarcodeReader {
    private final String barcode;
    private final @NonNull
    ElementStrings.ParseResult resultGTIN;
    private final @Nullable
    HIBC.Decoded decodedHIBC;
    private final boolean isGTIN;
    private final boolean isHIBC;

    public BarcodeReader(String barcode) {
        this.barcode = barcode;

        resultGTIN = ElementStrings.parse(barcode);
        isGTIN = !resultGTIN.isPartial() && !resultGTIN.isEmpty();

        decodedHIBC = isGTIN ? null : new HIBC().decode(barcode);
        isHIBC = !isGTIN && decodedHIBC != null;
    }

    public boolean isGTIN() {
        return isGTIN;
    }

    public boolean isHIBC() {
        return isHIBC;
    }

    public boolean isStandardBarcode() {
        return !isGTIN && !isHIBC;
    }

    public String getItemCode() {
        if (isGTIN)
            return resultGTIN.getString(ApplicationIdentifier.GTIN);
        if (isHIBC)
            return decodedHIBC.getProduct();
        return barcode;
    }

    @Nullable
    public Date getExpiryDate() {
        if (isGTIN)
            return resultGTIN.getDate(ApplicationIdentifier.EXPIRATION_DATE);
        if (isHIBC) {
            String date = decodedHIBC.getDate();

            if (date == null || date.length() != 4)
                return null;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(0, 2)));
            calendar.set(Calendar.YEAR, 2000 + Integer.parseInt(date.substring(2)));

            return calendar.getTime();
        }

        return null;
    }

    @Nullable
    public String getLot() {
        if (isGTIN)
            return resultGTIN.getString(ApplicationIdentifier.BATCH_OR_LOT_NUMBER);
        if (isHIBC)
            return decodedHIBC.getProperty() == HIBC.PropertyType.LOT ? decodedHIBC.getPropertyValue() : null;

        return null;
    }

    public String getBarcode() {
        return barcode;
    }
}
