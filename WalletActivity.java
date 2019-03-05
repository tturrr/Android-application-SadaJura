package com.example.user.sadajura;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;

public class WalletActivity extends AppCompatActivity {
    WalletCreate wc = new WalletCreate();
    String url = config.addressethnode();
    Web3j web3 = Web3jFactory.build(new HttpService(url));
    String smartcontract = config.addresssmartcontract();
    String passwordwallet = config.passwordwallet();
    File DataDir;
    TextView ethaddress, ethbalance, tokensymbol, tokenbalance, tokensymbolbalance;
    TextView tv_gas_limit, tv_gas_price, tv_fee;
    EditText sendtoaddress, sendtokenvalue, sendethervalue;
    ImageView qr_small, qr_big,back_btn;
    BigInteger GasPrice, GasLimit;
    final Context context = this;
    IntentIntegrator qrScan;
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        back_btn = (ImageView)findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WalletActivity.this,ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                finish();
                Intent intent = new Intent(WalletActivity.this,WalletActivity.class);
                startActivity(intent);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        ethaddress = (TextView) findViewById(R.id.ethaddress); // Your Ether Address
        ethbalance = (TextView) findViewById(R.id.ethbalance); // Your Ether Balance
        tokensymbol = (TextView) findViewById(R.id.tokensymbol); // Token Symbol
        tokenbalance = (TextView) findViewById(R.id.tokenbalance); // Token Balance
        tokensymbolbalance = (TextView) findViewById(R.id.tokensymbolbalance);
        sendtoaddress = (EditText) findViewById(R.id.sendtoaddress); // Address for sending ether or token
        sendtokenvalue = (EditText) findViewById(R.id.SendTokenValue); // Ammount token for sending
        sendethervalue = (EditText) findViewById(R.id.SendEthValue); // Ammount ether for sending
        qr_small = (ImageView)findViewById(R.id.qr_small);
        qrScan = new IntentIntegrator(this);

        tv_gas_limit = (TextView) findViewById(R.id.tv_gas_limit);
        tv_gas_price = (TextView) findViewById(R.id.tv_gas_price);
        tv_fee = (TextView) findViewById(R.id.tv_fee);
        final SeekBar sb_gas_limit = (SeekBar) findViewById(R.id.sb_gas_limit);
        sb_gas_limit.setOnSeekBarChangeListener(seekBarChangeListenerGL);
        final SeekBar sb_gas_price = (SeekBar) findViewById(R.id.sb_gas_price);
        sb_gas_price.setOnSeekBarChangeListener(seekBarChangeListenerGP);
        GetFee();
        /**
         * Получаем полный путь к каталогу с ключами
         * Get the full path to the directory with the keys
         */
        DataDir = this.getExternalFilesDir("/keys/");
        File KeyDir = new File(String.valueOf(DataDir));
        /**
         * Проверяем есть ли кошельки
         * Check whether there are purses
         */
        File[] listfiles = KeyDir.listFiles();
        if (listfiles.length == 0 ) {
            /**
             * 지갑을 스토리지안에 만든다
             */
            try {
                String fileName = WalletUtils.generateNewWalletFile(passwordwallet, DataDir, false);
                System.out.println("FileName: " + DataDir.toString());
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else {
            /**
             * Если кошелек создан, начинаем выполнение потока
             * 지갑주소의 월레주소를 가지고 온다.
             */
            wc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    ///////////////////// QR Generation //////////////////////
    /**
     * QR генерация Ether Адреса
     * qr코드로 이더리움 주소를 챙겨온다.
     */
    public Bitmap QRGen(String Value, int Width, int Heigth) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(Value, BarcodeFormat.DATA_MATRIX.QR_CODE, Width, Heigth);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    ////////////////// END QR Generation ////////////////////
    ///////////////////// QR SCAN ///////////////////////////
    /**
     * QR сканирование Ether Адреса
     * QR scan Ether Address
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                sendtoaddress.setText(result.getContents());
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //////////////////// END QR SCAN ////////////////////////

    /////////////////// SeekBar Listener ////////////////////
    /**
     * SeekBar Слушатель
     * SeekBar Listener
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGL = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasLimit(String.valueOf(seekBar.getProgress()*1000+201000));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGP = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasPrice(String.valueOf(seekBar.getProgress()+40));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };
    ///////////////// END SeekBar Listener /////////////////

    ///////////////////// Gas View /////////////////////////

    /**
     * Значение присваивается визуальным элементам
     * The value is assigned to the visual elements
     * @param value Value Gas Limit and Gas Price
     */
    public void GetGasLimit(String value) {
        tv_gas_limit.setText(value);
        GetFee();
    }
    public void GetGasPrice(String value) {
        tv_gas_price.setText(value);
        GetFee();
    }
    /////////////////////////////////////////////////////////////////

    /////////////////////////// Get Fee /////////////////////////////

    /**
     * Значение GazLimit и GasPrice конвертируеться в BigInteger и присваиваеться глобальным переменным
     * The value GazLimit and GasPrice converteres in BigInteger and prizhivaetsya global variables
     *
     * Расчет вознагрождения для майнеров
     * calculate the fee for miners
     */

    public void GetFee(){
        GasPrice = Convert.toWei(tv_gas_price.getText().toString(),Convert.Unit.GWEI).toBigInteger();
        GasLimit = BigInteger.valueOf(Integer.valueOf(String.valueOf(tv_gas_limit.getText())));

        // fee
        BigDecimal fee = BigDecimal.valueOf(GasPrice.doubleValue()*GasLimit.doubleValue());
        BigDecimal feeresult = Convert.fromWei(fee.toString(),Convert.Unit.ETHER);
        tv_fee.setText(feeresult.toPlainString() + " ETH");
    }
    ///////////////////////// End Get Fee ///////////////////////////

    /////////////////////// On Click /////////////////////////
    /**
     * Начать выполнение потока для отправки эфира или Токена
     * Start executing thread for sending Ether or sending Token
     */
    public void onClick(View view) {
        SendingToken st = new SendingToken();
        SendingEther se = new SendingEther();
        switch (view.getId()) {
            case R.id.SendEther:
                se.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.SendToken:
                st.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.qr_small:
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.qr_view);
                qr_big = (ImageView) dialog.findViewById(R.id.qr_big);
                qr_big.setImageBitmap(QRGen(ethaddress.getText().toString(), 600, 600));
                dialog.show();
                break;
            case R.id.qrScan:
                qrScan.setOrientationLocked(false);
                qrScan.setBarcodeImageEnabled(true);
                qrScan.initiateScan();
                break;
        }

    }
    /////////////////////// end on click /////////////////////
    ///////////////////// Create and Load Wallet /////////////////
    public class WalletCreate extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));
            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);

                /**
                 // Получаем Баланс
                 // Get balance Ethereum
                 */
                EthGetBalance etherbalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalance = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                System.out.println("Eth Balance: " + ethbalance);

                /**
                 // Загружаем Токен
                 // Download Token
                 */
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GasPrice, GasLimit);

                /**
                 // Получаем название токена
                 // Get the name of the token
                 */
                String tokenname = token.name().send();
                System.out.println("Token Name: " + tokenname);

                /**
                 // Получаем Символ Токена
                 // Get Symbol marking token
                 */
                String tokensymbol = token.symbol().send();
                System.out.println("Symbol Token: " + tokensymbol);

                /**
                 // Получаем адрес Токена
                 // Get The Address Token
                 */
                String tokenaddress = token.getContractAddress();
                System.out.println("Address Token: " + tokenaddress);

                /**
                 // Получаем общее количество выпускаемых токенов
                 // Get the total amount of issued tokens
                 */
                BigInteger totalSupply = token.totalSupply().send();
                System.out.println("Supply Token: "+totalSupply.toString());

                /**
                 // Получаем количество токенов в кошельке
                 // Receive the Balance of Tokens in the wallet
                 */
                BigInteger tokenbalance = token.balanceOf(address).send();
                System.out.println("Balance Token: "+ tokenbalance.toString());

                JSONObject result = new JSONObject();
                result.put("ethaddress",address);
                result.put("ethbalance", ethbalance);
                result.put("tokenbalance", tokenbalance.toString());
                result.put("tokenname", tokenname);
                result.put("tokensymbol", tokensymbol);
                result.put("tokenaddress",tokenaddress);
                result.put("tokensupply", totalSupply.toString());
                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (result != null ){
                try {
                    ethaddress.setText(result.get("ethaddress").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    ethbalance.setText(result.get("ethbalance").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    tokensymbol.setText(result.get("tokensymbol").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    tokenbalance.setText(result.get("tokenbalance").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    tokensymbolbalance.setText(" "+result.get("tokensymbol").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    qr_small.setImageBitmap(QRGen(result.get("ethaddress").toString(), 400, 400));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Error!!!");
            }

        }
    }
    ////////////////// End create and load wallet ////////////////

    ///////////////////////// Sending Tokens /////////////////////
    public class SendingToken extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... param) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));

            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);
                /**
                 * Загружаем Токен
                 * Load Token
                 */
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GasPrice, GasLimit);

                String status = null;
                String balance = null;

                /**
                 * Конвертируем сумму токенов в BigInteger и отправляем на указанные адрес
                 * Convert the amount of tokens to BigInteger and send to the specified address
                 */
                BigInteger sendvalue = BigInteger.valueOf(Long.parseLong(String.valueOf(sendtokenvalue.getText())) * 1000000000);
                BigInteger dsf = sendvalue.multiply(BigInteger.valueOf(1000000000));
                status = token.transfer(String.valueOf(sendtoaddress.getText()), dsf).send().getTransactionHash();
                /**
                 * Обновляем баланс Токенов
                 * Renew Token balance
                 */
                BigInteger tokenbalance = token.balanceOf(address).send();
                System.out.println("Balance Token: "+ tokenbalance.toString());
                balance = tokenbalance.toString();

                /**
                 * Возвращаем из потока, Статус транзакции и баланс Токенов
                 * Returned from thread, transaction Status and Token balance
                 */
                JSONObject result = new JSONObject();
                result.put("status",status);
                result.put("balance",balance);

                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (result != null) {
                try {
                    tokenbalance.setText(result.get("balance").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast toast = null;
                try {
                    toast = Toast.makeText(getApplicationContext(),result.get("status").toString(), Toast.LENGTH_LONG);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                toast.show();
            } else {System.out.println();}
        }
    }
    /////////////////////// End Sending Tokens ///////////////////

    ///////////////////////// Sending Ether //////////////////////
    public class SendingEther  extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(Void... param) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));

            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);

                /**
                 * Получаем счетчик транзакций
                 * Get count transaction
                 */
                EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                BigInteger nonce = ethGetTransactionCount.getTransactionCount();

                /**
                 * Convert ammount ether to BigInteger
                 */
                BigInteger value = Convert.toWei(String.valueOf(sendethervalue.getText()), Convert.Unit.ETHER).toBigInteger();

                /**
                 * Транзакция
                 * Transaction
                 */
                RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(nonce, GasPrice, GasLimit, String.valueOf(sendtoaddress.getText()), value);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = "0x"+ Hex.toHexString(signedMessage);
                EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue.toString()).sendAsync().get();

                /**
                 * Get Transaction Error and Hash
                 */
                System.out.println("Error: "+ ethSendTransaction.getError());
                System.out.println("Transaction: " + ethSendTransaction.getTransactionHash());

                /**
                 * Возвращаем из потока, Адрес и Хэш транзакции
                 * Returned from thread, Ether Address and transaction hash
                 */
                JSONObject JsonResult = new JSONObject();
                JsonResult.put("Address", address);
                JsonResult.put("TransactionHash", ethSendTransaction.getTransactionHash());

                return JsonResult;

            }catch (Exception ex) {ex.printStackTrace();}
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                /**
                 * Получаем баланс Ethereum
                 * Get balance Ethereum
                 */
                EthGetBalance etherbalance = web3.ethGetBalance(result.get("Address").toString(), DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalanceafter = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                System.out.println("Eth Balance: " + ethbalanceafter);
                ethbalance.setText(ethbalanceafter);
            } catch(Exception ex) {System.out.println(ex);}
            Toast toast = null;
            try {
                toast = Toast.makeText(getApplicationContext(),result.get("TransactionHash").toString(), Toast.LENGTH_LONG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            toast.show();
        }
    }
    //////////////////// End Sending Ether ///////////////////////
}

