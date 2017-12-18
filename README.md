# unionpay
UnionPay demo with NihaoPay API integrated -Andorid Studio <br />
Send request to NihaoPay's API and call UnionPay APK with nihaopay's response. 

Input: <br />
Amount, Total amount of purchase product.<br />
Currency, chose from USD, JPY, GBP, EUR, HKD, CAD.<br />
Vendor, unionpay only. <br />
reference number, should be an unique alphanumeric string up to 30 characters.<br />

See https://docs.nihaopay.com/api/v1.2/en/ for detail.<br />
Replace the token number in config.java to start demo.<br />
Click the "click" button to start a request.<br />

Output:<br />
Status: show reqeust status.<br />

# Demo Configuration
Android studio minSDK version 23<br />
Grails version :2.4.3

# Demo
 see unionpay/app/src/main/java/com/example/zijiguo/unionpaydemo/MainActivity.java for detail <br />
 import project into your workspace, and edit MainActivity.java to match your requirement. Do not change other files. <br />
