package com.example.within.calls;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * The pricing class only handles the logics for the call prices. these logics makes sure the phone number the user wants
 * to call is listed for calls on our systems. if the country is listed so would the pricing . the class aids the display of this to the user
 * before the user is allowed to make a call, they would be shown a screen showing them how much they would be charged to make calls to that
 * destination per minute.
* */
public class PricingModel extends AppCompatActivity{
    private final String TAG = "Pricing";
    private final Map<String, Double> countryAndPricing ;
    private final Map<String, String> countryAndDialCodeMap; // Maps the country and the country code which is the first 3 or two numbers of the number


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /*
     * private method to get the country name based on the phone number.
     * the method extracts the first three number matching it against the
     * values of the countryAndCountryCodeMap
     * */
    private String checkPhoneNumber (String phoneNumber){
        if (phoneNumber != null && phoneNumber.length() > 3){
            String firstThree = phoneNumber.substring(0, 3); // Get the first four values excluding the + sign
            // loop through the key value pair of the map to get all its children
            // elements then match the first three numbers against the value to get the
            // keys
            if (countryAndDialCodeMap != null) {
                for (Map.Entry<String, String> dailCodeEntry: countryAndDialCodeMap.entrySet()) {
                    String dialCode = dailCodeEntry.getValue();
                    if (dialCode != null && dialCode.equals(phoneNumber.substring(0, 3)) ||
                            dialCode.equals(phoneNumber.substring(0, 4)) ||
                            dialCode.equals(phoneNumber.substring(0, 2))) {
                        return dailCodeEntry.getKey();
                    }
                }
            }
        }
        return null;
    }

    /*
     * Method checks the pricing based on the country returned from the checkPhoneNumber method.
     * the country is used to search the checkPricing map to get the value of the country key
     * if the country matches the value of the checkPricing would be a double value for how much it cost
     * to make a call to the destination.
     * the method returns the found  country with its value*/
    Map<String, Double> checkPricing (String phoneNumber){
        String country = checkPhoneNumber(phoneNumber); // Helper method to check the country dial code and returns a country if the first three numbers match a dialcode
        System.out.println(country);

        if (country != null && countryAndPricing != null){
            // If country returns a true string values uses a loop to iterate the map
            // for countryPricing
            for (Map.Entry<String, Double> pricingModel: countryAndPricing.entrySet()) {
                if (pricingModel.getKey().equals(country)){
                    // Return a map containing only the selected country and its pricing
                    Map<String, Double> selectedCountryPricing = new HashMap<>();
                    selectedCountryPricing.put(pricingModel.getKey(), pricingModel.getValue());
                    return selectedCountryPricing;
                }
            }
        }else {
            System.out.println("Couldnt retrieve a valid country from number provided");
        }
        return null;
    }


    public void readCsvFile (){

    }


 public PricingModel(){

     // Initializes a set of maps with key value pairs for the countries
     // and their country code which is the first two or three numbers of the
     // phone number.... this will be used to check the phone number of the recipient
     // and display the amount that will be charged based on the country
     countryAndDialCodeMap = new HashMap<>();
     countryAndPricing = new HashMap<>();

     countryAndDialCodeMap.put("United states", "+1"); countryAndDialCodeMap.put("United kingdom", "+44");
     countryAndDialCodeMap.put("gambia", "");countryAndDialCodeMap.put("Albania", "+355");
     countryAndDialCodeMap.put("Andorra", "+376");countryAndDialCodeMap.put("Armenia", "+374");
     countryAndDialCodeMap.put("Austria", "+43");countryAndDialCodeMap.put("Azerbaijan", "+994");
     countryAndDialCodeMap.put("Belarus", "+375");countryAndDialCodeMap.put("Belgium", "+32");
     countryAndDialCodeMap.put("Bosnia and Herzegovina", "+387");countryAndDialCodeMap.put("Bulgaria", "+359");
     countryAndDialCodeMap.put("Croatia", "+385");
     countryAndDialCodeMap.put("Cyprus", "+357");
     countryAndDialCodeMap.put("Czech Republic", "+420");
     countryAndDialCodeMap.put("Denmark", "+45");
     countryAndDialCodeMap.put("Estonia", "+372");
     countryAndDialCodeMap.put("Finland", "+358");
     countryAndDialCodeMap.put("France", "+33");
     countryAndDialCodeMap.put("Georgia", "+995");
     countryAndDialCodeMap.put("Germany", "+49");
     countryAndDialCodeMap.put("Greece", "+30");
     countryAndDialCodeMap.put("Hungary", "+36");
     countryAndDialCodeMap.put("Iceland", "+354"); countryAndDialCodeMap.put("Ireland", "+353");
     countryAndDialCodeMap.put("Italy", "+39");
     countryAndDialCodeMap.put("Kazakhstan", "+7");
     countryAndDialCodeMap.put("Kosovo", "+383");
     countryAndDialCodeMap.put("Latvia", "+371");
     countryAndDialCodeMap.put("Liechtenstein", "+423");
     countryAndDialCodeMap.put("Lithuania", "+370");
     countryAndDialCodeMap.put("Luxembourg", "+352");
     countryAndDialCodeMap.put("Malta", "+356");
     countryAndDialCodeMap.put("Slovenia", "+386");
     countryAndDialCodeMap.put("Moldova", "+373");
     countryAndDialCodeMap.put("Monaco", "+377");
     countryAndDialCodeMap.put("Montenegro", "+382");
     countryAndDialCodeMap.put("Netherlands", "+31");
     countryAndDialCodeMap.put("North Macedonia", "+389");
     countryAndDialCodeMap.put("Norway", "+47");
     countryAndDialCodeMap.put("Poland", "+48");
     countryAndDialCodeMap.put("Portugal", "+351");
     countryAndDialCodeMap.put("Romania", "+40");
     countryAndDialCodeMap.put("Russia", "+7");
     countryAndDialCodeMap.put("San Marino", "+378");
     countryAndDialCodeMap.put("Serbia", "+381");
     countryAndDialCodeMap.put("Slovakia", "+421");
     countryAndDialCodeMap.put("Spain", "+34");
     countryAndDialCodeMap.put("Sweden", "+46");
     countryAndDialCodeMap.put("Switzerland", "+41");
     countryAndDialCodeMap.put("Turkey", "+90");
     countryAndDialCodeMap.put("Ukraine", "+380");
     countryAndDialCodeMap.put("Vatican City", "+379"); countryAndDialCodeMap.put("Algeria", "+213");
     countryAndDialCodeMap.put("Angola", "+244");
     countryAndDialCodeMap.put("Benin", "+229");
     countryAndDialCodeMap.put("Botswana", "+267");
     countryAndDialCodeMap.put("Burkina Faso", "+226");
     countryAndDialCodeMap.put("Burundi", "+257");
     countryAndDialCodeMap.put("Cabo Verde", "+238");
     countryAndDialCodeMap.put("Cameroon", "+237");
     countryAndDialCodeMap.put("Central African Republic", "+236");
     countryAndDialCodeMap.put("Chad", "+235");
     countryAndDialCodeMap.put("Comoros", "+269");
     countryAndDialCodeMap.put("Congo", "+242");
     countryAndDialCodeMap.put("Djibouti", "+253");
     countryAndDialCodeMap.put("Egypt", "+20");
     countryAndDialCodeMap.put("Equatorial Guinea", "+240");
     countryAndDialCodeMap.put("Eritrea", "+291");
     countryAndDialCodeMap.put("Eswatini", "+268");
     countryAndDialCodeMap.put("Ethiopia", "+251");
     countryAndDialCodeMap.put("Gabon", "+241");
     countryAndDialCodeMap.put("Gambia", "+220");
     countryAndDialCodeMap.put("Ghana", "+233");
     countryAndDialCodeMap.put("Guinea", "+224");
     countryAndDialCodeMap.put("Guinea-Bissau", "+245");
     countryAndDialCodeMap.put("Ivory Coast", "+225");
     countryAndDialCodeMap.put("Kenya", "+254");
     countryAndDialCodeMap.put("Lesotho", "+266");
     countryAndDialCodeMap.put("Liberia", "+231");
     countryAndDialCodeMap.put("Libya", "+218");
     countryAndDialCodeMap.put("Madagascar", "+261");
     countryAndDialCodeMap.put("Malawi", "+265");
     countryAndDialCodeMap.put("Mali", "+223");
     countryAndDialCodeMap.put("Mauritania", "+222");
     countryAndDialCodeMap.put("Mauritius", "+230");
     countryAndDialCodeMap.put("Morocco", "+212");
     countryAndDialCodeMap.put("Mozambique", "+258");
     countryAndDialCodeMap.put("Namibia", "+264");
     countryAndDialCodeMap.put("Niger", "+227");
     countryAndDialCodeMap.put("Nigeria", "+234");
     countryAndDialCodeMap.put("Rwanda", "+250");
     countryAndDialCodeMap.put("Sao Tome and Principe", "+239");
     countryAndDialCodeMap.put("Senegal", "+221");
     countryAndDialCodeMap.put("Seychelles", "+248");
     countryAndDialCodeMap.put("Sierra Leone", "+232");
     countryAndDialCodeMap.put("Somalia", "+252");
     countryAndDialCodeMap.put("South Africa", "+27");
     countryAndDialCodeMap.put("South Sudan", "+211");
     countryAndDialCodeMap.put("Sudan", "+249");
     countryAndDialCodeMap.put("Tanzania", "+255");
     countryAndDialCodeMap.put("Togo", "+228");
     countryAndDialCodeMap.put("Tunisia", "+216");
     countryAndDialCodeMap.put("Uganda", "+256");
     countryAndDialCodeMap.put("Zambia", "+260");
     countryAndDialCodeMap.put("Zimbabwe", "+263");

     countryAndPricing.put("United states", 0.21); countryAndPricing.put("United kingdom", 0.34);countryAndPricing.put("gambia", 0.45);
     countryAndPricing.put("Albania", 0.33); countryAndPricing.put("Andorra", 0.55); countryAndPricing.put("Armenia", 0.56);
     countryAndPricing.put("Nigeria", 0.90);countryAndPricing.put("Azerbaijan", 1.0);
     countryAndPricing.put("Belarus", 0.22);countryAndPricing.put("Belgium", 0.11);
 }

}
