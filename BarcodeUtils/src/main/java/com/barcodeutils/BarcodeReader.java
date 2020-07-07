package com.barcodeutils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hibcc.HIBC;
import se.injoin.gs1utils.ApplicationIdentifier;
import se.injoin.gs1utils.ElementStrings;

import java.util.Calendar;
import java.util.Date;

public class BarcodeReader {
    private final String barcode;
    private final BarcodeType barcodeType;
    private final @NonNull
    ElementStrings.ParseResult resultGTIN;
    private final @Nullable
    HIBC.Decoded decodedHIBC;

    public BarcodeReader(String barcode) {
        this.barcode = barcode;

        resultGTIN = ElementStrings.parse(barcode);
        boolean isGTIN = !resultGTIN.isPartial() && !resultGTIN.isEmpty();

        decodedHIBC = isGTIN ? null : new HIBC().decode(barcode);
        boolean isHIBC = !isGTIN && decodedHIBC != null;

        barcodeType = isGTIN ? BarcodeType.GTIN : isHIBC ? BarcodeType.HIBC : BarcodeType.STANDARD;
    }

    public BarcodeType getBarcodeType(){
        return barcodeType;
    }

    public String getItemCode() {
        if (barcodeType == BarcodeType.GTIN)
            return resultGTIN.getString(ApplicationIdentifier.GTIN);
        if (barcodeType == BarcodeType.HIBC)
            return decodedHIBC.getProduct();
        return barcode;
    }

    @Nullable
    public Date getExpiryDate() {
        if (barcodeType == BarcodeType.GTIN)
            return resultGTIN.getDate(ApplicationIdentifier.EXPIRATION_DATE);
        if (barcodeType == BarcodeType.HIBC) {
            String date = decodedHIBC.getDate();

            if (date == null || date.length() != 4)
                return null;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(0, 2)));
            calendar.set(Calendar.YEAR, 2000 + Integer.parseInt(date.substring(2)));

            return calendar.getTime();
        }

        return null;
    }

    @Nullable
    public String getLot() {
        if (barcodeType == BarcodeType.GTIN)
            return resultGTIN.getString(ApplicationIdentifier.BATCH_OR_LOT_NUMBER);
        if (barcodeType == BarcodeType.HIBC)
            return decodedHIBC.getProperty() == HIBC.PropertyType.LOT ? decodedHIBC.getPropertyValue() : null;

        return null;
    }

    public String getBarcode() {
        return barcode;
    }
}
