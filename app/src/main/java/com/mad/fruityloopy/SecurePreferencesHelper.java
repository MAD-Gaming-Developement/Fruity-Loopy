package com.mad.fruityloopy;


import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecurePreferencesHelper {

    private final String SHARED_PREFS_FILE_NAME = "secure_prefs";
    private SharedPreferences sharedPreferences;
    private Context context;

    public SecurePreferencesHelper(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Initialize SecuredPreferences
     **/
    public void initialize() throws GeneralSecurityException, IOException {
        // Check if encryption key and IV are already set
        String encryptionKey = sharedPreferences.getString("encryption_key", null);
        String ivBase64 = sharedPreferences.getString("iv", null);

        if (encryptionKey == null || ivBase64 == null) {
            // Generate new encryption key and IV
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("encryption_key", masterKeyAlias);
            byte[] newIv = generateIV();

            editor.putString("iv", Base64.getEncoder().encodeToString(newIv));
            Log.d(MainActivity.TAG, "New SecuredPrefs Generated:\nMasterKey: " + masterKeyAlias + "\nIV: " + Base64.getEncoder().encodeToString(newIv));

            editor.apply();
            editor.commit();
            createSecurePrefs();
        }
        else
        {
            createSecurePrefs();
        }

    }

    private void createSecurePrefs() throws GeneralSecurityException, IOException {
        String masterKeyAlias = sharedPreferences.getString("encryption_key", null);
        //byte[] ivBytes = Base64.getDecoder().decode(sharedPreferences.getString("iv", null));
        assert masterKeyAlias != null;
        sharedPreferences = EncryptedSharedPreferences.create(
                SHARED_PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );


    }

    // Save a string value securely
    public void saveString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    // Retrieve a string value securely
    public  String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Remove a value securely
    public  void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
        editor.commit();
    }

    // Clear all values securely
    public  void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        editor.commit();
    }

    // Method to generate a random IV
    private static byte[] generateIV() {
        byte[] iv = new byte[16]; // 16 bytes for AES
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // Encrypt a string value
    public String encryptString(String value) throws GeneralSecurityException, IOException {
        String masterKeyAlias = sharedPreferences.getString("encryption_key", null);
        String ivBase64 = sharedPreferences.getString("iv", null);

        if (ivBase64 == null) {
            throw new IllegalStateException("IV is null. SecurePreferencesHelper not properly initialized.");
        }

        byte[] ivBytes = Base64.getDecoder().decode(ivBase64);

        SecretKey masterKey = getOrCreateSecretKey(masterKeyAlias);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(128, ivBytes));
        byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt a string value
    public  String decryptString(String encryptedValue) throws GeneralSecurityException, IOException {
        String masterKeyAlias = sharedPreferences.getString("encryption_key", null);
        byte[] ivBytes = Base64.getDecoder().decode(sharedPreferences.getString("iv", null));

        SecretKey masterKey = getOrCreateSecretKey(masterKeyAlias);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(128, ivBytes));
        byte[] decodedEncryptedBytes = Base64.getDecoder().decode(encryptedValue);
        byte[] decryptedBytes = cipher.doFinal(decodedEncryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // Helper method to obtain SecretKey from masterKeyAlias
    private static SecretKey getOrCreateSecretKey(String masterKeyAlias) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(masterKeyAlias)) {
            // Generate a new secret key
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    masterKeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        }

        // Retrieve the secret key
        return (SecretKey) keyStore.getKey(masterKeyAlias, null);
    }
}