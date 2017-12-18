package com.example.zijiguo.unionpaydemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.android.volley.NetworkResponse;
import android.app.Activity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;

import android.os.Handler;

import org.json.*;

import java.io.*;
import com.unionpay.UPPayAssistEx;
import android.app.ProgressDialog;

import java.util.HashMap;
import java.util.Calendar;
import java.util.Map;
import java.net.HttpURLConnection;

import com.android.volley.AuthFailureError;

public class MainActivity extends BaseActivity {
    @BindView(R.id.click)
    TextView Clickview;
    @BindView(R.id.textView)
    TextView Statusview;
    @BindView(R.id.editText_Amount)
    EditText editText_Amount;
    @BindView(R.id.editText_reference)
    EditText editText_reference;
    @BindView(R.id.editText_Currency)
    EditText editText_Currency;
    @BindView(R.id.editText_verndor)
    EditText editText_verndor;


    private int mGoodsIdx = 0;
    private Handler mHandler = null;
    private ProgressDialog mLoadingDialog = null;
    private final String mMode = "00";
    @Override
    public void doStartUnionPayPlugin(Activity activity, String tn, String mode) {
        if(UPPayAssistEx.checkInstalled(this)){
            UPPayAssistEx.startPay(activity, null, null, tn.trim(), mode);

        }else{
            Log.e("---------UPPay---------","noUppay installed");
        }

    }

    public void updateTextView(TextView tv) {
        String txt = "After";
        tv.setText(txt);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.click})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.click:
                editText_verndor.getText().toString().trim();
                editText_Currency .getText().toString().trim();
                editText_reference.getText().toString().trim();
                editText_Amount.getText().toString().trim();

                Log.e("editText_verndor",editText_verndor.getText().toString().trim());
                Log.e("editText_Currency",editText_Currency.getText().toString().trim());
                Log.e("editText_reference",editText_reference.getText().toString().trim());
                Log.e("editText_Amount",editText_Amount.getText().toString().trim());
                //gather transaction info and pass info to Nihaopay API
                applytoapi(editText_Amount.getText().toString().trim(),editText_verndor.getText().toString().trim(),editText_Currency.getText().toString().trim(),editText_reference.getText().toString().trim());
                break;
        }
    }

    private void applytoapi(final String amount,final String venrdor,final String currency,final String reference) {
        if (TextUtils.isEmpty(Config.TOKEN)) {
            new AlertDialog.Builder(this)
                    .setTitle("Warn")
                    .setMessage("Need config API TOKEN")
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    //
                                    finish();
                                }
                            }).show();
            return;
        }
        // Set current time as reference number
        Calendar calender = Calendar.getInstance();
        RequestQueue queue = Volley.newRequestQueue(this);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(Calendar.getInstance().getTime());
        // set Nihaopay API 
        String url = "https://api.nihaopay.com/v1.2/transactions/apppay";
        final JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("amount", amount);
        jsonBody.addProperty("currency", currency);
        jsonBody.addProperty("vendor", venrdor);
        jsonBody.addProperty("reference", reference);

        jsonBody.addProperty("ipn_url", "https://demo.nihaopay.com/ipn");
        final String requestBody = jsonBody.toString();
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Statusview.setText("NihaoPay Success");
                        try {
                            JSONObject obj = new JSONObject(response);
                           String orderinfo= (String)obj.get("orderInfo");
                            // Call UnionPay with NihaoPay orderinfo
                            doStartUnionPayPlugin(MainActivity.this, orderinfo,mMode);
                        } catch (JSONException e2) {
                            // returned data is not JSONObject?
                            e2.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // show error code on edittext "status"
                        Statusview.setText(res);
                        JSONObject obj = new JSONObject(res);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                }
            }

        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonBody.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //set up request header
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("authorization", "Bearer " + Config.TOKEN);
                return headers != null ? headers : super.getHeaders();
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                int statusCode = response.statusCode;
                switch (statusCode) {
                    case HttpURLConnection.HTTP_OK:
                        Log.e("Response Code", "SUCCESSE" + statusCode);
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        Log.e("Response Code", "Not found" + statusCode);
                        break;
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        //do stuff
                        Log.e("Response Code", "HTTP_INTERNAL_ERROR" + statusCode);
                        break;
                }

                return super.parseNetworkResponse(response);
            }
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
