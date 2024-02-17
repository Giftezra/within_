package com.example.within.helpers;

import android.content.Context;
import android.content.Intent;

public class NavigationManager {

    public static void navigateToPage(Context context, Class<?> destinationActivity ) {


        if (destinationActivity != null) {
            Intent intent = new Intent(context, destinationActivity);
            context.startActivity(intent);
        }
    }
}

