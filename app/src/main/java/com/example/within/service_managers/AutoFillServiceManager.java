package com.example.within.service_managers;

import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AutoFillServiceManager extends AutofillService {

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal, FillCallback callback) {
        // Get the structure from the request
        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Traverse the structure looking for nodes to fill out
        ParsedStructure parsedStructure = parseStructure(structure);

        // Fetch user data that matches the fields
        UserData userData = fetchUserData(parsedStructure);

        // Build the presentation of the datasets
        RemoteViews emailPresentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        emailPresentation.setTextViewText(android.R.id.text1, "email");

        RemoteViews passwordPresentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        passwordPresentation.setTextViewText(android.R.id.text1, "password for email");

        // Add a dataset to the response
        FillResponse fillResponse = new FillResponse.Builder()
                .addDataset(new Dataset.Builder()
                        .setValue(parsedStructure.emailId,
                                AutofillValue.forText(userData.email), emailPresentation)
                        .setValue(parsedStructure.passwordId,
                                AutofillValue.forText(userData.password), passwordPresentation)
                        .build())
                .build();

        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse);
    }

    private ParsedStructure parseStructure(AssistStructure structure) {
        ParsedStructure parsedStructure = new ParsedStructure();

        // Implement your logic to traverse the structure and find relevant AutofillIds
        // For example, you might look for views with specific autofill hints
        // Here is a simple example:
        traverseStructure(structure);

        return parsedStructure;
    }

    private UserData fetchUserData(ParsedStructure parsedStructure) {
        UserData userData = new UserData();

        // Implement your logic to fetch user data based on the parsed structure
        // For example, you might retrieve the email and password from a database
        // Here is a simple example:
        userData.email = "user@example.com"; userData.password = "secure_password";
        userData.confirmPassword= "confirm_password"; userData.phone = "phone";
        userData.firstName = "enigma"; userData.lastName = "ross";


        return userData;
    }


    public void traverseStructure(AssistStructure structure) {
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            traverseNode(viewNode);
        }
    }

    public void traverseNode(AssistStructure.ViewNode viewNode) {
        if(viewNode.getAutofillHints() != null && viewNode.getAutofillHints().length > 0) {
            // If the client app provides autofill hints, you can obtain them using
            // viewNode.getAutofillHints();
        } else {
            // Or use your own heuristics to describe the contents of a view
            // using methods such as getText() or getHint()
        }

        for(int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            traverseNode(childNode);
        }
    }


    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        // Get the structure from the request
        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Traverse the structure looking for data to save
        traverseStructure(structure);

        // Persist the data - if there are no errors, call onSuccess()
        callback.onSuccess();
    }




    class ParsedStructure {
        AutofillId emailId; AutofillValue phone; AutofillValue firstName;
        AutofillValue lastName; AutofillId passwordId; AutofillValue confirmPassword;
    }

    class UserData {
        String email; String phone; String firstName;
        String lastName; String password; String confirmPassword;
    }

}
