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
        boolean isHIBC = !isGTIN && decodedHIBC != null && decodedHIBC.getError() == null;

        barcodeType = isGTIN ? BarcodeType.GTIN : isHIBC ? BarcodeType.HIBC : BarcodeType.STANDARD;
    }

    public BarcodeType getBarcodeType(){
        return barcodeType;
    }

    @Nullable
    public String getItemCode() {
        if (barcodeType == BarcodeType.GTIN) {
            String code = resultGTIN.getString(ApplicationIdentifier.GTIN);
            if(code == null)
                code = resultGTIN.getString(ApplicationIdentifier.CONTAINED_GTIN);

            return code == null ? resultGTIN.getString(ApplicationIdentifier.SSCC) : code;
        }
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
        if (barcodeType == BarcodeType.GTIN) {
            String serialNumber = resultGTIN.getString(ApplicationIdentifier.SERIAL_NUMBER);
            return serialNumber == null ? resultGTIN.getString(ApplicationIdentifier.BATCH_OR_LOT_NUMBER) : serialNumber;
        }
        if (barcodeType == BarcodeType.HIBC)
            return decodedHIBC.getProperty() == HIBC.PropertyType.LOT ? decodedHIBC.getPropertyValue() : null;

        return null;
    }

    public int getQuantity(){
        if(barcodeType == BarcodeType.GTIN){
            String quantity = resultGTIN.getString(ApplicationIdentifier.COUNT_OF_TRADE_ITEMS);
            return quantity == null ? 0 : Integer.parseInt(quantity);
        }

        return 0;
    }

    public String getBarcode() {
        return barcode;
    }
}
